package newpackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GraphVisualizer extends JFrame {
    private GraphPanel graphPanel;
    private JTextField edgeStartField;
    private JTextField edgeEndField;
    private JButton addEdgeButton;
    private JCheckBoxMenuItem enableAddNodesMenuItem;
    private JTextField startNodeField;
    private JTextField endNodeField;
    private JButton findPathButton;
    private JMenuItem undoMenuItem;
    private JMenuItem redoMenuItem;
    private JComboBox<String> algorithmComboBox;

    public GraphVisualizer() {
        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER); // Add graphPanel to the center

        setTitle("Graph Visualizer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openBackgroundMenuItem = new JMenuItem("Open Background Image");
        fileMenu.add(openBackgroundMenuItem);
        openBackgroundMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(GraphVisualizer.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        BufferedImage backgroundImage = ImageIO.read(selectedFile);
                        graphPanel.setBackgroundImage(backgroundImage);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(GraphVisualizer.this, "Error loading image: " + ex.getMessage());
                    }
                }
            }
        });

        JMenuItem clearMenuItem = new JMenuItem("Clear");
        fileMenu.add(clearMenuItem);
        clearMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphPanel.clear();
            }
        });

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);

        enableAddNodesMenuItem = new JCheckBoxMenuItem("Enable Add Nodes");
        editMenu.add(enableAddNodesMenuItem);
        enableAddNodesMenuItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                graphPanel.setAddNodesEnabled(enableAddNodesMenuItem.isSelected());
            }
        });

        undoMenuItem = new JMenuItem("Undo");
        editMenu.add(undoMenuItem);
        undoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphPanel.undo();
            }
        });

        redoMenuItem = new JMenuItem("Redo");
        editMenu.add(redoMenuItem);
        redoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphPanel.redo();
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT)); // Align components to the right
        
        algorithmComboBox = new JComboBox<>(new String[]{"DFS", "Dijkstra"});
        controlPanel.add(new JLabel("Algorithm:"));
        controlPanel.add(algorithmComboBox);
        
        edgeStartField = new JTextField(5);
        edgeEndField = new JTextField(5);
        addEdgeButton = new JButton("Add Edge");

        controlPanel.add(new JLabel("Edge from:"));
        controlPanel.add(edgeStartField);
        controlPanel.add(new JLabel("Edge to:"));
        controlPanel.add(edgeEndField);
        controlPanel.add(addEdgeButton);

        addEdgeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int fromIndex = Integer.parseInt(edgeStartField.getText()) - 1;
                    int toIndex = Integer.parseInt(edgeEndField.getText()) - 1;
                    graphPanel.addEdgeByIndices(fromIndex, toIndex);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GraphVisualizer.this, "Please enter valid node indices.");
                }
            }
        });

        startNodeField = new JTextField(5);
        endNodeField = new JTextField(5);
        findPathButton = new JButton("Find Path");

        controlPanel.add(new JLabel("Start Node:"));
        controlPanel.add(startNodeField);
        controlPanel.add(new JLabel("End Node:"));
        controlPanel.add(endNodeField);
        controlPanel.add(findPathButton);

        findPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int startNodeIndex = Integer.parseInt(startNodeField.getText()) - 1;
                    int endNodeIndex = Integer.parseInt(endNodeField.getText()) - 1;
                    if ("DFS".equals(algorithmComboBox.getSelectedItem())) {
                        graphPanel.findAndSetPathDFS(startNodeIndex, endNodeIndex);
                    } else if ("Dijkstra".equals(algorithmComboBox.getSelectedItem())) {
                        graphPanel.findAndSetPathDijkstra(startNodeIndex, endNodeIndex);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GraphVisualizer.this, "Please enter valid node indices.");
                }
            }
        });

        controlPanel.add(new JLabel("Algorithm:"));
        controlPanel.add(algorithmComboBox);
        add(controlPanel, BorderLayout.SOUTH); // Add controlPanel to the bottom

        pack(); // Adjust frame size to fit contents
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GraphVisualizer().setVisible(true);
            }
        });
    }
}