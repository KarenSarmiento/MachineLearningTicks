package uk.ac.cam.cl.ks828.exercises.tick6;

import uk.ac.cam.cl.ks828.exercises.tick1.DataPreparation1;
import uk.ac.cam.cl.ks828.exercises.tick1.Sentiment;
import uk.ac.cam.cl.ks828.exercises.tick1.Tokenizer;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise6;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by KSarm on 05/02/2018.
 */
public class Exercise6 implements IExercise6 {

    @Override
    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        double posCount = 0;
        double negCount = 0;
        List<NuancedSentiment> valueSet = new LinkedList<>(trainingSet.values());
        for (NuancedSentiment s : valueSet) {
            if (s.equals(NuancedSentiment.POSITIVE))
                posCount++;
            else if (s.equals(NuancedSentiment.NEGATIVE))
                negCount++;
        }
        double p_pos = posCount / valueSet.size();
        double p_neg = negCount / valueSet.size();
        double p_neut = 1 - p_pos - p_neg;

        Map<NuancedSentiment, Double> classProbabilities = new HashMap<>();
        classProbabilities.put(NuancedSentiment.POSITIVE, p_pos);
        classProbabilities.put(NuancedSentiment.NEGATIVE, p_neg);
        classProbabilities.put(NuancedSentiment.NEUTRAL, p_neg);

