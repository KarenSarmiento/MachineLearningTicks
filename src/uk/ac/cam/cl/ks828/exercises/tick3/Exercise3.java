package uk.ac.cam.cl.ks828.exercises.tick3;

import edu.stanford.nlp.ling.Word;
import uk.ac.cam.cl.ks828.exercises.tick1.Tokenizer;
import uk.ac.cam.cl.ks828.exercises.tick3.BestFit.Point;
import uk.ac.cam.cl.ks828.exercises.tick3.BestFit.Line;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by KSarm on 26/01/2018.
 */
public class Exercise3 {

    static final Path dataDirectory = Paths.get("data");
    private Line bestFit;

    /*
        Step 2: Count how many unique words the system finds in your input files for any given number of tokens.
        Collect a datapoint every time the total number of tokens you have read in reaches a power of two
        Also provide a data point for the total number of tokens in all texts
     */

    private void plotHeapsGraph() {
        System.out.println("starting..");
        Map<Integer, Integer> typeToNumOfTokensMap = countUniqueWords();
        System.out.println(1);
        List<Point> points = new LinkedList<>();
        for (int typesCount : typeToNumOfTokensMap.keySet()) {
            points.add(new Point(Math.log(typeToNumOfTokensMap.get(typesCount)),
                    Math.log(typesCount)));
        }
        System.out.println(2);
        ChartPlotter.plotLines(points);
        System.out.println(3);
    }

