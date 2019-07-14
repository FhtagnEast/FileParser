package com.gmail.fhtagneast;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GraphicInterface {

    private static DefaultListModel<String> filesModel;
    private static DefaultListModel<String> matchesModel;

    public GraphicInterface(String title) {

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        filesModel = new DefaultListModel<>();

        JList<String> northList = new JList<>(filesModel);
        northList.setLayoutOrientation(JList.VERTICAL);
        northList.setVisibleRowCount(0);

        JScrollPane filesPanel = new JScrollPane(northList);
        filesPanel.setPreferredSize(new Dimension(100, 100));

        matchesModel = new DefaultListModel<>();
        JList<String> centerList = new JList<>(matchesModel);
        centerList.setLayoutOrientation(JList.VERTICAL);
        centerList.setVisibleRowCount(0);

        JScrollPane centerScroll = new JScrollPane(centerList);
        centerScroll.setPreferredSize(new Dimension(100, 100));

        mainPanel.add(filesPanel);
        mainPanel.add(centerScroll);

        frame.getContentPane().add(mainPanel);

        frame.setPreferredSize(new Dimension(330, 450));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        start1();
        start2();

    }

    private void start1() {
        SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                for (int i = 0; i <= 100; i++) {
                    Thread.sleep(100);
                    publish(i);
                }

                return true;
            }


            @Override
            protected void process(List<Integer> chunks) {
                int mostRecentValue = chunks.get(chunks.size()-1);
                filesModel.add(0, Integer.toString(chunks.get(chunks.size()-1)));
            }


        };

        worker.execute();

    }

    private void start2() {
        SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
            @Override
            protected Boolean doInBackground() {

//                new MatchFinder(500000,
//                        10,
//                        queue,
//                        "JOJO".toCharArray());
                return true;
            }


            @Override
            protected void process(List<Integer> chunks) {
                int mostRecentValue = chunks.get(chunks.size()-1);
                matchesModel.add(0, Integer.toString(chunks.get(chunks.size()-1)));
            }


        };

        worker.execute();

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame.setDefaultLookAndFeelDecorated(true);
                new GraphicInterface("FileFinder");
            }

        });

    }

}
