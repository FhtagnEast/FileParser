package com.gmail.fhtagneast;

import java.io.*;
import java.nio.file.Path;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;


public class MatchFinder {

    final private int cachingBufferOffset;
    final private AbstractQueue<Path> pathQueue;
    final private char[] patternForSearch;
    final private int bufferLength;

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

                    final String matchWithTextSurround = cachingMatch(massiveToParse,
                            i - patternOffset,
                            patternForSearch.length,
                            this.cachingBufferOffset);

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
//                    if (i < 0) {
//                        i = 0; //TODO: did it normally
//                    }
                    patternOffset = 0;
                }
            }
        }

        this.initialPositionInMatchPatternForSearch = patternOffset;

        //objectOutputStreamForMatches.close();
        return listOfMatchedPositionsWithCache;
    }


    public String cachingMatch(final char[] massiveWithMatch, // FIXME Not a caching match at all
                               final int position,
                               final int patternLength,
                               final int offset) { // TODO Rename
        int startIndex = position - offset;
        if (startIndex < 0) {
            startIndex = 0;
        }

        int endIndex = position + patternLength + offset;
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

    public ArrayList<TextMatchWithCachedSurround> parseFile() throws IOException {

        if (isBusy){
            return new ArrayList<>();
        }
       // System.out.println("ok");
        final BufferedReader b = new BufferedReader(new FileReader(pathQueue.remove().toString()));
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
            blockRead = this.readBlock(b, bufferLength);
        }

        return arrayOfMatchPositionsAndCachedSurround;
    }


    public String cacheMassiveToString(final char[] cache,
                                       final int positionOfStartingSymbol) {
        return new String(cache,
                positionOfStartingSymbol,
                (cache.length - positionOfStartingSymbol)) +
                new String(cache,
                        0,
                        positionOfStartingSymbol);
    }

    public int findMatchesBackwardInMatchCache(final char[] cache,
                                               final char[] pattern,
                                               final int stepsBackInMassive,
                                               final long positionInText) {

        //int positionInMassive = initialPositionInMassive - stepsBackInMassive;
        int positionInCacheMassive;
        int positionInPatternMassive = 0;

        for (int i = 0; i < stepsBackInMassive; i++) {
            positionInCacheMassive = (int) (positionInText - stepsBackInMassive + i) % cache.length;

            if (pattern[i] == pattern[positionInPatternMassive]) {
                positionInPatternMassive++;
            } else {
                if (positionInPatternMassive != 0) {
                    i -= positionInCacheMassive;
                    positionInPatternMassive = 0;
                }
            }
        }

        return positionInPatternMassive;
    }

    public static void main(String[] args) {

        final AbstractQueue<Path> queue = new ConcurrentLinkedQueue<>();
        final MatchFinder matchFinder = new MatchFinder(
                5000000,
                10,
                queue,
                "JOJ".toCharArray());
        final FileBuffer fileBuffer = new FileBuffer(queue, matchFinder);
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
return;
    }

}
