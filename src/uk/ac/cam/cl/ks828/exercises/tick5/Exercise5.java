package uk.ac.cam.cl.ks828.exercises.tick5;

import uk.ac.cam.cl.ks828.exercises.tick1.Exercise1;
import uk.ac.cam.cl.ks828.exercises.tick1.Sentiment;
import uk.ac.cam.cl.ks828.exercises.tick2.Exercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by KSarm on 02/02/2018.
 */
public class Exercise5 implements IExercise5 {
    private Exercise2 ex2 = new Exercise2();
    private Exercise1 ex1 = new Exercise1();

    @Override
    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
        //Place into list so they can be selected randomly
        List<Path> pathList = new LinkedList<>(dataSet.keySet());
        return splitHashMapIntoTenFolds(dataSet, seed, pathList);
    }

    @Override
    public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {
        //split positive and negative sentiment paths into 10 equal folds separately,
        //then merge them in order create an over 10 folds containing equal amounts of positive and negative paths
        List<Path> positivePaths = new LinkedList<>();
        List<Path> negativePaths = new LinkedList<>();
        for (Path path : dataSet.keySet()) {
            if (dataSet.get(path).equals(Sentiment.POSITIVE))
                positivePaths.add(path);
            else
                negativePaths.add(path);
        }
        List<Map<Path, Sentiment>> positiveFolds = splitHashMapIntoTenFolds(dataSet, seed, positivePaths);
        List<Map<Path, Sentiment>> negativeFolds = splitHashMapIntoTenFolds(dataSet, seed, negativePaths);
        List<Map<Path, Sentiment>> result = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            Map<Path, Sentiment> totalFold = new HashMap<>();
            totalFold.putAll(positiveFolds.get(i));
            totalFold.putAll(negativeFolds.get(i));
            result.add(totalFold);
        }
        return result;
    }

    private List<Map<Path, Sentiment>> splitHashMapIntoTenFolds(Map<Path, Sentiment> dataSet, int seed, List<Path> pathList) {
        //Create 10 lists (folds) for each of the paths to be added to
        List<Map<Path, Sentiment>> result = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            Map<Path, Sentiment> newMap = new HashMap<>();
            result.add(newMap);
        }

        //Generates a number between 0 and 1
        Random rng = new Random(seed);
        int initialListSize = pathList.size() -1;
        for (int i = initialListSize; i >= 0; i--) {
            int indexOfChosenPath = (int) rng.nextDouble()*i;
            int fold = i%10;
            result.get(fold).put(pathList.get(indexOfChosenPath), dataSet.get(pathList.get(indexOfChosenPath)));
            pathList.remove(indexOfChosenPath);
        }

        return result;
    }

    @Override
    public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {
        double[] accuracies = new double[10];
        for (int i = 0; i < accuracies.length; i++) {
            //Create test set for current accuracy
            Map<Path, Sentiment> trainingData = new HashMap<>();
            Map<Path, Sentiment> testingData = new HashMap<>();
            for (int j = 0; j < folds.size(); j++) {
                if (j != i)
                    trainingData.putAll(folds.get(j));
                else
                    testingData = folds.get(j);
            }

            //Perform naive bayes
            Map<String, Map<Sentiment, Double>> smoothedLogProbs = ex2.calculateSmoothedLogProbs(trainingData);
            Map<Sentiment, Double> classProbabilities = ex2.calculateClassProbabilities(trainingData);
            Map<Path, Sentiment> smoothedNBPredictions = ex2.naiveBayes(testingData.keySet(), smoothedLogProbs, classProbabilities);
            double smoothedNBAccuracy = ex1.calculateAccuracy(testingData, smoothedNBPredictions);
            accuracies[i] = smoothedNBAccuracy;
        }
        return accuracies;
    }

    @Override
    public double cvAccuracy(double[] scores) {
        double sum = 0;
        for (double accuracy : scores) {
            sum += accuracy;
        }
        return sum/scores.length;
    }

    @Override
    public double cvVariance(double[] scores) {
        double average = cvAccuracy(scores);
        double sum = 0;
        for (double accuracy : scores) {
            sum += Math.pow(accuracy - average,2);
        }
        return (1.0/scores.length)*sum;
    }

}
