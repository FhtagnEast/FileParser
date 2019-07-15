package com.gmail.fhtagneast;

import java.io.Serializable;

public class TextMatchWithCachedSurround implements Serializable {
    final private String cachedSurroundOfMatch;
    final private long matchPositionInWholeText;
   // private int numberOfStringWithMatch = 0;
//    private long matchPositionInString = 0;

    TextMatchWithCachedSurround(
            long position,
            String match) {
        this.matchPositionInWholeText = position;
        this.cachedSurroundOfMatch = match;
    }

//    public TextMatchWithCachedSurround(
//            long positionInText,
//            String match,
//            int numberOfString,
//            long positionInString) {
//        this.matchPositionInWholeText = positionInText;
//        this.cachedSurroundOfMatch = match;
//        this.numberOfStringWithMatch = numberOfString;
//        this.matchPositionInString = positionInString;
//    }

    @Override
    public String toString(){
        return "Match in: " +
                this.matchPositionInWholeText +
                " char. Match: " +
                cachedSurroundOfMatch;
    }
}

