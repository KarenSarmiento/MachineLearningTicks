package uk.ac.cam.cl.ks828.exercises.tick10;

import uk.ac.cam.cl.mlrd.exercises.networks.IExercise10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by KSarm on 05/03/2018.
 */
public class Exercise10 implements IExercise10 {
    @Override
    public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
        Map<Integer, Set<Integer>> result = new HashMap<>();

        //Read through file and add edges to map
        BufferedReader br = Files.newBufferedReader(graphFile);
        String line;
        while ((line = br.readLine()) != null) {
            String[] nodes = line.split(" ");
            int nodeA = Integer.valueOf(nodes[0]);
            int nodeB = Integer.valueOf(nodes[1]);
            Set<Integer> setA = result.getOrDefault(nodeA, new HashSet<>());
            Set<Integer> setB = result.getOrDefault(nodeB, new HashSet<>());
            setA.add(nodeB);
            setB.add(nodeA);
            result.put(nodeA, setA);
            result.put(nodeB, setB);
        }
        return result;
    }

    @Override
    public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {
        Map<Integer, Integer> result = new HashMap<>();
        for (Integer node : graph.keySet()) {
            Set<Integer> neighbours = graph.get(node);
            result.put(node, neighbours.size());
        }
        return result;
    }


    @Override
    public int getDiameter(Map<Integer, Set<Integer>> graph) {
        int maxDistance = 0;
        for (Integer node : graph.keySet()) {
            int currDistance = bfs(graph, node);
            if (currDistance > maxDistance)
                maxDistance = currDistance;
        }
        return maxDistance;
    }

    private int bfs(Map<Integer, Set<Integer>> graph, int source) {
        Map<Integer, Integer> seen = new HashMap<>();

        //Add first to node to toExplore
        List<Integer> toExplore = new LinkedList<>();
        toExplore.add(source);
        seen.put(source, 0);

        //traverse graph, visiting everything reachable from s
        int curr = -1;
        while (!toExplore.isEmpty()) {
            curr = toExplore.remove(0);
            for (int neighbour : graph.get(curr)) {
                if (!seen.containsKey(neighbour)) {
                    toExplore.add(neighbour);
                    seen.put(neighbour, seen.get(curr) +1);
                }
            }
        }
        //curr should be the last that has been touched, and hence, the farthest
        return seen.get(curr);
    }
}
