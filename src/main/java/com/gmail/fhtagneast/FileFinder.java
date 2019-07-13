package com.gmail.fhtagneast;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import static sun.jvm.hotspot.runtime.BasicObjectLock.size;

public class FileFinder /*implements DirectoryParcer */{

// public File[] getFiles(String directory, String extension) {
//        final String ext = extension;
//        File inputDirectory = new File(directory);
//        //ArrayList outputArray = new ArrayList();
//
//        File[] directoryList = inputDirectory.listFiles(new FileFilter() {
//            public boolean accept(File directoryTested) {
//                return directoryTested.isDirectory() | (directoryTested.getName().matches(".+(" + ext + ")$")
//                        & directoryTested.isFile());
//            }
//
//        });
//
//        return directoryList;
//        //return Arrays.asList(directoryList);
//    }
//
//    public File[] recursivelyGetFiles(File directory, String extension) {
//        final String ext = extension;
//        final File[] directoriesAndTargetFiles = directory.listFiles(directoryTested ->
//                directoryTested.isDirectory() |
//                        (directoryTested.getName().toLowerCase().matches(".+(" + ext + ")$")
//                                & directoryTested.isFile()));
//
//        File[] loopOutput = new File[0];
//
//        // TODO
////        if (directoriesAndTargetFiles == null) {
////
////        }
//
//        if (directoriesAndTargetFiles != null) {
//            for (final File f : directoriesAndTargetFiles) {
//                if (f.isDirectory()) {
//                    //System.out.println(f.toString());
//                    final File[] loopDirectory = recursivelyGetFiles(f, extension);
//                    if (loopDirectory != null) {
//                        loopOutput = mergeFiles(loopOutput, loopDirectory);
//                    }
//                }
//            }
//        }
//        if (loopOutput != null & directoriesAndTargetFiles != null) {
//            return mergeFiles(directoriesAndTargetFiles, loopOutput);
//        }
//
//        return new File[0];
//    }
//
//    public ArrayList<File> cleanDirectories(File[] dirs) {
//        ArrayList<File> output = new ArrayList<>();
//        for (File dir : dirs) {
//            if (dir.isFile()) {
//                output.add(dir);
//            }
//        }
//        return output;
//    }

//    public File[] mergeFiles(File[] first, File[] second) {
//        File[] output = new File[first.length + second.length];
//        System.arraycopy(first, 0, output, 0, first.length);
//        System.arraycopy(second, 0, output, first.length, second.length);
//        return output;
//    }
//
//    public <T> T[] mergeMassives(T[] first, T[] second) {
//        //T[] output = new T[first.length + second.length];
//        T[] output = (T[]) Array.newInstance(first.getClass(), first.length + second.length);
//        System.arraycopy(first, 0, output, 0, first.length);
//        System.arraycopy(second, 0, output, first.length, second.length);
//        return output;
//    }
//    public File[] recursiveGetFiles (String directory, String extension, File[] fileArray){
//        if (fileArray.length == 0) return fileArray;
//        File[] outputArray = new File[0]; //exception question
//        for (File f : fileArray){
//            if f.isDirectory() f=0;
//            File[] recursiveFileArray = getFiles(f.toString(), extension);
//            outputArray = recursiveGetFiles(directory, extension, recursiveFileArray);
//        }
//        return outputArray;
//    }

