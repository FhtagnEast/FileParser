package com.gmail.fhtagneast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BufferedReaderWrapper {

    private BufferedReader reader;
    private int positionInArray;
    private char[] bufferOfChars;
    private int internalCounter;
    private int charsRead;


    public BufferedReaderWrapper(String fileToParse, int bufferLength) throws IOException {
        this.reader = new BufferedReader(new FileReader(fileToParse));
        this.bufferOfChars = new char[bufferLength];
        this.internalCounter = 0;
        this.charsRead = this.reader.read(this.bufferOfChars, 0, this.bufferOfChars.length);
    }

    public int read() throws IOException {
        if (this.internalCounter >= this.charsRead) {
            this.charsRead = this.reader.read(this.bufferOfChars, 0, bufferOfChars.length);
            internalCounter = 0;
            if (this.charsRead == -1) {
                return -1;
            }
        }


        this.internalCounter++;

        return this.bufferOfChars[this.internalCounter - 1];


    }

}