        return classProbabilities;
    }

    @Override
    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Set<Path> pathSet = trainingSet.keySet();
        Tokenizer tokenizer = new Tokenizer();
        //Describes, for each word, the NUMBER of times the word appears
        //among all words in all documents, for each of positive and negative sentiments
        Map<String, Map<NuancedSentiment, Double>> result = new HashMap<>();
        for (Path p : pathSet) {
            NuancedSentiment currentPathSentiment = trainingSet.get(p);
            List<String> wordsInPath = tokenizer.tokenize(p);
            for (String word : wordsInPath) {
                //if word does not exist in the set, then add it
                if (!result.containsKey(word)) {
                    //Put positive and negative counts in and set to 0
                    Map<NuancedSentiment, Double> nuancedSentimentCount = new HashMap<>();
                    nuancedSentimentCount.put(NuancedSentiment.POSITIVE, 1.0);
                    nuancedSentimentCount.put(NuancedSentiment.NEGATIVE, 1.0);
                    nuancedSentimentCount.put(NuancedSentiment.NEUTRAL, 1.0);
                    result.put(word, nuancedSentimentCount);
                }

                //Now, the current word must exist in the map, so we increment the relevant count
                Map<NuancedSentiment, Double> currentWordSentimentMap = result.get(word);
                if (currentPathSentiment.equals(NuancedSentiment.POSITIVE))
                    currentWordSentimentMap.put(NuancedSentiment.POSITIVE, currentWordSentimentMap.get(NuancedSentiment.POSITIVE) + 1);
                else if (currentPathSentiment.equals(NuancedSentiment.NEGATIVE))
                    currentWordSentimentMap.put(NuancedSentiment.NEGATIVE, currentWordSentimentMap.get(NuancedSentiment.NEGATIVE) + 1);
                else
                    currentWordSentimentMap.put(NuancedSentiment.NEUTRAL, currentWordSentimentMap.get(NuancedSentiment.NEUTRAL) + 1);
            }

        }

        //Convert result such that it describes, for each word, the FRACTION of times the word appears
        //among all words in all documents, for each of positive and negative sentiments
        Set<String> wordsInLexicon = result.keySet();
        double totalPositiveCount = 0;
        double totalNegativeCount = 0;
        double totalNeutralCount = 0;
        for (String word : wordsInLexicon) {
            totalPositiveCount += result.get(word).get(NuancedSentiment.POSITIVE);
            totalNegativeCount += result.get(word).get(NuancedSentiment.NEGATIVE);
            totalNeutralCount += result.get(word).get(NuancedSentiment.NEUTRAL);

        }
        for (String word : wordsInLexicon) {
            Map<NuancedSentiment, Double> currentWorldSentimentMap2 = result.get(word);
            currentWorldSentimentMap2.put(NuancedSentiment.POSITIVE, Math.log(currentWorldSentimentMap2.get(NuancedSentiment.POSITIVE) / totalPositiveCount));
            currentWorldSentimentMap2.put(NuancedSentiment.NEGATIVE, Math.log(currentWorldSentimentMap2.get(NuancedSentiment.NEGATIVE) / totalNegativeCount));
            currentWorldSentimentMap2.put(NuancedSentiment.NEUTRAL, Math.log(currentWorldSentimentMap2.get(NuancedSentiment.NEUTRAL) / totalNeutralCount));
        }
        return result;
    }

    @Override
    public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities) throws IOException {
        Map<Path, NuancedSentiment> result = new HashMap<>();

        //For each path, calculate the predicted sentiment value by applying the formula
        Set<String> keySet = tokenLogProbs.keySet();
        for(Path p : testSet) {
            Tokenizer tokenizer = new Tokenizer();
            List<String> wordsInPath = tokenizer.tokenize(p);

            double positiveProbability = Math.log(classProbabilities.get(NuancedSentiment.POSITIVE));
            double negativeProbability = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE));
            double neutralProbability = Math.log(classProbabilities.get(NuancedSentiment.NEUTRAL));
            for (String word : wordsInPath) {
                if (tokenLogProbs.containsKey(word)) {
                    positiveProbability += tokenLogProbs.get(word).get(NuancedSentiment.POSITIVE);
                    negativeProbability+= tokenLogProbs.get(word).get(NuancedSentiment.NEGATIVE);
                    neutralProbability+= tokenLogProbs.get(word).get(NuancedSentiment.NEUTRAL);
                }
            }
            /*
                Check this is the way the probs should be classified. Priority: NEUTRAL > POSITIVE > NEGATIVE
             */
            if (neutralProbability >= positiveProbability && neutralProbability >= negativeProbability)
                result.put(p, NuancedSentiment.NEUTRAL);
            else if (positiveProbability >= neutralProbability && positiveProbability >= negativeProbability)
                result.put(p, NuancedSentiment.POSITIVE);
            else
                result.put(p, NuancedSentiment.NEGATIVE);
        }
        System.out.println("Naive Classifier predictions:\n" + result);
        return result;
    }

    @Override
    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments, Map<Path, NuancedSentiment> predictedSentiments) {
        double correctCount = 0;
        double incorrectCount = 0;

        for (Path path : trueSentiments.keySet()) {
            if (predictedSentiments.containsKey(path)) {
                if (predictedSentiments.get(path).equals(trueSentiments.get(path)))
                    correctCount++;
                else
                    incorrectCount++;
            }
        }

        double percentage = correctCount / (correctCount + incorrectCount);
        return percentage;
    }

    @Override
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {
        //create template for map to be returned. i.e. it must contain a map for each integer key 1,2,3,4, relating to each review
        //This maps each review (1-4) to the number of predictions for each sentiment
        Map<Integer, Map<Sentiment, Integer>> result = new HashMap<>();

        List<Map<Integer, Sentiment>> personalPredictions = new LinkedList<>(predictedSentiments);
        for (Map<Integer, Sentiment> currentPredictions : personalPredictions) {
            for (int reviewNumber : currentPredictions.keySet()) {
                Sentiment currentPredictedSentiment = currentPredictions.get(reviewNumber);
                //increment
                Map<Sentiment, Integer> counts = result.getOrDefault(reviewNumber, new HashMap<>());
                counts.put(currentPredictedSentiment, counts.getOrDefault(currentPredictedSentiment, 0) +1);
                result.put(reviewNumber, counts);
            }
        }
        return result;
    }

    //See task 6 notes to understand whats going on here
    @Override
    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
    //agreement table maps review number (1-4) -> Sentiment (+/-/.) -> count
        double N = agreementTable.size();

        //Calculate n(i,j)
        Map<Integer, Map<Sentiment, Double>> nij = new HashMap<>();
        for(Integer currReview : agreementTable.keySet()){
            Map<Sentiment, Double> values = new HashMap<>();

            values.put(Sentiment.POSITIVE, (double) agreementTable.get(currReview).getOrDefault(Sentiment.POSITIVE, 0));
            values.put(Sentiment.NEGATIVE, (double) agreementTable.get(currReview).getOrDefault(Sentiment.NEGATIVE, 0));

            nij.put(currReview, values);
        }
        System.out.println(nij);

        //Calculate n(i)
        Map<Integer, Double> ni = new HashMap<>();
        for(Integer review : agreementTable.keySet()){
            ni.put(review, nij.get(review).get(Sentiment.POSITIVE) + nij.get(review).get(Sentiment.NEGATIVE));
        }
        System.out.println(ni);

        //Calculate Pa
        double P_a = 0;
        for(Integer i : agreementTable.keySet()){
            double P_a_inner = 0;
            for(Sentiment j : nij.get(i).keySet()) {
                P_a_inner += nij.get(i).get(j)*(nij.get(i).get(j) - 1);
            }
            P_a += P_a_inner/(ni.get(i)*(ni.get(i) - 1));
        }

        P_a *= 1/N;

        //Calculate Pe
        double P_e = 0;
        for(Sentiment j : Sentiment.values()){
            double P_e_inner = 0;
            for(Integer review : agreementTable.keySet()){
                P_e_inner += nij.get(review).get(j)/ni.get(review);
            }
            P_e += (P_e_inner/N)*(P_e_inner/N);
        }

        double kappa = (P_a - P_e)/(1- P_e);
        return kappa;
    }

}