    //Returns a Map<Integer, Integer> which maps the number of new types seen to number of tokens read
    private Map<Integer, Integer> countUniqueWords() {
        Set<Path> dataSet = readInDataSet();
        Map<Integer, Integer> typeToNumOfTokensMap = new HashMap<>();
        Set<String> seenWords = new HashSet<>();
        try {
            int wordsSearched = 0;
            for (Path path : dataSet) {
                Tokenizer tokenizer = new Tokenizer();
                List<String> wordsInPath = tokenizer.tokenize(path);
                for (String word : wordsInPath) {
                    seenWords.add(word);
                    if (Integer.bitCount(wordsSearched) == 1 || wordsSearched == 0) { //checks if the number of element you've read is power of 2
                        typeToNumOfTokensMap.put(seenWords.size(), wordsSearched+1);
                    }
                    wordsSearched++;
                }
            }
            typeToNumOfTokensMap.put(seenWords.size(), wordsSearched);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return typeToNumOfTokensMap;
    }

    /*
        7. Use the best-fit line to estimate the Zipf’s law parameters k and α.
     */
    private void printKandAlpha() {
        generateBestFitLine(findFrequenciesOfAllTokensInDatasetAndRankThem());
        System.out.println("K = " + estimateK());
        System.out.println("Alpha = " + estimateAlpha());
    }
    private double estimateK() {
        return Math.exp(bestFit.yIntercept);
    }

    private double estimateAlpha() {
        return -bestFit.gradient;
    }

    /*
        6. Use the best fit line to create a function which given a rank can output an expected frequency.
     */
    private void printPredictedAndAcualFrequency() {
        System.out.println("Getting frequencies...");
        String[] topTenWords = {"amazing", "entertaining", "exciting", "effective", "funny",
                "boring", "uninteresting", "bad", "unexciting", "uneventful"};
        List<WordFrequency> orderedWords = findFrequenciesOfAllTokensInDatasetAndRankThem();
        System.out.println("Searching words...");
        for (WordFrequency wordFrequency : orderedWords) {
            for (String task1Word : topTenWords) {
                if (wordFrequency.getWord().equals(task1Word)) {
                    System.out.println("For the word " + task1Word
                            + ": Predicted Frequency = " + predictFrequencyForRank(wordFrequency.getRank())
                            + "; Actual Frequency = " + wordFrequency.getFrequency());
                }
            }
        }
    }


    private int predictFrequencyForRank(int rank) {
        List<WordFrequency> orderedWords = findFrequenciesOfAllTokensInDatasetAndRankThem();
        generateBestFitLine(orderedWords);
        double loggedY = Math.log(rank)*bestFit.gradient + bestFit.yIntercept;
        return (int) Math.rint(Math.exp(loggedY));
    }

    /*
        5. Fit a line to the log-log graph. Weight each word by its frequency to avoid distortion
        in favour of less common words. Add the line to the plot.
     */
    private void plotAndFitLineToLogGraph() {
        System.out.println(3);
        List<WordFrequency> orderedWords = findFrequenciesOfAllTokensInDatasetAndRankThem();
        System.out.println(2);
        generateBestFitLine(orderedWords);
        System.out.println(0);
        List<Point> bestFitPoints = new LinkedList<>();
        bestFitPoints.add(new Point(Math.log(1), bestFit.gradient*Math.log(1) + bestFit.yIntercept));
        bestFitPoints.add(new Point(Math.log(10000), bestFit.gradient*Math.log(10000) + bestFit.yIntercept));

        List<Point> curvePoints = generateLoggedCurvePoints(orderedWords);

        ChartPlotter.plotLines(curvePoints, bestFitPoints);
        System.out.println("GO");
    }

    private void generateBestFitLine(List<WordFrequency> orderedWords) {
        Map<Point, Double> pointsAndWeights = new HashMap<>();
        for (WordFrequency wordFrequency : orderedWords) {
            pointsAndWeights.put(new Point(Math.log(wordFrequency.getRank() +1),
                    Math.log(wordFrequency.getFrequency() +1)), (double) wordFrequency.getFrequency());
        }
        this.bestFit = BestFit.leastSquares(pointsAndWeights);
    }

    /*
        4. Plot the main graph on the log-log scale.
     */
    private void plotFrequencyAgainstRankWithLoggedScales() {
        List<Point> points = generateLoggedCurvePoints(findFrequenciesOfAllTokensInDatasetAndRankThem());
        ChartPlotter.plotLines(points);
        System.out.println("GO!:)");

    }

    private List<Point> generateLoggedCurvePoints(List<WordFrequency> orderedWords) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            WordFrequency wordFrequency = orderedWords.get(i);
            points.add(new Point(Math.log(wordFrequency.getRank() +1), Math.log(wordFrequency.getFrequency() +1)));
        }
        return points;
    }


    /*
        3. Plot the Task 1 words on the frequency-rank plot as a separate series
     */
    private void plotTenWordsOfChoice() {
        String[] topTenWords = {"amazing", "entertaining", "exciting", "effective", "funny",
                "boring", "uninteresting", "bad", "unexciting", "uneventful"};
        //Add words that exist in our frequency list to a new list and plot accordingly
        List<WordFrequency> orderedWords = findFrequenciesOfAllTokensInDatasetAndRankThem();
        List<Point> points = new LinkedList<>();
        for (String wordToPlot : topTenWords) {
            for (WordFrequency orderedWordFrequency : orderedWords) {
                if (wordToPlot.equals(orderedWordFrequency.getWord())) {
                    points.add(new Point(orderedWordFrequency.getRank(), orderedWordFrequency.getFrequency()));
                    break;
                }
            }
        }
        ChartPlotter.plotLines(points);
    }

    /*
        2. Plot a frequency vs rank graph for the 10,000 highest-ranked tokens.
     */
    private void plotFrequencyAgainstRank() {
        //Create a list of points to be plotted
        List<WordFrequency> orderedWords = findFrequenciesOfAllTokensInDatasetAndRankThem();
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            WordFrequency wordFrequency = orderedWords.get(i);
            points.add(new Point(wordFrequency.getRank(), wordFrequency.getFrequency()));
        }
        ChartPlotter.plotLines(points);
    }

    /*
        1. Find frequencies of all the tokens in the dataset and rank them.
        This is output in the form of a list of WordFrequency objects, whereby the word at index 0 is of the highest frequency.
     */
    private List<WordFrequency> findFrequenciesOfAllTokensInDatasetAndRankThem() {
        Set<Path> dataset = readInDataSet();
        Tokenizer tokenizer = new Tokenizer();
        Map<String, Integer> wordFrequencies = new HashMap<>();
        try {
            //Count the frequency of each word within all paths
            for (Path path : dataset) {
                List<String> wordsInPath = tokenizer.tokenize(path);
                for (String word : wordsInPath) {
                    if (wordFrequencies.containsKey(word))
                        wordFrequencies.put(word, wordFrequencies.get(word) + 1);
                    else
                        wordFrequencies.put(word, 1);
                }
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

        //order the words within the linked list
        List<String> orderedWords = new ArrayList<>(wordFrequencies.keySet());
        Collections.sort(orderedWords, (word1, word2) -> wordFrequencies.get(word2) - wordFrequencies.get(word1));

        //add on the frequency and rank by using the WordFrequency object
        List<WordFrequency> orderedWithFrequency = new ArrayList<>();
        for (int i = 0; i < orderedWords.size(); i++) {
            String word = orderedWords.get(i);
            orderedWithFrequency.add(new WordFrequency(word, wordFrequencies.get(word), i));
        }
        //frequency is fine here within orderedWithFreq and map
        return orderedWithFrequency;
    }

    private Set<Path> readInDataSet() {
        HashSet<Path> dataset = new HashSet<>();
        try {
            //Load in the dataset
            Path dataDirectory = Paths.get("data");
            Path large_dataset = dataDirectory.resolve("large_dataset");
            DirectoryStream<Path> files = Files.newDirectoryStream(large_dataset);
            for (Path item : files) {
                dataset.add(item);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return dataset;
    }

    public static void main(String[] args) {
        Exercise3 ex = new Exercise3();
        ex.plotHeapsGraph();
    }
}
