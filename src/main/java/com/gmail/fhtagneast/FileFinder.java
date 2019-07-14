package com.gmail.fhtagneast;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileFinder implements Runnable {

    final private String extension;
    final private String directory;
    final private FileBuffer buffer;

    public FileFinder(String directory, String extension, FileBuffer buffer) {
        this.directory = directory;
        this.extension = extension;
        this.buffer = buffer;
    }

   // @Override
    public void run() {

        //final FileBuffer fileB = new ArrayList<>();

//        try(Files.walkFileTree(Paths.get(directory), new FileVisitor<>() {
        buffer.setFilesRanOut(false);
        try{Files.walkFileTree(Paths.get(directory), new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                        if (file.toString().toLowerCase().endsWith(extension)) {
                            buffer.addFile(file);
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
                }

        );
            buffer.setFilesRanOut(true);
        } catch (IOException exc){
            buffer.setFilesRanOut(true);
            exc.printStackTrace();
        }
        //return;
//        } catch (IOException exc){
//            exc.printStackTrace();
//        }
    }


}
