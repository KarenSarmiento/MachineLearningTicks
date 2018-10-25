package uk.ac.cam.cl.ks828.exercises.Supervisions;

import uk.ac.cam.cl.ks828.exercises.tick1.DataPreparation1;
import uk.ac.cam.cl.ks828.exercises.tick1.Exercise1;
import uk.ac.cam.cl.ks828.exercises.tick1.Sentiment;
import uk.ac.cam.cl.ks828.exercises.tick1.Tokenizer;
import uk.ac.cam.cl.ks828.exercises.tick2.DataSplit;
import uk.ac.cam.cl.ks828.exercises.tick2.Exercise2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by KSarm on 03/02/2018.
 */
public class Supervision1 {

    static Exercise1 ex1 = new Exercise1();
    static Exercise2 ex2 = new Exercise2();

    static final Path dataDirectory = Paths.get("data/sentiment_dataset");
    static final Path myTestDirectory = Paths.get("data/Supervision");

    private static Set<String> privateReadInPartnersList(String fileName) {
        Set<String> tokens = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\KSarm\\workspace\\MachineLearning\\data\\Supervision\\" + fileName));
            String line;
            while ((line = br.readLine()) != null) {
                tokens.add(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return tokens;
    }

    private static List<String> createListOnMyText() throws IOException {
        Path myText = Paths.get("data/Supo1TextToAnalyse.txt");
        List<String> tokens = Tokenizer.tokenize(myText);
        Set<String> setOfTokens = new HashSet<>(tokens);
        List<String> finalTokens = new LinkedList<>(setOfTokens);
        Collections.sort(finalTokens);
        for (String s : finalTokens) {
            System.out.println(s);
        }
        return tokens;
    }

    private static void testTask2() throws IOException {
        Path sentimentFile = dataDirectory.resolve("review_sentiment");
        Map<Path, Sentiment> dataSet = DataPreparation1.loadSentimentDataset(dataDirectory.resolve("reviews"),
                sentimentFile);


        DataSplit<Sentiment> split = new DataSplit<Sentiment>(dataSet, 0);
        Path mySentimentFile = myTestDirectory.resolve("review_sentiment");
        Path myReviewsDir = myTestDirectory.resolve("reviews");
        Map<Path, Sentiment> valSet = DataPreparation1.loadSentimentDataset(myReviewsDir, mySentimentFile);
        split.setValidationSet(valSet);


        Map<Sentiment, Double> classProbabilities = ex2.calculateClassProbabilities(split.trainingSet);
        Map<String, Map<Sentiment, Double>> smoothedLogProbs = ex2
                .calculateSmoothedLogProbs(split.trainingSet);
        Map<Path, Sentiment> smoothedNBPredictions = ex2.naiveBayes(split.validationSet.keySet(),
                smoothedLogProbs, classProbabilities);

        double smoothedNBAccuracy = ex1.calculateAccuracy(split.validationSet, smoothedNBPredictions);
        System.out.println("Naive Bayes classifier accuracy with smoothing:");
        System.out.println(smoothedNBAccuracy);
        System.out.println();
    }

    private static void testTask1() throws IOException {
        Path lexiconFile = Paths.get("data/sentiment_lexicon.txt");
        Path sentimentFile = myTestDirectory.resolve("review_sentiment");
        Path reviewsDir = myTestDirectory.resolve("reviews");
        Map<Path, Sentiment> dataSet = DataPreparation1.loadSentimentDataset(reviewsDir, sentimentFile);

        Map<Path, Sentiment> predictedSentiments = ex1.simpleClassifier(dataSet.keySet(), lexiconFile);
        System.out.println("Classifier predictions:");
        System.out.println(predictedSentiments);
        System.out.println();

        TreeMap treeMap;

        double calculatedAccuracy = ex1.calculateAccuracy(dataSet, predictedSentiments);
        System.out.println("Classifier accuracy:");
        System.out.println(calculatedAccuracy);
        System.out.println();

        Map<Path, Sentiment> improvedPredictions = ex1.improvedClassifier(dataSet.keySet(), lexiconFile);
        System.out.println("Improved classifier predictions:");
        System.out.println(improvedPredictions);
        System.out.println();

        System.out.println("Improved classifier accuracy:");
        System.out.println(ex1.calculateAccuracy(dataSet, improvedPredictions));
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        testTask1();
        testTask2();
    }
}
