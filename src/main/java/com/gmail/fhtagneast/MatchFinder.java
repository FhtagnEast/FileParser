package com.gmail.fhtagneast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class MatchFinder implements Runnable {

    final private int cachingBufferOffset;
    final private AbstractQueue<Path> pathQueue;
    final private char[] patternForSearch;
    final private int bufferLength;
    final private HashMap<Path, ArrayList<TextMatchWithCachedSurround>> hashMapFromFilesAndMatchArrays = new HashMap<>();
    // final private FileBuffer fileBuffer;

    private boolean isBusy = false;
    private long currentPositionInText;
    private int initialPositionInMatchPatternForSearch;

    public MatchFinder(
            int bufferLength,
            int cachingBufferOffset,
            AbstractQueue<Path> pathQueue,
            char[] patternForSearch) {
        this.bufferLength = bufferLength;
        this.cachingBufferOffset = cachingBufferOffset;
        this.pathQueue = pathQueue;
        this.patternForSearch = patternForSearch;
    }


    public char[] readBlock(final BufferedReader b, final int length) throws IOException {
        final char[] buf = new char[length];
        final int charsRead = b.read(buf, 0, length);

        if (charsRead != -1) {
            return buf;
        } else {
            return null;
        }
    }

    public ArrayList<TextMatchWithCachedSurround> findMatchesInArray(final char[] massiveToParse

                                                    /*,
                                                    final File fileToParse*/) throws IOException {
        final ArrayList<TextMatchWithCachedSurround> listOfMatchedPositionsWithCache = new ArrayList<>();
        long initialMatchPosition = currentPositionInText - initialPositionInMatchPatternForSearch;
        int matchCounter = 0;

        // final FileOutputStream outputStreamForMatchObjects = new FileOutputStream(String.valueOf(fileToParse.hashCode()), true);
        //final ObjectOutputStream objectOutputStreamForMatches = new ObjectOutputStream(outputStreamForMatchObjects);

        // todo: try with resources java
//        try (final ObjectOutputStream objectOutputStreamForMatches = new ObjectOutputStream(outputStreamForMatchObjects)) {
//
//        }

        int patternOffset = initialPositionInMatchPatternForSearch;

        for (int i = patternForSearch.length; i < massiveToParse.length; i++) {

            if (massiveToParse[i] == patternForSearch[patternOffset]) {

                if (patternOffset == 0) {
                    initialMatchPosition = currentPositionInText + i;
                }

                patternOffset++;

                if (patternOffset == patternForSearch.length) {

                    final String matchWithTextSurround = mergingMatchWithSurround(massiveToParse,
                            i - patternOffset + patternForSearch.length);
                    System.out.println(massiveToParse[i - patternOffset + patternForSearch.length]);

                    //objectOutputStreamForMatches.writeObject(new TextMatchWithCachedSurround(initialMatchPosition, matchWithTextSurround));

                    TextMatchWithCachedSurround match = new TextMatchWithCachedSurround(initialMatchPosition, matchWithTextSurround);
                    System.out.println(match.toString());
                    listOfMatchedPositionsWithCache.add(match);
                    matchCounter++;

                    patternOffset = 0;
                }

            } else {
                if (patternOffset != 0) {
                    i -= patternOffset;
                    patternOffset = 0;
                }
            }
        }

        this.initialPositionInMatchPatternForSearch = patternOffset;

        //objectOutputStreamForMatches.close();
        return listOfMatchedPositionsWithCache;
    }


    public String mergingMatchWithSurround(final char[] massiveWithMatch,
                                           final int position) {
        int startIndex = position - cachingBufferOffset;
        if (startIndex < 0) {
            startIndex = 0;
        }

        int endIndex = position + patternForSearch.length + cachingBufferOffset;
        if (endIndex >= massiveWithMatch.length) {
            endIndex = massiveWithMatch.length - 1;
        }

        final char[] outputArray = Arrays.copyOfRange(massiveWithMatch, startIndex, endIndex);

        return String.valueOf(outputArray);
    }

    public static char[] mergeCharArrays(char[] a, char[] b) {
        int length = a.length + b.length;
        char[] mergedMassive = new char[length];
        System.arraycopy(a, 0, mergedMassive, 0, a.length);
        System.arraycopy(b, 0, mergedMassive, a.length, b.length);
        return mergedMassive;
    }

    public void parseFile() throws IOException {

        if (isBusy) {
            return;
        }
        // System.out.println("ok");
        final Path pathToFileForParse = pathQueue.remove();
        final FileReader fileReaderForFileForParse = new FileReader(pathToFileForParse.toString());
        final BufferedReader b = new BufferedReader(fileReaderForFileForParse);
        final ArrayList<TextMatchWithCachedSurround> arrayOfMatchPositionsAndCachedSurround = new ArrayList<>();
//        final FileOutputStream outputStreamForMatchObjects = new FileOutputStream(String.valueOf(fileToParse.hashCode()));
//        final ObjectOutputStream objectOutputStreamForMatches = new ObjectOutputStream(outputStreamForMatchObjects);

        char[] endOfOlderReadMassivePart = new char[patternForSearch.length];
        char[] blockRead = this.readBlock(b, bufferLength);
        char[] fullReadMassive;
        currentPositionInText = 0L;
        this.initialPositionInMatchPatternForSearch = 0;
        int matchCounter = 0;

        while (blockRead != null) {

            fullReadMassive = mergeCharArrays(endOfOlderReadMassivePart, blockRead);

//            matchCounter += this.findMatchesInArray(
//                    fullReadMassive
//                    //patternForSearch.toCharArray(),
//                    //initialPosition
//                    );

            //initialPosition = initialPositionInMatchPatternForSearch;

            //new ArrayList<TextMatchWithCachedSurround>
            ArrayList<TextMatchWithCachedSurround> foundMatches = this.findMatchesInArray(fullReadMassive);

            arrayOfMatchPositionsAndCachedSurround.addAll(foundMatches);

            currentPositionInText += bufferLength;
//            System.arraycopy(fullReadMassive,
//                    fullReadMassive.length - 1 - patternForSearch.length,
//                    endOfOlderReadMassivePart ,
//                    0,
//                    endOfOlderReadMassivePart.length);
            blockRead = this.readBlock(b, bufferLength);
        }


        hashMapFromFilesAndMatchArrays.put(pathToFileForParse, arrayOfMatchPositionsAndCachedSurround);

        ArrayList<TextMatchWithCachedSurround> test = hashMapFromFilesAndMatchArrays.get(pathToFileForParse);
        for(TextMatchWithCachedSurround point : test) {
            System.out.println(point.toString());
        }

        if (!pathQueue.isEmpty()) {
            parseFile();
        } else {
            return;
        }
    }


    // @Override
    public void run() {


//        final MatchFinder matchFinder = new MatchFinder(
//                4,
//                10,
//                queue,
//                "JOJO".toCharArray());
        final FileBuffer fileBuffer = new FileBuffer(this.pathQueue, this);
        final FileFinder fileFinder = new FileFinder("D:\\FhtagnEast\\Downloads\\indiv18\\itcont.txt",
                ".txt", fileBuffer);

        long start, finish;

        start = System.currentTimeMillis();
        new Thread(fileFinder).start();
        finish = System.currentTimeMillis();
        System.out.println("Time: " + (finish - start));

        // String patternForSearch = "JOJ";
        //  try {
//            final ArrayList<Path> pathsList =
//                    fileFinder.searchFilesMatchExtension(
//                            "D:\\FhtagnEast\\Downloads\\indiv18\\test.txt",
//                            ".txt");
//
        // final ArrayList<Path> pathsList = fileFinder.searchFiles();


////            final ArrayList<Path> pathsList =
////                    fileFinder.searchFilesMatchExtension(
////                            "D:\\FhtagnEast\\Downloads\\indiv18\\by_date\\",
////                            ".txt");


        //  start = System.currentTimeMillis();

        //new Thread()

//            for (Path path : pathsList) {
//
//                final File fileToParse = new File(path.toString());
//
//
//                int numberOfMatches =
//                        matchFinder.parseFile(); //MARY goes bad
//
//
//                System.out.println(path.toString() + ", " + numberOfMatches);
////            for (TextMatchWithCachedSurround match : output){
////                System.out.println(match.getPositionInWholeText());
////                System.out.println(match.getStringOfMatchWithSurround());
////            }
//            }

//            finish = System.currentTimeMillis();
//            System.out.println("Time: " + (finish - start));
//
//            start = System.currentTimeMillis();
//
//            for (Path path : pathsList1) {
//
//                final File fileToParse = new File(path.toString());
//
//
//                final ArrayList<TextMatchWithCachedSurround> output =
//                        fileFinder.parseFileForMatchedPatterns(fileToParse,
//                                patternForSearch.toCharArray(), 5000); //MARY goes bad
//
//                System.out.println(path.toString() + ", " + output.size());
////            for (TextMatchWithCachedSurround match : output){
////                System.out.println(match.getPositionInWholeText());
////                System.out.println(match.getStringOfMatchWithSurround());
////            }
//            }
//
//            finish = System.currentTimeMillis();
//            System.out.println("Time: " + (finish - start));


//            start = System.currentTimeMillis();
//
//            for (Path path : pathsList) {
//
//                final File fileToParse = new File(path.toString());
//
//
//                final ArrayList<TextMatchWithCachedSurround> output =
//                        fileFinder.parseFile(fileToParse,
//                                patternForSearch, 5000); //MARY goes bad
//
//                System.out.println(path.toString() + ", " + output.size());
////                for (TextMatchWithCachedSurround match : output) {
////                    System.out.println(match.getPositionInWholeText());
////                    System.out.println(match.getStringOfMatchWithSurround());
////                }
//            }
//
//            finish = System.currentTimeMillis();
//
//            System.out.println("Time: " + (finish - start));

//        } catch (final IOException e) {
//            System.out.println("IO error");
//            e.printStackTrace();
//        }

        if (pathQueue.isEmpty() & fileBuffer.getFilesRanOut()) {
            return;
        }

    }

    public static void main(String[] args){
        final AbstractQueue<Path> queue = new ConcurrentLinkedQueue<>();
        final MatchFinder matchFinder = new MatchFinder(500000,
                10,
                queue,
                "JOJO".toCharArray());
        new Thread(matchFinder).start();


    }
}
