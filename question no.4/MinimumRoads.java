import java.util.*;

public class MinimumRoads {

    // Class to represent a state in the BFS search
    static class State {
        int node;   // Current node
        int mask;   // Bitmask representing collected packages
        int steps;  // Number of steps taken

        public State(int node, int mask, int steps) {
            this.node = node;
            this.mask = mask;
            this.steps = steps;
        }
    }

    /**
     * Finds the minimum number of roads needed to collect all packages and return to the starting point.
     *
     * @param packages Array indicating locations with packages (1 for package present, 0 otherwise).
     * @param roads    2D array representing bidirectional roads between nodes.
     * @return Minimum number of roads required to collect all packages and return to start, or -1 if not possible.
     */
    public static int minRoads(int[] packages, int[][] roads) {
        int n = packages.length;
        if (n == 0) return 0; // No nodes, no roads needed

        // Identify all nodes containing packages
        List<Integer> packageNodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (packages[i] == 1) {
                packageNodes.add(i);
            }
        }
        int m = packageNodes.size();
        if (m == 0) return 0; // No packages, no roads needed

        // Build adjacency list representation of the graph
        List<List<Integer>> adj = buildAdjacencyList(n, roads);

        // Precompute which packages can be covered within two steps from each node
        int[] coverage = precomputeCoverage(n, adj, packageNodes);

        // Precompute shortest paths between all nodes
        int[][] shortestPaths = precomputeShortestPaths(n, adj);

        int minTotal = Integer.MAX_VALUE;

        // Try starting from each node
        for (int start = 0; start < n; start++) {
            int initialMask = coverage[start];

            // If the starting node already covers all packages, return 0 steps
            if (initialMask == (1 << m) - 1) {
                return 0;
            }

            // BFS to explore paths and collect all packages
            Queue<State> queue = new LinkedList<>();
            boolean[][] visited = new boolean[n][1 << m];
            queue.add(new State(start, initialMask, 0));
            visited[start][initialMask] = true;

            int currentMin = Integer.MAX_VALUE;

            while (!queue.isEmpty()) {
                State curr = queue.poll();

                // If all packages are collected, check return path to start node
                if (curr.mask == (1 << m) - 1) {
                    int returnSteps = shortestPaths[curr.node][start];
                    if (returnSteps != -1) {
                        currentMin = Math.min(currentMin, curr.steps + returnSteps);
                    }
                    continue;
                }

                // Explore all neighboring nodes
                for (int neighbor : adj.get(curr.node)) {
                    int newMask = curr.mask | coverage[neighbor];
                    int newSteps = curr.steps + 1;
                    if (!visited[neighbor][newMask]) {
                        visited[neighbor][newMask] = true;
                        queue.add(new State(neighbor, newMask, newSteps));
                    }
                }
            }

            if (currentMin < minTotal) {
                minTotal = currentMin;
            }
        }

        return minTotal == Integer.MAX_VALUE ? -1 : minTotal;
    }

    /**
     * Builds an adjacency list representation of the graph.
     *
     * @param n     Number of nodes.
     * @param roads Array of roads connecting nodes.
     * @return Adjacency list representing the graph.
     */
    private static List<List<Integer>> buildAdjacencyList(int n, int[][] roads) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
        for (int[] road : roads) {
            int a = road[0];
            int b = road[1];
            adj.get(a).add(b);
            adj.get(b).add(a);
        }
        return adj;
    }

    /**
     * Precomputes the package coverage for each node within two steps.
     *
     * @param n            Number of nodes.
     * @param adj          Adjacency list representation of the graph.
     * @param packageNodes List of nodes containing packages.
     * @return An array where each index represents the bitmask of packages a node can reach within two steps.
     */
    private static int[] precomputeCoverage(int n, List<List<Integer>> adj, List<Integer> packageNodes) {
        int m = packageNodes.size();
        int[] coverage = new int[n];

        for (int u = 0; u < n; u++) {
            Set<Integer> covered = getNodesWithinTwoSteps(u, adj);
            int mask = 0;
            for (int i = 0; i < m; i++) {
                if (covered.contains(packageNodes.get(i))) {
                    mask |= (1 << i);
                }
            }
            coverage[u] = mask;
        }
        return coverage;
    }

    /**
     * Gets all nodes that can be reached within two steps from a given node.
     *
     * @param u   Starting node.
     * @param adj Adjacency list representation of the graph.
     * @return A set of nodes reachable within two steps.
     */
    private static Set<Integer> getNodesWithinTwoSteps(int u, List<List<Integer>> adj) {
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(u);
        visited.add(u);
        int steps = 0;

        while (steps < 2 && !queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int node = queue.poll();
                for (int neighbor : adj.get(node)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            steps++;
        }

        return visited;
    }

    /**
     * Computes the shortest paths between all pairs of nodes using BFS.
     *
     * @param n   Number of nodes.
     * @param adj Adjacency list representation of the graph.
     * @return A 2D array where dist[i][j] gives the shortest distance between nodes i and j.
     */
    private static int[][] precomputeShortestPaths(int n, List<List<Integer>> adj) {
        int[][] dist = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            Arrays.fill(dist[i], -1);
            Queue<Integer> q = new LinkedList<>();
            q.add(i);
            dist[i][i] = 0;
            
            while (!q.isEmpty()) {
                int u = q.poll();
                for (int v : adj.get(u)) {
                    if (dist[i][v] == -1) {
                        dist[i][v] = dist[i][u] + 1;
                        q.add(v);
                    }
                }
            }
        }
        return dist;
    }

    public static void main(String[] args) {
        // Example test case
        int[] packages = {0,0,0,1,1,0,0,1}; 
        int[][] roads = {{0, 1}, {0, 2}, {1, 3}, {1, 4}, {2, 5}, {5,6}, {5,7}};
        
        // Output the minimum roads required
        System.out.println(minRoads(packages, roads)); 
    }
}
