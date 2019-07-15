package com.gmail.fhtagneast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;


class MatchFinder {

    final private int cachingBufferOffset;
    final private char[] patternForSearch;
    final private int bufferLength;
    final private ConcurrentHashMap<Path,
            ArrayList<TextMatchWithCachedSurround>>
            hashMapFromFilesAndMatchArrays = new ConcurrentHashMap<>();

    private long currentPositionInText;
    private int initialPositionInMatchPatternForSearch;


    MatchFinder(
            int bufferLength,
            int cachingBufferOffset,
            char[] patternForSearch) {
        this.bufferLength = bufferLength;
        this.cachingBufferOffset = cachingBufferOffset;
        this.patternForSearch = patternForSearch;
    }

    ArrayList<TextMatchWithCachedSurround> getCachedSurroundForPath(Path path) {
        return hashMapFromFilesAndMatchArrays.get(path);
    }

    private char[] readBlock(final BufferedReader b, final int length) throws IOException {
        final char[] buf = new char[length];
        final int charsRead = b.read(buf, 0, length);

        if (charsRead != -1) {
            return buf;
        } else {
            return null;
        }
    }

    private ArrayList<TextMatchWithCachedSurround> findMatchesInArray(
            final char[] massiveToParse) {
        final ArrayList<TextMatchWithCachedSurround> listOfMatchedPositionsWithCache = new ArrayList<>();

        int patternOffset = initialPositionInMatchPatternForSearch;

        for (int i = patternForSearch.length; i < massiveToParse.length; i++) {

            if (massiveToParse[i] == patternForSearch[patternOffset]) {

                patternOffset++;

                if (patternOffset == patternForSearch.length) {

                    final String matchWithTextSurround = mergingMatchWithSurround(massiveToParse,
                            i - ( patternOffset - 1 ));


                    TextMatchWithCachedSurround match =
                            new TextMatchWithCachedSurround(
                                    i - patternOffset - patternForSearch.length + currentPositionInText,
                                    matchWithTextSurround);
                    listOfMatchedPositionsWithCache.add(match);

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
        return listOfMatchedPositionsWithCache;
    }


    private String mergingMatchWithSurround(final char[] massiveWithMatch,
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

    private static char[] mergeCharArrays(char[] a, char[] b) {
        int length = a.length + b.length;
        char[] mergedMassive = new char[length];
        System.arraycopy(a, 0, mergedMassive, 0, a.length);
        System.arraycopy(b, 0, mergedMassive, a.length, b.length);
        return mergedMassive;
    }

    ArrayList<TextMatchWithCachedSurround> parseFile(Path path) throws IOException {

        final FileReader fileReaderForFileForParse = new FileReader(path.toString());
        final BufferedReader b = new BufferedReader(fileReaderForFileForParse);
        final ArrayList<TextMatchWithCachedSurround> arrayOfMatchPositionsAndCachedSurround = new ArrayList<>();

        char[] endOfOlderReadMassivePart = new char[patternForSearch.length];
        char[] blockRead = this.readBlock(b, bufferLength);
        char[] fullReadMassive;
        currentPositionInText = 0L;
        this.initialPositionInMatchPatternForSearch = 0;

        while (blockRead != null) {

            fullReadMassive = mergeCharArrays(endOfOlderReadMassivePart, blockRead);

            ArrayList<TextMatchWithCachedSurround> foundMatches = this.findMatchesInArray(fullReadMassive);

            arrayOfMatchPositionsAndCachedSurround.addAll(foundMatches);
            endOfOlderReadMassivePart = Arrays.copyOfRange(blockRead,
                    blockRead.length - patternForSearch.length - 1,
                    blockRead.length);

            currentPositionInText += bufferLength;
            blockRead = this.readBlock(b, bufferLength);
        }

        hashMapFromFilesAndMatchArrays.put(path, arrayOfMatchPositionsAndCachedSurround);

        return arrayOfMatchPositionsAndCachedSurround;
    }

}
