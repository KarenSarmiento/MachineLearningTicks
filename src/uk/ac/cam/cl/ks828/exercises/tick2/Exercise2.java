package uk.ac.cam.cl.ks828.exercises.tick2;

import uk.ac.cam.cl.ks828.exercises.tick1.Sentiment;
import uk.ac.cam.cl.ks828.exercises.tick1.Tokenizer;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by KSarm on 22/01/2018.
 */
public class Exercise2 implements IExercise2 {

    /**
     * Calculate the probability of a document belonging to a given class based
     * on the training data.
     *
     * @param trainingSet
     *            {@link Map}<{@link Path}, {@link Sentiment}> Training review
     *            paths
     * @return {@link Map}<{@link Sentiment}, {@link Double}> Class
     *         probabilities.
     * @throws IOException
     */
    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {
        double posCount = 0;
        List<Sentiment> valueSet = new LinkedList<>(trainingSet.values());
        for (Sentiment s : valueSet) {
            if (s.equals(Sentiment.POSITIVE))
                posCount++;
        }
        double p_pos = posCount / valueSet.size();
        double p_neg = 1 - p_pos;

        Map<Sentiment, Double> classProbabilities = new HashMap<>();
        classProbabilities.put(Sentiment.POSITIVE, p_pos);
        classProbabilities.put(Sentiment.NEGATIVE, p_neg);

        return classProbabilities;
    }

