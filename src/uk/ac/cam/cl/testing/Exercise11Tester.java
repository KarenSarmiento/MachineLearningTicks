package uk.ac.cam.cl.testing;

import uk.ac.cam.cl.ks828.exercises.tick11.Exercise11;
import uk.ac.cam.cl.mlrd.exercises.networks.IExercise11;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.Set;



public class Exercise11Tester {
    static final Path graphFile = Paths.get("data/network_files/simple_network.edges");

    public static void main(String[] args) throws IOException {
        IExercise11 implementation = (IExercise11) new Exercise11();

        Map<Integer, Double> betweennesses = implementation.getNodeBetweenness(graphFile);
        List<Entry<Integer, Double>> sortedBetweennesses = new ArrayList<>(betweennesses.entrySet());
        sortedBetweennesses.sort(new Comparator<Entry<Integer, Double>>() {
            @Override
            public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
                return (int) Math.signum(o2.getValue() - o1.getValue());
            }
        });
        System.out.println("Network betweennesses:");
        System.out.println(sortedBetweennesses);
        System.out.println();
    }
}