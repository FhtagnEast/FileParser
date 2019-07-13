package com.gmail.fhtagneast;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;


public class MatchFinder {

    private int cachingBufferOffset;
    private int initialPositionInMatchPatternForSearch;

    public MatchFinder(int bufferSize) {
        this.cachingBufferOffset = bufferSize;
    }

    public ArrayList<Path> searchFilesMatchExtension(final String directory,
                                                     final String extension) throws IOException {

        final ArrayList<Path> filesList = new ArrayList<>();

        Files.walkFileTree(Paths.get(directory), new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                if (file.toString().toLowerCase().endsWith(extension)) {
                    filesList.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        return filesList;
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

    public int findMatchesWithFixedBackwardStepping (final char[] massiveToParse,
                                                                                       final char[] massiveWithPattern,
                                                                                       final int initialPatternOffset,
                                                                                       final long currentTextPosition,
                                                                                       final File fileToParse) throws IOException{
//        final ArrayList<TextMatchWithCachedSurround> listOfMatchedPositionsWithCache = new ArrayList<>();
        long initialMatchPosition = currentTextPosition - initialPatternOffset;
        int matchCounter = 0;

        final FileOutputStream outputStreamForMatchObjects = new FileOutputStream(String.valueOf(fileToParse.hashCode()), true);
        final ObjectOutputStream objectOutputStreamForMatches = new ObjectOutputStream(outputStreamForMatchObjects);

        // todo: try with resources java
//        try (final ObjectOutputStream objectOutputStreamForMatches = new ObjectOutputStream(outputStreamForMatchObjects)) {
//
//        }

        int patternOffset = initialPatternOffset;

        for (int i = massiveWithPattern.length; i < massiveToParse.length; i++) {

            if (massiveToParse[i] == massiveWithPattern[patternOffset]) {

                if (patternOffset == 0) {
                    initialMatchPosition = currentTextPosition + i;
                }

                patternOffset++;

                if (patternOffset == massiveWithPattern.length) {

                    final String matchWithTextSurround = cachingMatch(massiveToParse,
                            i - patternOffset,
                            massiveWithPattern.length,
                            this.cachingBufferOffset);

                    objectOutputStreamForMatches.writeObject(new TextMatchWithCachedSurround(initialMatchPosition, matchWithTextSurround));

                    //listOfMatchedPositionsWithCache.add(new TextMatchWithCachedSurround(initialMatchPosition, matchWithTextSurround));
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

        objectOutputStreamForMatches.close();
        return matchCounter;
    }


    public ArrayList<TextMatchWithCachedSurround> findMatches(final char[] massiveToParse,
                                                              final char[] massiveWithPattern,
                                                              final int initialPatternOffset,
                                                              final long currentTextPosition) {
        final ArrayList<TextMatchWithCachedSurround> listOfMatchedPositionsWithCache = new ArrayList<>();
        long initialMatchPosition = currentTextPosition - initialPatternOffset;

        int patternOffset = initialPatternOffset;

        for (int i = 0; i < massiveToParse.length; i++) {

            if (massiveToParse[i] == massiveWithPattern[patternOffset]) {

                if (patternOffset == 0) {
                    initialMatchPosition = currentTextPosition + i;
                }

                patternOffset++;

                if (patternOffset == massiveWithPattern.length) {

                    final String matchWithTextSurround = cachingMatch(massiveToParse,
                            i - patternOffset,
                            massiveWithPattern.length,
                            50);

                    listOfMatchedPositionsWithCache.add(new TextMatchWithCachedSurround(initialMatchPosition, matchWithTextSurround));

                    patternOffset = 0;
                }

            } else {
                if (patternOffset != 0) {
                    i -= patternOffset;
                    if (i < 0) {
                        i = 0;
                    }
                    patternOffset = 0;
                }
            }
        }

        this.initialPositionInMatchPatternForSearch = patternOffset;

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

    public static char[] mergeCharMassives(char[] a, char[] b) {
        int length = a.length + b.length;
        char[] mergedMassive = new char[length];
        System.arraycopy(a, 0, mergedMassive, 0, a.length);
        System.arraycopy(b, 0, mergedMassive, a.length, b.length);
        return mergedMassive;
    }

    public int parseFileWithFixedBackwardStepping(final File fileToParse, //good worked one
                                                                                     final String pattern,
                                                                                     final int bufferLength) throws IOException {

        final BufferedReader b = new BufferedReader(new FileReader(fileToParse));
//       final ArrayList<TextMatchWithCachedSurround> arrayOfMatchPositionsAndCachedSurround = new ArrayList<>();
//        final FileOutputStream outputStreamForMatchObjects = new FileOutputStream(String.valueOf(fileToParse.hashCode()));
//        final ObjectOutputStream objectOutputStreamForMatches = new ObjectOutputStream(outputStreamForMatchObjects);

        char[] endOfOlderReadMassivePart = new char[pattern.length()];
        char[] blockRead = this.readBlock(b, bufferLength);
        char[] fullReadMassive;
        long position = 0L;
        int initialPosition = 0;
        int matchCounter = 0;

        while (blockRead != null) {

            fullReadMassive = mergeCharMassives(endOfOlderReadMassivePart, blockRead);

            matchCounter += this.findMatchesWithFixedBackwardStepping(
                    fullReadMassive,
                    pattern.toCharArray(),
                    initialPosition,
                    position,
                    fileToParse);

            initialPosition = initialPositionInMatchPatternForSearch;

            //arrayOfMatchPositionsAndCachedSurround.addAll(foundMatches);

            position += position;
            blockRead = this.readBlock(b, bufferLength);
        }

        return matchCounter;
    }

    public ArrayList<TextMatchWithCachedSurround> parseFile(final File fileToParse,
                                                            final String pattern,
                                                            final int bufferLength) throws IOException {

        final BufferedReader b = new BufferedReader(new FileReader(fileToParse));
        final ArrayList<TextMatchWithCachedSurround> arrayOfMatchPositionsAndCachedSurround = new ArrayList<>();

        //char[] prePositionedMassive = new char[bufferLength + pattern.length()];

        //Arrays.fill
        long position = 0L;
        int initialPosition = 0;

        while (true) {
            final char[] blockRead = this.readBlock(b, bufferLength);

            if (blockRead == null) {
                break;
            }

            final ArrayList<TextMatchWithCachedSurround> foundMatches = this.findMatches(
                    blockRead,
                    pattern.toCharArray(),
                    initialPosition /*+ pattern.length()*/,
                    position);

            initialPosition = initialPositionInMatchPatternForSearch;

            arrayOfMatchPositionsAndCachedSurround.addAll(foundMatches);

            position += position;
        }

        return arrayOfMatchPositionsAndCachedSurround;
    }


    public ArrayList<TextMatchWithCachedSurround> parseFileForMatchedPatterns(final File fileToParse,
                                                                              final char[] pattern,
                                                                              final int bufferLength) throws IOException {
        final BufferedReaderWrapper buffer = new BufferedReaderWrapper(fileToParse.toString(), bufferLength);
        //final BufferedReader buffer = new BufferedReader(new FileReader(fileToParse));
        final ArrayList<TextMatchWithCachedSurround> listOfMatches = new ArrayList<>();
        char[] matchCache = new char[2 * this.cachingBufferOffset + pattern.length];
        int currentPositionInPatternMassive = 0;
        final LinkedList<Long> positionForDeferredCacheWrite = new LinkedList<>();

        int readLetter = buffer.read();
        long position = 0L;

        while (readLetter != -1) {


            matchCache[(int) (position % matchCache.length)] = (char) readLetter;
            if (readLetter == pattern[currentPositionInPatternMassive]) {
                currentPositionInPatternMassive++;

                if (currentPositionInPatternMassive == pattern.length) {
                    positionForDeferredCacheWrite.add(position + this.cachingBufferOffset);
                    currentPositionInPatternMassive = 0;
                }

            } else {
                if (currentPositionInPatternMassive != 0) {
                    currentPositionInPatternMassive = findMatchesBackwardInMatchCache(matchCache,
                            pattern,
                            currentPositionInPatternMassive - 1,
                            position);
                }
            }

            if (!positionForDeferredCacheWrite.isEmpty()) {
                if (positionForDeferredCacheWrite.getFirst() == position) {
                    listOfMatches.add(new TextMatchWithCachedSurround(position - (this.cachingBufferOffset + pattern.length - 1),
                            cacheMassiveToString(matchCache, (int) ((position + 1) % matchCache.length))));
                    positionForDeferredCacheWrite.removeFirst();
                }
            }

            position++;
            readLetter = buffer.read();
        }

        while (!positionForDeferredCacheWrite.isEmpty()) {
            listOfMatches.add(new TextMatchWithCachedSurround(positionForDeferredCacheWrite.getFirst() - (this.cachingBufferOffset + pattern.length - 1),
                    cacheMassiveToString(matchCache, (int) (positionForDeferredCacheWrite.getFirst() % matchCache.length))));
            positionForDeferredCacheWrite.removeFirst();
        }
        return listOfMatches;

    }

//    public void writeMatchToFile(final TextMatchWithCachedSurround match,
//                                 final File fileToWrite, final ObjectOutputStream output) throws IOException {
//        output.writeObject(match);
//    }

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

        final MatchFinder fileFinder = new MatchFinder(10);

        long start, finish;


        String patternForSearch = "MARY";
        try {
//            final ArrayList<Path> pathsList =
//                    fileFinder.searchFilesMatchExtension(
//                            "D:\\FhtagnEast\\Downloads\\indiv18\\test.txt",
//                            ".txt");
//
            final ArrayList<Path> pathsList =
                    fileFinder.searchFilesMatchExtension(
                            "D:\\FhtagnEast\\Downloads\\indiv18\\itcont.txt",
                            ".txt");


            final ArrayList<Path> pathsList1 =
                    fileFinder.searchFilesMatchExtension(
                            "D:\\FhtagnEast\\Downloads\\indiv18\\itcont1.txt",
                            ".txt");


            final ArrayList<Path> pathsList2 =
                    fileFinder.searchFilesMatchExtension(
                            "D:\\FhtagnEast\\Downloads\\indiv18\\itcont2.txt",
                            ".txt");

////            final ArrayList<Path> pathsList =
////                    fileFinder.searchFilesMatchExtension(
////                            "D:\\FhtagnEast\\Downloads\\indiv18\\by_date\\",
////                            ".txt");


            start = System.currentTimeMillis();

            for (Path path : pathsList2) {

                final File fileToParse = new File(path.toString());


                int numberOfMatches=
                        fileFinder.parseFileWithFixedBackwardStepping(fileToParse,
                                patternForSearch, 5000000); //MARY goes bad



                System.out.println(path.toString() + ", " + numberOfMatches);
//            for (TextMatchWithCachedSurround match : output){
//                System.out.println(match.getPositionInWholeText());
//                System.out.println(match.getStringOfMatchWithSurround());
//            }
            }

            finish = System.currentTimeMillis();
            System.out.println("Time: " + (finish - start));
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

        } catch (final IOException e) {
            System.out.println("IO error");
            e.printStackTrace();
        }

    }

}