    /**
     * For each word and sentiment present in the training set, estimate the
     * unsmoothed log probability of a word to occur in a review with a
     * particular sentiment.
     *
     * @param trainingSet
     *            {@link Map}<{@link Path}, {@link Sentiment}> Training review
     *            paths
     * @return {@link Map}<{@link String}, {@link Map}<{@link Sentiment},
     *         {@link Double}>> Estimated log probabilities
     * @throws IOException
     */
    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        Set<Path> pathSet = trainingSet.keySet();
        Tokenizer tokenizer = new Tokenizer();
        //Describes, for each word, the NUMBER of times the word appears
        //among all words in all documents, for each of positive and negative sentiments
        Map<String, Map<Sentiment, Double>> result = new HashMap<>();
        for (Path p : pathSet) {
            Sentiment currentPathSentiment = trainingSet.get(p);
            List<String> wordsInPath = tokenizer.tokenize(p);
            for (String word : wordsInPath) {
                //if word does not exist in the set, then add it
                if (!result.containsKey(word)) {
                    //Put positive and negative counts in and set to 0
                    Map<Sentiment, Double> posAndNegCount = new HashMap<>();
                    posAndNegCount.put(Sentiment.POSITIVE, 0.0);
                    posAndNegCount.put(Sentiment.NEGATIVE, 0.0);
                    result.put(word, posAndNegCount);
                }

                //Now, the current word must exist in the map, so we increment the relevant count
                Map<Sentiment, Double> currentWordSentimentMap = result.get(word);
                if (currentPathSentiment.equals(Sentiment.POSITIVE))
                    currentWordSentimentMap.put(Sentiment.POSITIVE, currentWordSentimentMap.get(Sentiment.POSITIVE) + 1);
                else
                    currentWordSentimentMap.put(Sentiment.NEGATIVE, currentWordSentimentMap.get(Sentiment.NEGATIVE) + 1);
            }

        }
        //Convert result such that it describes, for each word, the FRACTION of times the word appears
        //among all words in all documents, for each of positive and negative sentiments
        Set<String> wordsInLexicon = result.keySet();
        double totalPositiveCount = 0;
        double totalNegativeCount = 0;
        for (String word : wordsInLexicon) {
            totalPositiveCount += result.get(word).get(Sentiment.POSITIVE);
            totalNegativeCount += result.get(word).get(Sentiment.NEGATIVE);

        }
        for (String word : wordsInLexicon) {
            Map<Sentiment, Double> currentWorldSentimentMap2 = result.get(word);
            currentWorldSentimentMap2.put(Sentiment.POSITIVE, Math.log(currentWorldSentimentMap2.get(Sentiment.POSITIVE) / totalPositiveCount));
            currentWorldSentimentMap2.put(Sentiment.NEGATIVE, Math.log(currentWorldSentimentMap2.get(Sentiment.NEGATIVE) / totalNegativeCount));
        }
        System.out.println(result.toString());
        return result;
    }

    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        Set<Path> pathSet = trainingSet.keySet();
        Tokenizer tokenizer = new Tokenizer();
        //Describes, for each word, the NUMBER of times the word appears
        //among all words in all documents, for each of positive and negative sentiments
        Map<String, Map<Sentiment, Double>> result = new HashMap<>();
        for (Path p : pathSet) {
            Sentiment currentPathSentiment = trainingSet.get(p);
            List<String> wordsInPath = tokenizer.tokenize(p);
            for (String word : wordsInPath) {
                //if word does not exist in the set, then add it
                if (!result.containsKey(word)) {
                    //Put positive and negative counts in and set to 0
                    Map<Sentiment, Double> posAndNegCount = new HashMap<>();
                    posAndNegCount.put(Sentiment.POSITIVE, 1.0);
                    posAndNegCount.put(Sentiment.NEGATIVE, 1.0);
                    result.put(word, posAndNegCount);
                }

                //Now, the current word must exist in the map, so we increment the relevant count
                Map<Sentiment, Double> currentWordSentimentMap = result.get(word);
                if (currentPathSentiment.equals(Sentiment.POSITIVE))
                    currentWordSentimentMap.put(Sentiment.POSITIVE, currentWordSentimentMap.get(Sentiment.POSITIVE) + 1);
                else
                    currentWordSentimentMap.put(Sentiment.NEGATIVE, currentWordSentimentMap.get(Sentiment.NEGATIVE) + 1);
            }

        }

        //Convert result such that it describes, for each word, the FRACTION of times the word appears
        //among all words in all documents, for each of positive and negative sentiments
        Set<String> wordsInLexicon = result.keySet();
        double totalPositiveCount = 0;
        double totalNegativeCount = 0;
        for (String word : wordsInLexicon) {
            totalPositiveCount += result.get(word).get(Sentiment.POSITIVE);
            totalNegativeCount += result.get(word).get(Sentiment.NEGATIVE);

        }
        for (String word : wordsInLexicon) {
            Map<Sentiment, Double> currentWorldSentimentMap2 = result.get(word);
            currentWorldSentimentMap2.put(Sentiment.POSITIVE, Math.log(currentWorldSentimentMap2.get(Sentiment.POSITIVE) / totalPositiveCount));
            currentWorldSentimentMap2.put(Sentiment.NEGATIVE, Math.log(currentWorldSentimentMap2.get(Sentiment.NEGATIVE) / totalNegativeCount));
        }
        return result;
    }

    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException {
        Map<Path, Sentiment> result = new HashMap<>();

        //For each path, calculate the predicted sentiment value by applying the formula
        Set<String> keySet = tokenLogProbs.keySet();
        for(Path p : testSet) {
            Tokenizer tokenizer = new Tokenizer();
            List<String> wordsInPath = tokenizer.tokenize(p);

            double positiveProbability = Math.log(classProbabilities.get(Sentiment.POSITIVE));
            double negativeProbability = Math.log(classProbabilities.get(Sentiment.NEGATIVE));
            for (String word : wordsInPath) {
                if (tokenLogProbs.containsKey(word)) {
                    positiveProbability += tokenLogProbs.get(word).get(Sentiment.POSITIVE);
                    negativeProbability+= tokenLogProbs.get(word).get(Sentiment.NEGATIVE);
                }
            }
            if (positiveProbability >= negativeProbability)
                result.put(p, Sentiment.POSITIVE);

            else
                result.put(p, Sentiment.NEGATIVE);
        }
        System.out.println("Naive Classifier predictions:\n" + result);
        return result;
    }
}