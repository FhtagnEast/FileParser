package com.gmail.fhtagneast;

import java.io.Serializable;

public class TextMatchWithCachedSurround implements Serializable {
    final private String cachedSurroundOfMatch;
    final private long matchPositionInWholeText;
    private int numberOfStringWithMatch = 0;
    private long matchPositionInString = 0;

    public TextMatchWithCachedSurround(
            long position,
            String match) {
        this.matchPositionInWholeText = position;
        this.cachedSurroundOfMatch = match;
    }

    public TextMatchWithCachedSurround(
            long positionInText,
            String match,
            int numberOfString,
            long positionInString) {
        this.matchPositionInWholeText = positionInText;
        this.cachedSurroundOfMatch = match;
        this.numberOfStringWithMatch = numberOfString;
        this.matchPositionInString = positionInString;
    }

    public long getMatchPositionInWholeText() {
        return this.matchPositionInWholeText;
    }

    public String getCachedSurroundOfMatch() {
        return this.cachedSurroundOfMatch;
    }

    public int getNumberOfStringWithMatch() {
        return this.numberOfStringWithMatch;
    }

    public long getMatchPositionInString() {
        return this.matchPositionInString;
    }

    public void setMatchPositionInString(long matchPositionInString) {
        this.matchPositionInString = matchPositionInString;
    }

    public void setNumberOfStringWithMatch(int numberOfStringWithMatch) {
        this.numberOfStringWithMatch = numberOfStringWithMatch;
    }

    @Override
    public String toString(){
        String output = Long.toString(this.matchPositionInWholeText) + " " + cachedSurroundOfMatch;
        return output;
    }
}

