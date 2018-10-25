package uk.ac.cam.cl.ks828.exercises.tick12;

import uk.ac.cam.cl.mlrd.exercises.networks.IExercise12;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by KSarm on 08/03/2018.
 */
public class Exercise12 implements IExercise12 {
    @Override
    public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents) {
        //Create gopy so as not to remove links in actual graph
        Map<Integer, Set<Integer>> graphCopy = new HashMap<>(graph);

        List<Set<Integer>> subgraph = getComponents(graphCopy);
        while (subgraph.size() < minimumComponents) {
            Map<Integer, Map<Integer, Double>> betCent = getEdgeBetweenness(graphCopy);
            //remove edges with highest betCent
            List<Edge> toDelete = findMaxEdgesToRemove(betCent);
            for (Edge edge : toDelete) {
                graphCopy.get(edge.getNode()).remove(edge.getNeighbour());
            }

            subgraph = getComponents(graphCopy);
        }
        return subgraph;
    }

    private List<Edge> findMaxEdgesToRemove(Map<Integer, Map<Integer, Double>> betCent) {
        List<Edge> maxEdges = new ArrayList<>();
        double maxBetCent = 0.0;
        for (int node : betCent.keySet()) {
            for (int neighbour : betCent.get(node).keySet()) {
                double currBetCent = betCent.get(node).get(neighbour);
                if (currBetCent > maxBetCent) {
                    maxBetCent = currBetCent;
                    maxEdges = new ArrayList<>();
                    maxEdges.add(new Edge(node, neighbour));
                }
                else if (currBetCent == maxBetCent)
                    maxEdges.add(new Edge(node, neighbour));
            }
        }

        return maxEdges;
    }

    @Override
    public int getNumberOfEdges(Map<Integer, Set<Integer>> graph) {
        int cEdges = 0;
        for (Set<Integer> neighbours : graph.values()) {
            cEdges += neighbours.size();
        }
        return cEdges/2; //divide by 2 due to handshaking lemma
    }

    @Override
    public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph) {
        List<Set<Integer>> result = new LinkedList<>();
        //Create a set of nodes that have not been visited yet
        Set<Integer> notVisited = new HashSet<>(graph.keySet());

        Random r = new Random();
        //Apply dfs until all nodes have been reached i.e. removed from notVisited
        while (notVisited.size() > 0) {
            //select random node that has not yet been visited to be the source
            List<Integer> keysAsArray = new ArrayList<>(notVisited);
            int nextSource = keysAsArray.get(r.nextInt(keysAsArray.size()));
            //Nodes when found shall be added to set, newCluster
            Set<Integer> newCluster = new HashSet<>();
            visit(graph, notVisited, nextSource, newCluster);
            result.add(newCluster);
        }
        return result;
    }

    private void visit(Map<Integer, Set<Integer>> graph, Set<Integer> notVisited, int curr, Set<Integer> result) {
        notVisited.remove(curr);
        result.add(curr);
        for (int neighbour : graph.get(curr)) {
            if (notVisited.contains(neighbour))
                visit(graph, notVisited, neighbour, result);
        }
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph) {
        //Set default values of betCent to 0.0
        Map<Integer, Map<Integer, Double>> betCent = new HashMap<>();
        for (Integer v : graph.keySet()) {
            Map<Integer, Double> inner = new HashMap<>();
            for (Integer w : graph.get(v)) {
                inner.put(w, 0.0);
            }
            betCent.put(v, inner);
        }


        for (Integer s : graph.keySet()) {
            Map<Integer, List<Integer>> predecessors = new HashMap<>();
            Map<Integer, Integer> distance = new HashMap<>();
            Map<Integer, Integer> cShortestPaths = new HashMap<>();
            Map<Integer, Double> dependency = new HashMap<>();

            List<Integer> queue = new LinkedList<>();
            List<Integer> stack = new LinkedList<>();
            //Initialise values
            for (Integer w : graph.keySet()) {
                predecessors.put(w, new LinkedList<>());
                distance.put(w, -1);
                cShortestPaths.put(w, 0);
            }
            distance.put(s, 0);
            cShortestPaths.put(s, 1);
            queue.add(s);

            //single-source shortest-path problem
            while (!queue.isEmpty()) {
                Integer v = queue.remove(0);
                stack.add(0, v);
                for (Integer w : graph.get(v)) {
                    //path discovery
                    if (distance.get(w) == -1) {
                        distance.put(w, distance.get(v) + 1);
                        queue.add(w);
                    }
                    //path counting
                    if (distance.get(w) == distance.get(v) + 1) {
                        cShortestPaths.put(w, cShortestPaths.get(w) + cShortestPaths.get(v));
                        predecessors.get(w).add(v);
                    }
                }
            }
            //accumulation
            for (Integer v : graph.keySet()) {
                dependency.put(v, 0.0);
            }
            while (!stack.isEmpty()) {
                Integer w = stack.remove(0);
                for (Integer v : predecessors.get(w)) {
                    double c = ((double) cShortestPaths.get(v) / (double) cShortestPaths.get(w)) * (1 + dependency.get(w));
                    betCent.get(v).put(w, betCent.get(v).get(w) + c);
                    dependency.put(v, dependency.get(v) + c);
                }
            }
        }
        return betCent;
    }
}