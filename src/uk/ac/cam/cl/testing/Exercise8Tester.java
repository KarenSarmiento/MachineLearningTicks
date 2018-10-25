package uk.ac.cam.cl.testing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import uk.ac.cam.cl.ks828.exercises.tick1.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.markov_models.*;
import uk.ac.cam.cl.ks828.exercises.tick7.*;
import uk.ac.cam.cl.ks828.exercises.tick8.*;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise7;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise8;

public class Exercise8Tester {

    static final Path dataDirectory = Paths.get("data/dice_dataset");

    public static List<List<Path>> splitIntoTenFolds(List<Path> dataSet, int seed) {
        //Create 10 lists (folds) for each of the paths to be added to
        List<List<Path>> result = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            List<Path> newList = new LinkedList<>();
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

        public static void crossValidate(List<List<Path>> folds) throws IOException {
            double[] accuracies = new double[10];
            IExercise7 implementation7 = (IExercise7) new Exercise7();
            IExercise8 implementation = (IExercise8) new Exercise8();
            double precision = 0;
            double recall = 0;
            double fOneMeasure = 0;
            for (int i = 0; i < accuracies.length; i++) {
                //Create test set for current accuracy
                List<Path> trainingData = new LinkedList<>();
                List<Path> testingData = new LinkedList<>();
                for (int j = 0; j < folds.size(); j++) {
                    if (j != i)
                        trainingData.addAll(folds.get(j));
                    else
                        testingData = folds.get(j);
                }
                //Perform training on training data i.e. generate transitions and emissions
                HiddenMarkovModel<DiceRoll, DiceType> model = implementation7.estimateHMM(testingData);
                HMMDataStore<DiceRoll, DiceType> data = HMMDataStore.loadDiceFile(trainingData.get(0));
                List<DiceType> predicted = implementation.viterbi(model, data.observedSequence);

                Map<List<DiceType>, List<DiceType>> true2PredictedMap = implementation.predictAll(model, trainingData);

                precision += implementation.precision(true2PredictedMap);
                recall += implementation.recall(true2PredictedMap);
                fOneMeasure += implementation.fOneMeasure(true2PredictedMap);
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


    public static void main(String[] args)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        List<Path> sequenceFiles = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
            for (Path item : files) {
                sequenceFiles.add(item);
            }
        } catch (IOException e) {
            throw new IOException("Cant access the dataset.", e);
        }

        // Use for testing the code
        Collections.shuffle(sequenceFiles, new Random(0));

        List<List<Path>> tenFolds = splitIntoTenFolds(sequenceFiles, 0);
        crossValidate(tenFolds);
    }

}