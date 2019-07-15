package com.gmail.fhtagneast;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GraphicInterface {

    private static DefaultListModel<String> filesModel;
    private static DefaultListModel<String> matchesModel;
    private static MatchFinder matchFinder;
    private static boolean searchPerformed = false;
    private static String searchDirectory ;
    private static String matchPattern ;
    private static String extensionOfSearchedFiles ;

    private GraphicInterface(String title) {

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel searchResultsPanel = new JPanel();
        searchResultsPanel.setLayout(new BoxLayout(searchResultsPanel, BoxLayout.X_AXIS));

        JPanel inputFieldsPanel = new JPanel();
        inputFieldsPanel.setLayout(new BoxLayout(inputFieldsPanel, BoxLayout.X_AXIS));




        filesModel = new DefaultListModel<>();

        JList<String> filesList = new JList<>(filesModel);
        filesList.setLayoutOrientation(JList.VERTICAL);
        filesList.setVisibleRowCount(0);
        filesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (!searchPerformed) {
                    loadMatches(filesList.getSelectedValue());
                }
            }
        });

        JScrollPane filesPanel = new JScrollPane(filesList);
        filesPanel.setPreferredSize(new Dimension(100, 100));

        matchesModel = new DefaultListModel<>();
        JList<String> centerList = new JList<>(matchesModel);
        centerList.setLayoutOrientation(JList.VERTICAL);
        centerList.setVisibleRowCount(0);

        JScrollPane centerScroll = new JScrollPane(centerList);
        centerScroll.setPreferredSize(new Dimension(100, 100));

        JTextField inputDirectory = new JTextField();
        inputDirectory.setToolTipText("Input directory here");
        JTextField inputExtension = new JTextField(".log");
        inputExtension.setToolTipText("Input extension here");
        JTextField inputPattern = new JTextField();
        inputPattern.setToolTipText("Input pattern here");

        JButton startSearchButton = new JButton("Start Search");
        startSearchButton.addActionListener(e -> {
            extensionOfSearchedFiles = inputExtension.getText();
            searchDirectory = inputDirectory.getText();
            matchPattern = inputPattern.getText();
            findFiles();
        });

        searchResultsPanel.add(filesPanel);
        searchResultsPanel.add(centerScroll);
        inputFieldsPanel.add(inputDirectory);
        inputFieldsPanel.add(inputExtension);
        inputFieldsPanel.add(inputPattern);
        inputFieldsPanel.add(startSearchButton);



        mainPanel.add(inputFieldsPanel, BorderLayout.SOUTH);
        mainPanel.add(searchResultsPanel, BorderLayout.CENTER);
        frame.getContentPane().add(mainPanel);

        frame.setPreferredSize(new Dimension(1000, 500));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }


    private void loadMatches(String stringPath){
        SwingWorker<Boolean, ArrayList<TextMatchWithCachedSurround>> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {


                Path path = Paths.get(stringPath);
                ArrayList<TextMatchWithCachedSurround> matches = matchFinder.getCachedSurroundForPath(path);
                publish(matches);

                return true;
            }



            @Override
            protected void process(List<ArrayList<TextMatchWithCachedSurround>> chunks) {
                ArrayList<TextMatchWithCachedSurround> loadedListOfMatches = chunks.get(chunks.size() - 1);
                matchesModel.clear();
                for (TextMatchWithCachedSurround match : loadedListOfMatches) {

                    matchesModel.addElement(match.toString());
                }

            }


        };

        worker.execute();
    }

    private void findFiles() {
        SwingWorker<Boolean, Path> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {

                final FileFinder fileFinder = new FileFinder(searchDirectory,
                        extensionOfSearchedFiles);
                System.out.println("ok");
                ArrayList<Path> listOfPaths = fileFinder.findFiles();
                System.out.println(listOfPaths.size());


                for (Path path : listOfPaths) {
                    publish(path);

                }
                findMatches(listOfPaths);

                return true;
            }


            @Override
            protected void process(List<Path> chunks) {
                filesModel.clear();


                for (Path chunk : chunks) {
                    filesModel.addElement(chunk.toAbsolutePath().toString());
                }

            }


        };

        worker.execute();

    }

    private void findMatches(ArrayList<Path> pathList) {
        SwingWorker<Boolean, ArrayList<TextMatchWithCachedSurround>> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                searchPerformed = true;
                matchFinder = new MatchFinder(5000000,
                        10,
                        matchPattern.toCharArray());

                for (Path path : pathList) {
                    try {
                        ArrayList<TextMatchWithCachedSurround> matchWithSurround = matchFinder.parseFile(path);
                        publish(matchWithSurround);
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                }
                return true;
            }

            protected void done() {

                searchPerformed = false;

            }

            @Override
            protected void process(List<ArrayList<TextMatchWithCachedSurround>> chunks) {
                ArrayList<TextMatchWithCachedSurround> listOfMatches = chunks.get(chunks.size() - 1);
                matchesModel.clear();

                for (TextMatchWithCachedSurround match : listOfMatches) {
                    matchesModel.addElement(match.toString());
                }

            }


        };

        worker.execute();

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            new GraphicInterface("FileFinder");
        });

    }

}
