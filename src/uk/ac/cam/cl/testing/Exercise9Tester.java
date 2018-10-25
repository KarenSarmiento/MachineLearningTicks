package uk.ac.cam.cl.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import uk.ac.cam.cl.ks828.exercises.tick7.*;
import uk.ac.cam.cl.ks828.exercises.tick8.Exercise8;
import uk.ac.cam.cl.ks828.exercises.tick9.*;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise7;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise8;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise9;

public class Exercise9Tester {

    static final Path dataFile = Paths.get("data/bio_dataset.txt");

    public static List<List<HMMDataStore<AminoAcid, Feature>>> splitIntoTenFolds(List<HMMDataStore<AminoAcid, Feature>> dataSet, int seed) {
        //Create 10 lists (folds) for each of the paths to be added to
        List<List<HMMDataStore<AminoAcid, Feature>>> result = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            List<HMMDataStore<AminoAcid, Feature>> newList = new LinkedList<>();
            result.add(newList);
        }

        //Generates a number between 0 and 1
        Random rng = new Random(seed);
        int initialListSize = dataSet.size() - 1;
        for (int i = initialListSize; i >= 0; i--) {
            int indexOfChosenPath = (int) rng.nextDouble() * i;
            int fold = i % 10;
            result.get(fold).add(dataSet.get(indexOfChosenPath));
            dataSet.remove(indexOfChosenPath);
        }

        return result;
    }

    public static void crossValidate(List<List<HMMDataStore<AminoAcid, Feature>>> folds) throws IOException {
        double[] accuracies = new double[10];
        IExercise7 implementation7 = (IExercise7) new Exercise7();
        IExercise9 implementation = (IExercise9) new Exercise9();
        double precision = 0;
        double recall = 0;
        double fOneMeasure = 0;
        for (int i = 0; i < accuracies.length; i++) {
            //Create test set for current accuracy
            List<HMMDataStore<AminoAcid, Feature>> trainingData = new LinkedList<>();
            List<HMMDataStore<AminoAcid, Feature>> testingData = new LinkedList<>();
            for (int j = 0; j < folds.size(); j++) {
                if (j != i)
                    trainingData.addAll(folds.get(j));
                else
                    testingData = folds.get(j);
            }
            //Perform training on training data i.e. generate transitions and emissions
            HiddenMarkovModel<AminoAcid, Feature> model = implementation.estimateHMM(testingData);

            //Test
            HMMDataStore<AminoAcid, Feature> data = trainingData.get(0);
            List<Feature> predicted = implementation.viterbi(model, data.observedSequence);
            Map<List<Feature>, List<Feature>> true2PredictedSequences = implementation.predictAll(model, testingData);

            precision += implementation.precision(true2PredictedSequences);
            recall += implementation.recall(true2PredictedSequences);
            fOneMeasure += implementation.fOneMeasure(true2PredictedSequences);
        }
        System.out.println("Prediction precision:");
        System.out.println(precision / 10.0);
        System.out.println();

        System.out.println("Prediction recall:");
        System.out.println(recall / 10.0);
        System.out.println();

        System.out.println("Prediction fOneMeasure:");
        System.out.println(fOneMeasure / 10.0);
        System.out.println();
    }

    public static void main(String[] args) throws IOException {

        List<HMMDataStore<AminoAcid, Feature>> sequencePairs = HMMDataStore.loadBioFile(dataFile);

        // Use for testing the code
        Collections.shuffle(sequencePairs, new Random(0));

        IExercise9 implementation = (IExercise9) new Exercise9();
        List<List<HMMDataStore<AminoAcid, Feature>>> tenFolds = splitIntoTenFolds(sequencePairs, 0);
        crossValidate(tenFolds);
    }
}