    public ArrayList<Path> recursiveWalkFiles(String directory, String extension) throws IOException {

        ArrayList<Path> filesList = new ArrayList<>();
        Files.walkFileTree(Paths.get(directory), new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().toLowerCase().endsWith(extension)) {
                    filesList.add(file);
                    //System.out.println("visit file: " + file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        //for (Path path:filesList)
        //System.out.println(path);
        return filesList;
    }

    public List getMatches(List directories) {


        return directories;
    }

    //public ArrayList parceFile(Path inputFile, String pattern) throws IOException{
    public String readFile(BufferedReader b, int length) throws IOException{
        //ArrayList<Integer> matchPositionInText= new ArrayList<>();
        //final File fileToParce = new File(inputFile.toString());
        //BufferedReader b = new BufferedReader(new FileReader(fileToParce))){
            //int length = 10;
            char[] buf = new char[length];
            int charsRead = b.read(buf, 0, length);

            String result;
            if (charsRead != -1){
                result = new String(buf, 0, charsRead);
            } else {
                result = "";
            }
            return result;



        //System.out.println(inputFile.toString());

        //return matchPositionInText;
    }


    public ArrayList<ArrayList> parceFile(char[] massiveToParce, char[] massiveWithPattern,
                                           int initialPositionInPatternMassive,
                                           BigInteger currentTextPosition) {
        ArrayList<ArrayList> output = new ArrayList<>();
        BigInteger initialMatchPosition = currentTextPosition.add(new BigInteger(Integer.toString(-initialPositionInPatternMassive)));
        //System.out.println(initialMatchPosition);
        int positionInPatternMassive = initialPositionInPatternMassive;

        for (int i = 0; i < massiveToParce.length; i++) {
            if (massiveToParce[i] == massiveWithPattern[positionInPatternMassive]) {
                if (positionInPatternMassive == 0) {
                    initialMatchPosition = currentTextPosition.add(new BigInteger(Integer.toString(i)));
                }
                positionInPatternMassive++;
                if (positionInPatternMassive == massiveWithPattern.length){
                    ArrayList<String> out = new ArrayList<>();
                    out.add(initialMatchPosition.toString());
                    out.add(cachingMatch(massiveToParce, i - positionInPatternMassive, massiveWithPattern.length, 50));
                    output.add(out);
                    positionInPatternMassive = 0;
                }

            } else {
                if (positionInPatternMassive != 0) {
                    i -= positionInPatternMassive;
                    if(i<0) i=0; //TODO: did it okay
                    positionInPatternMassive = 0;
                }
            }
        }
        //if (positionInPatternMassive != 0){
        ArrayList<String> position = new ArrayList<>();
        position.add(Integer.toString(positionInPatternMassive));
        position.add(Integer.toString(0)); //debug
        output.add(position);
        //}
        return output;
    }

    public String cachingMatch(char[] massiveWithMatch, int position, int patternLength, int offset) {
        String output = "";
        int startIndex = position - offset;
        int endIndex = position + patternLength + offset;
        //char[] outputArray = new Arrays.copyOfRange(massiveWithMatch, startIndex, endIndex);
        if (startIndex < 0) startIndex = 0;
        if (endIndex > massiveWithMatch.length) endIndex = massiveWithMatch.length - 1;

        char[] outputArray = Arrays.copyOfRange(massiveWithMatch, startIndex, endIndex);
        //System.out.println(outputArray);
        //String.copyValueOf();
        //char[] outputArray = new Arrays.copyOfRange();
        return String.valueOf(outputArray);
    }

    public ArrayList<ArrayList> fileParcer(File fileToParce, String pattern, int bufferLength) throws IOException{

        BufferedReader b = new BufferedReader(new FileReader(fileToParce));
        String readedMassive = "";
        BigInteger position = new BigInteger("0");
        ArrayList<ArrayList> output = new ArrayList<>();
        BigInteger positionIncrement = new BigInteger(Integer.toString(bufferLength));
        int initialPosition = 0;

        while (true) {
            readedMassive = this.readFile(b, bufferLength);

            if (readedMassive == ""){break;}

            output.addAll(this.parceFile(readedMassive.toCharArray(), pattern.toCharArray(),
                    initialPosition, position));

            String stringPosition = output.get(output.size()-1).get(0).toString();
            output.remove(output.size()-1);
            // stringPosition = ;
            //System.out.println(stringPosition);

            initialPosition = Integer.valueOf(stringPosition);

            position = position.add(positionIncrement);
        }
//
//        for (ArrayList n : output) {
//            ArrayList<String> tmp = n;
//            String one = tmp.get(0);
//            String two = tmp.get(1);
//            System.out.println(one + ", " + two + ", ");
//        }
        return output;
    }

    public static void main(String[] args) {

        FileFinder test = new FileFinder();
//
//        try {ArrayList<Path> pathsList = test.recursiveWalkFiles("D:\\FhtagnEast\\Downloads\\indiv18\\by_date\\itcont_2018_20181111_52010302.txt",
//                ".txt");

        try {ArrayList<Path> pathsList = test.recursiveWalkFiles("D:\\FhtagnEast\\Downloads\\indiv18\\test.txt",
                ".txt");


//        try {ArrayList<Path> pathsList = test.recursiveWalkFiles("D:\\",
//                ".txt");


        //System.out.println(pathsList.size());
//            Path pathToFile = pathsList.get(0);
            final File fileToParce = new File(pathsList.get(0).toString());
            ArrayList<ArrayList> output = test.fileParcer(fileToParce, "slslslava", 14);
//            BufferedReader b = new BufferedReader(new FileReader(fileToParce));
//            String readedMassive = "";
//            BigInteger position = new BigInteger("0");
//            ArrayList<ArrayList> output = new ArrayList<>();
//            int length = 100000;
//            BigInteger positionIncrement = new BigInteger(Integer.toString(length));
//
//            while (true) {
//                readedMassive = test.readFile(b, length);
//
//                if (readedMassive == ""){break;}
//
//
//                output.addAll(test.parceFile(readedMassive.toCharArray(), "DONNA".toCharArray(),
//                        0, position));
//
//                //position.add(new BigInteger(Integer.toString(length)));
//                position = position.add(positionIncrement);
//                //System.out.println(position);
                //System.out.println(readedMassive);
//                for (BigInteger n : output) {
//                    System.out.print(n.toString()+ ", ");
//                }
//            }

            for (ArrayList n : output) {
                ArrayList<String> tmp = n;
                String one = tmp.get(0);
                String two = tmp.get(1);
                System.out.println(one + ", " + two + ", ");
            }
            //for (char c : readedMassive.toCharArray()) {
                //String output = test.readFile(b, 100);

            //}

            //System.out.println(pathsList.get(0).toString());
        }
        catch (IOException e) {
            System.out.println("IO error");
        }





//        File[] dirList = test.recursivelyGetFiles(new File("C:\\"), ".jpg");
//        //File[] dirList = test.getFiles("D:\\FhtagnEast\\Downloads", ".jpg");
////        for (File f : dirList){
////                System.out.println(f.toString());
////        }
////        System.out.println("");
//
//        ArrayList<File> dirListCln = test.cleanDirectories(dirList);
//        for (File f : dirListCln) {
//            System.out.println(f.toString());
//        }
//
//        System.out.println(dirListCln.size());



    }

}
