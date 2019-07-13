package com.gmail.fhtagneast;

import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractQueue;
import java.util.ArrayList;

public class FileBuffer {
    final AbstractQueue<Path> filesQueue;
    final MatchFinder matchFinder;

    public FileBuffer (AbstractQueue<Path> filesQueue,
                       MatchFinder matchFinder){
        this.filesQueue = filesQueue;
        this.matchFinder = matchFinder;
    }

    public void addFile(Path path){
      //  System.out.println(path.toString());
        filesQueue.add(path);
        try{
            ArrayList<TextMatchWithCachedSurround> matches = matchFinder.parseFile();
        //for(TextMatchWithCachedSurround match : matches){
            //System.out.println(match.toString());
        }


        catch (IOException exc){
            exc.printStackTrace();
        }

    }

}
