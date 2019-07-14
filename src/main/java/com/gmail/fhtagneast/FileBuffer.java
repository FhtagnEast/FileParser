package com.gmail.fhtagneast;

import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractQueue;
import java.util.ArrayList;

public class FileBuffer {
    private final AbstractQueue<Path> filesQueue;
    private final MatchFinder matchFinder;
    private boolean filesRanOut = false;

    public FileBuffer (AbstractQueue<Path> filesQueue,
                       MatchFinder matchFinder){
        this.filesQueue = filesQueue;
        this.matchFinder = matchFinder;
    }

    public void setFilesRanOut(boolean filesRanOut){
        this.filesRanOut = filesRanOut;
    }

    public boolean getFilesRanOut(){
        return this.filesRanOut;
    }

    public void addFile(Path path){
      //  System.out.println(path.toString());
        filesQueue.add(path);
        try{
            matchFinder.parseFile();
            //System.out.println(matches.size());
        //for(TextMatchWithCachedSurround match : matches){
            //System.out.println(match.toString());
        }
        catch (IOException exc){
            exc.printStackTrace();
        }

    }

}
