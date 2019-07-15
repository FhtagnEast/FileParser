package com.gmail.fhtagneast;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

class FileFinder {

    final private String extension;
    final private String directory;

    FileFinder(String directory, String extension) {
        this.directory = directory;
        this.extension = extension;
    }

    ArrayList<Path> findFiles() {

        final ArrayList<Path> listOfPaths = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get(directory), new FileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                            if (file.toString().toLowerCase().endsWith(extension)) {
                                System.out.println(file.toString());
                               listOfPaths.add(file);
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

        } catch (IOException exc) {
            exc.printStackTrace();
        }

        return listOfPaths;
    }
}
