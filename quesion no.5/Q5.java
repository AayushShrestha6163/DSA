import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

class Q5 extends JFrame {

    /*
     * Suppose you are hired as software developer for certain organization and you
     * are tasked with creating a
     * GUI application that helps network administrators design a network topology
     * that is both cost-effective
     * and efficient for data transmission. The application needs to visually
     * represent servers and clients as
     * nodes in a graph, with potential network connections between them, each
     * having associated costs and
     * bandwidths. The goal is to enable the user to find a network topology that
     * minimizes both the total cost
     * and the latency of data transmission.
     */

    /*
     * Approach:
     * Step 1: Initialize GUI Components
     * - Create a JFrame with JTextFields for input, JTextArea for output, and
     * JButtons for user actions.
     * - Implement a JPanel (GraphPanel) to visualize the network topology.
     * 
     * Step 2: Implement Graph Data Structure
     * - Use an adjacency list (HashMap<String, List<Edge>>) to store nodes and
     * connections.
     * - Define an Edge class to represent connections with cost and bandwidth.
     * 
     * Step 3: Add Network Connections
     * - When a user inputs two nodes with cost and bandwidth, store them in the
     * adjacency list.
     * - Update the graphical representation by repainting GraphPanel.
     * 
     * Step 4: Optimize Network (Minimum Spanning Tree - MST)
     * - Use Kruskal’s Algorithm with a Union-Find data structure to find the MST.
     * - Sort edges by cost and iteratively add edges while ensuring no cycles.
     * 
     * Step 5: Find Shortest Path
     * - Use Dijkstra’s Algorithm with a priority queue to find the shortest path.
     * - Track distances from the start node to all other nodes.
     * 
     * Step 6: Draw Network Topology
     * - Assign positions to nodes and use Graphics to draw circles (nodes) and
     * lines (edges).
     * - Display edge costs at their midpoints.
     * 
     * Step 7: Handle User Interaction
     * - On "Add Connection" click: Update adjacency list and repaint the graph.
     * - On "Optimize Network" click: Compute MST and display results.
     * - On "Find Shortest Path" click: Execute Dijkstra’s Algorithm and display the
     * shortest path.
     * 
     * Step 8: Run the Program
     * - Use SwingUtilities.invokeLater to launch the GUI.
     */

    private Graph graph;
    private JTextArea outputArea;
    private JTextField nodeField1, nodeField2, costField, bandwidthField;
    private GraphPanel graphPanel; // Panel to display the graph

    public Q5() {
        setTitle("Network Optimization");
        setSize(800, 600); // Increased window size for graph visibility
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        graph = new Graph();
        outputArea = new JTextArea(10, 50);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel controlPanel = new JPanel();
        nodeField1 = new JTextField(5);
        nodeField2 = new JTextField(5);
        costField = new JTextField(5);
        bandwidthField = new JTextField(5);
        JButton addEdgeButton = new JButton("Add Connection");
        JButton optimizeButton = new JButton("Optimize Network");
        JButton shortestPathButton = new JButton("Find Shortest Path");

        controlPanel.add(new JLabel("Node 1:"));
        controlPanel.add(nodeField1);
        controlPanel.add(new JLabel("Node 2:"));
        controlPanel.add(nodeField2);
        controlPanel.add(new JLabel("Cost:"));
        controlPanel.add(costField);
        controlPanel.add(new JLabel("Bandwidth:"));
        controlPanel.add(bandwidthField);
        controlPanel.add(addEdgeButton);
        controlPanel.add(optimizeButton);
        controlPanel.add(shortestPathButton);

        // Add the graph panel to the JFrame
        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER); // Graph panel for visualization
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.SOUTH);

        addEdgeButton.addActionListener(e -> addEdge());
        optimizeButton.addActionListener(e -> optimizeNetwork());
        shortestPathButton.addActionListener(e -> findShortestPath());
    }

    private void addEdge() {
        String node1 = nodeField1.getText();
        String node2 = nodeField2.getText();
        int cost = Integer.parseInt(costField.getText());
        int bandwidth = Integer.parseInt(bandwidthField.getText());
        graph.addEdge(node1, node2, cost, bandwidth);
        outputArea.append(
                "Added connection: " + node1 + " - " + node2 + " (Cost: " + cost + ", Bandwidth: " + bandwidth + ")\n");

        // Repaint the graph after adding an edge
        graphPanel.repaint();
    }

    private void optimizeNetwork() {
        List<Edge> mst = graph.findMinimumSpanningTree();
        outputArea.append("\nOptimized Network (Minimum Cost Spanning Tree):\n");
        for (Edge edge : mst) {
            outputArea.append(edge.node1 + " - " + edge.node2 + " (Cost: " + edge.cost + ")\n");
        }

        // Repaint the graph after optimizing the network
        graphPanel.repaint();
    }

    private void findShortestPath() {
        String start = nodeField1.getText();
        String end = nodeField2.getText();
        int distance = graph.findShortestPath(start, end);
        outputArea.append("\nShortest path from " + start + " to " + end + " is " + distance + " units.\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Q5().setVisible(true));
    }

    // Custom JPanel for drawing the graph
    class GraphPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Set graphics properties
            g.setColor(Color.BLACK);
            // Call the graph's drawing method to visualize nodes and edges
            graph.drawGraph(g);
        }
    }
}

