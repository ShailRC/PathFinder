package newpackage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import javax.swing.*;

public class GraphPanel extends JPanel {
    private List<Node> nodes;
    private List<Edge> edges;
    private BufferedImage backgroundImage;
    private boolean addNodesEnabled;
    private javax.swing.Timer traversalTimer;
    private Point traversalPoint;
    private List<Integer> traversalPath;
    private int traversalIndex;
    private double traversalProgress;
    private Stack<Action> undoStack;
    private Stack<Action> redoStack;

    public GraphPanel() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.traversalPoint = null;
        this.traversalPath = new ArrayList<>();
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        setPreferredSize(new Dimension(600, 400));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (addNodesEnabled) {
                    Node newNode = new Node(e.getX(), e.getY(), String.valueOf(nodes.size() + 1));
                    addNode(newNode);
                    undoStack.push(new AddNodeAction(newNode));
                    redoStack.clear();
                    repaint();
                }
            }
        });
    }

    public void startTraversal() {
        if (traversalPath.size() < 2) {
            JOptionPane.showMessageDialog(null, "Traversal path must contain at least two nodes.");
            return;
        }

        traversalIndex = 0;
        traversalProgress = 0.0;
        traversalPoint = null;

        if (traversalTimer != null && traversalTimer.isRunning()) {
            traversalTimer.stop();
        }

        traversalTimer = new javax.swing.Timer(30, e -> {
            traversalProgress += 0.02;
            if (traversalProgress >= 1.0) {
                traversalProgress = 0.0;
                traversalIndex = (traversalIndex + 1) % (traversalPath.size() - 1);
            }

            int fromIndex = traversalPath.get(traversalIndex);
            int toIndex = traversalPath.get(traversalIndex + 1);
            if (isEdgeExist(fromIndex, toIndex)) {
                Node traversalStart = nodes.get(fromIndex);
                Node traversalEnd = nodes.get(toIndex);

                int x = (int) (traversalStart.x + traversalProgress * (traversalEnd.x - traversalStart.x));
                int y = (int) (traversalStart.y + traversalProgress * (traversalEnd.y - traversalStart.y));
                traversalPoint = new Point(x, y);
                repaint(); // Repaint the panel to show updated traversal point
            } else {
                traversalTimer.stop();
                JOptionPane.showMessageDialog(null, "Edge does not exist between selected nodes.");
            }
        });

        traversalTimer.start();
    }

    private boolean isEdgeExist(int fromIndex, int toIndex) {
        for (Edge edge : edges) {
            if ((edge.from == nodes.get(fromIndex) && edge.to == nodes.get(toIndex)) ||
                    (edge.from == nodes.get(toIndex) && edge.to == nodes.get(fromIndex))) {
                return true;
            }
        }
        return false;
    }

    public void addNode(Node node) {
        nodes.add(node);
        repaint();
    }

    public void removeNode(Node node) {
        nodes.remove(node);
        repaint();
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
        repaint();
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
        repaint();
    }

    public void addEdgeByIndices(int fromIndex, int toIndex) {
        if (fromIndex >= 0 && fromIndex < nodes.size() && toIndex >= 0 && toIndex < nodes.size()) {
            Edge newEdge = new Edge(nodes.get(fromIndex), nodes.get(toIndex));
            addEdge(newEdge);
            undoStack.push(new AddEdgeAction(newEdge));
            redoStack.clear();
        } else {
            JOptionPane.showMessageDialog(null, "Node indices out of bounds.");
        }
    }

    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
        repaint();
    }

    public void setAddNodesEnabled(boolean enabled) {
        this.addNodesEnabled = enabled;
    }

    public void clear() {
        nodes.clear();
        edges.clear();
        backgroundImage = null;
        traversalPoint = null;
        traversalPath.clear();
        traversalProgress = 0.0;
        if (traversalTimer != null && traversalTimer.isRunning()) {
            traversalTimer.stop();
        }
        undoStack.clear();
        redoStack.clear();
        repaint();
    }

    // Action interface and concrete actions for undo/redo
    private interface Action {
        void undo();
        void redo();
    }

    private class AddNodeAction implements Action {
        private Node node;

        AddNodeAction(Node node) {
            this.node = node;
        }

        @Override
        public void undo() {
            removeNode(node);
        }

        @Override
        public void redo() {
            addNode(node);
        }
    }

    private class AddEdgeAction implements Action {
        private Edge edge;

        AddEdgeAction(Edge edge) {
            this.edge = edge;
        }

        @Override
        public void undo() {
            removeEdge(edge);
        }

        @Override
        public void redo() {
            addEdge(edge);
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Action action = undoStack.pop();
            action.undo();
            redoStack.push(action);
            repaint();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Action action = redoStack.pop();
            action.redo();
            undoStack.push(action);
            repaint();
        }
    }

    public void findAndSetPathDFS(int startNodeIndex, int endNodeIndex) {
        if (startNodeIndex < 0 || startNodeIndex >= nodes.size() || endNodeIndex < 0 || endNodeIndex >= nodes.size()) {
            JOptionPane.showMessageDialog(null, "Invalid node indices.");
            return;
        }
    
        Node startNode = nodes.get(startNodeIndex);
        Node endNode = nodes.get(endNodeIndex);
    
        boolean[] visited = new boolean[nodes.size()];
        List<Integer> path = new ArrayList<>();
        Stack<Integer> stack = new Stack<>();
    
        stack.push(startNodeIndex);
        visited[startNodeIndex] = true;
    
        boolean found = false;
        outer:
        while (!stack.isEmpty()) {
            int currentNodeIndex = stack.peek();
    
            if (currentNodeIndex == endNodeIndex) {
                found = true;
                break;
            }
    
            List<Integer> neighbors = getNeighbors(currentNodeIndex);
            boolean allVisited = true;
    
            for (int neighbor : neighbors) {
                if (!visited[neighbor]) {
                    stack.push(neighbor);
                    visited[neighbor] = true;
                    path.add(neighbor);
                    allVisited = false;
                    break;
                }
            }
    
            if (allVisited) {
                stack.pop();
            }
        }
    
        if (found) {
            path.add(0, startNodeIndex);
            setTraversalPath(path);
        } else {
            JOptionPane.showMessageDialog(null, "Path not found.");
        }
    }
    
    private List<Integer> getNeighbors(int nodeIndex) {
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            if (edge.from == nodes.get(nodeIndex)) {
                neighbors.add(nodes.indexOf(edge.to));
            } else if (edge.to == nodes.get(nodeIndex)) {
                neighbors.add(nodes.indexOf(edge.from));
            }
        }
        return neighbors;
    }
    public void findAndSetPathDijkstra(int startNodeIndex, int endNodeIndex) {
    if (startNodeIndex < 0 || startNodeIndex >= nodes.size() || endNodeIndex < 0 || endNodeIndex >= nodes.size()) {
        JOptionPane.showMessageDialog(null, "Invalid node indices.");
        return;
    }

    Node startNode = nodes.get(startNodeIndex);
    Node endNode = nodes.get(endNodeIndex);

    // Initialize distances and predecessors
    double[] distances = new double[nodes.size()];
    int[] predecessors = new int[nodes.size()];
    PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(n -> distances[nodes.indexOf(n)]));

    Arrays.fill(distances, Double.POSITIVE_INFINITY);
    distances[startNodeIndex] = 0;
    predecessors[startNodeIndex] = -1;
    priorityQueue.add(startNode);

    while (!priorityQueue.isEmpty()) {
        Node currentNode = priorityQueue.poll();
        int currentNodeIndex = nodes.indexOf(currentNode);

        if (currentNodeIndex == endNodeIndex) {
            break; // Found the shortest path to the end node
        }

        List<Integer> neighbors = getNeighbors(currentNodeIndex);

        for (int neighborIndex : neighbors) {
            Node neighborNode = nodes.get(neighborIndex);
            double weight = 1; // Assuming all edges have weight = 1

            if (distances[currentNodeIndex] + weight < distances[neighborIndex]) {
                distances[neighborIndex] = distances[currentNodeIndex] + weight;
                predecessors[neighborIndex] = currentNodeIndex;
                priorityQueue.add(neighborNode);
            }
        }
    }

    // Reconstruct the shortest path
    List<Integer> path = new ArrayList<>();
    for (int at = endNodeIndex; at != -1; at = predecessors[at]) {
        path.add(at);
    }
    Collections.reverse(path);

    if (!path.isEmpty() && path.get(0) == startNodeIndex) {
        setTraversalPath(path);
    } else {
        JOptionPane.showMessageDialog(null, "Path not found.");
    }
}


    private void setTraversalPath(List<Integer> path) {
        this.traversalPath = path;
        startTraversal(); // Start traversal animation
    }

    private static class Node {
        int x, y;
        String label;

        Node(int x, int y, String label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }
    }

    private static class Edge {
        Node from, to;

        Edge(Node from, Node to) {
            this.from = from;
            this.to = to;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        for (Edge edge : edges) {
            g2d.drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y);
        }

        for (Node node : nodes) {
            g2d.fillOval(node.x - 5, node.y - 5, 10, 10);
            g2d.drawString(node.label, node.x + 5, node.y - 5);
        }

        if (traversalPoint != null) {
            g2d.setColor(Color.RED);
            g2d.fillOval(traversalPoint.x - 5, traversalPoint.y - 5, 10, 10);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graph Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new GraphPanel());
            frame.pack();
            frame.setVisible(true);
        });
    }
}