class Graph {
    private Map<String, List<Edge>> adjList = new HashMap<>();

    public void addEdge(String node1, String node2, int cost, int bandwidth) {
        adjList.putIfAbsent(node1, new ArrayList<>());
        adjList.putIfAbsent(node2, new ArrayList<>());
        adjList.get(node1).add(new Edge(node1, node2, cost, bandwidth));
        adjList.get(node2).add(new Edge(node2, node1, cost, bandwidth));
    }

    public List<Edge> findMinimumSpanningTree() {
        List<Edge> edges = new ArrayList<>();
        for (List<Edge> list : adjList.values()) {
            edges.addAll(list);
        }
        edges.sort(Comparator.comparingInt(e -> e.cost));

        List<Edge> mst = new ArrayList<>();
        UnionFind uf = new UnionFind(adjList.keySet());
        for (Edge edge : edges) {
            if (uf.union(edge.node1, edge.node2)) {
                mst.add(edge);
            }
        }
        return mst;
    }

    public int findShortestPath(String start, String end) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
        Map<String, Integer> distances = new HashMap<>();
        Set<String> visited = new HashSet<>();

        distances.put(start, 0);
        pq.add(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (!visited.add(current.name))
                continue;
            if (current.name.equals(end))
                return current.cost;

            for (Edge neighbor : adjList.getOrDefault(current.name, Collections.emptyList())) {
                int newDist = current.cost + neighbor.cost;
                if (newDist < distances.getOrDefault(neighbor.node2, Integer.MAX_VALUE)) {
                    distances.put(neighbor.node2, newDist);
                    pq.add(new Node(neighbor.node2, newDist));
                }
            }
        }
        return -1; // No path found
    }

    // Method to draw the graph (nodes and edges) on the panel
    public void drawGraph(Graphics g) {
        int nodeRadius = 20;
        Map<String, Point> nodePositions = new HashMap<>();
        int x = 100, y = 100; // Start position for drawing nodes

        // Example: Draw nodes and edges
        for (String node : adjList.keySet()) {
            nodePositions.put(node, new Point(x, y));
            g.setColor(Color.BLUE);
            g.fillOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2); // Draw node as a circle
            g.setColor(Color.BLACK);
            g.drawString(node, x - nodeRadius / 2, y - nodeRadius); // Draw node name

            x += 150; // Space out nodes horizontally
            if (x > 500) { // Move to next row if x position exceeds the width
                x = 100;
                y += 150;
            }
        }

        // Draw edges (connections between nodes)
        for (String node : adjList.keySet()) {
            Point node1Pos = nodePositions.get(node);
            for (Edge edge : adjList.get(node)) {
                Point node2Pos = nodePositions.get(edge.node2);
                g.setColor(Color.RED);
                g.drawLine(node1Pos.x, node1Pos.y, node2Pos.x, node2Pos.y); // Draw line for the edge
                g.setColor(Color.BLACK);
                g.drawString(edge.cost + "", (node1Pos.x + node2Pos.x) / 2, (node1Pos.y + node2Pos.y) / 2); // Draw cost
                                                                                                            // label
            }
        }
    }
}

class Edge {
    String node1, node2;
    int cost, bandwidth;

    public Edge(String node1, String node2, int cost, int bandwidth) {
        this.node1 = node1;
        this.node2 = node2;
        this.cost = cost;
        this.bandwidth = bandwidth;
    }
}

class Node {
    String name;
    int cost;

    public Node(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }
}

class UnionFind {
    private Map<String, String> parent = new HashMap<>();

    public UnionFind(Set<String> nodes) {
        for (String node : nodes)
            parent.put(node, node);
    }

    public String find(String node) {
        if (!parent.get(node).equals(node)) {
            parent.put(node, find(parent.get(node)));
        }
        return parent.get(node);
    }

    public boolean union(String node1, String node2) {
        String root1 = find(node1);
        String root2 = find(node2);
        if (!root1.equals(root2)) {
            parent.put(root1, root2);
            return true;
        }
        return false;
    }
}