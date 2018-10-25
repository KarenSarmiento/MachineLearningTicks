package uk.ac.cam.cl.ks828.exercises.tick1;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by KSarm on 19/01/2018.
 */
public class Exercise1 implements IExercise1 {

    public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<String, Sentiment> lexicon = lexiconFileToMap(lexiconFile);
        Map<Path, Sentiment> result = new HashMap<>();
        Tokenizer tokenizer = new Tokenizer();

        for (Path p : testSet) {
            List<String> words = tokenizer.tokenize(p);
            int positiveCount = 0;
            int negativeCount = 0;
            for (String word: words) {
                word = word.toLowerCase();

                if (lexicon.containsKey(word)) {
                    if(lexicon.get(word).equals(Sentiment.POSITIVE))
                        positiveCount++;
                    else
                        negativeCount++;
                }
            }
            if (positiveCount >= negativeCount)
                result.put(p, Sentiment.POSITIVE);
            else
                result.put(p, Sentiment.NEGATIVE);
        }

        return result;
    }

    private Map<String, Sentiment> lexiconFileToMap(Path lexiconFile) {
        Map<String, Sentiment> lexicon = new HashMap<>();
        try {
            //Example: "word=astounding intensity=strong polarity=positive"
            BufferedReader br = new BufferedReader(new FileReader(lexiconFile.toFile()));

            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(" ");
                String[] wordComp = splitLine[0].split("=");
                String[] polarityComp = splitLine[2].split("=");
                if (polarityComp[1].equals("positive"))
                    lexicon.put(wordComp[1], Sentiment.POSITIVE);
                else if (polarityComp[1].equals("negative"))
                    lexicon.put(wordComp[1], Sentiment.NEGATIVE);
            }

            return lexicon;
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("lexiconFileToStringFailed");
        }
        return lexicon;
    }


    public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<String, WordSentiment>lexicon = lexiconFileToMapImproved(lexiconFile);
        Map<Path, Sentiment> result = new HashMap<>();
        Tokenizer tokenizer = new Tokenizer();

        for (Path p : testSet) {
            List<String> words = tokenizer.tokenize(p);
            int likingCount = 0;
            //was initially 10
            int weighting = 2;
            for (String word: words) {
                word = word.toLowerCase();

                if (lexicon.containsKey(word)) {
                    if (lexicon.get(word).isStrong()) {
                        if(lexicon.get(word).getSentiment().equals(Sentiment.POSITIVE))
                            likingCount += weighting;
                        else
                            likingCount -= weighting;
                    }
                    else {
                        if(lexicon.get(word).getSentiment().equals(Sentiment.POSITIVE))
                            likingCount += 1;
                        else
                            likingCount -= 1;
                    }
                }
            }
            //was initially (likingCount >= 100)
            if (likingCount >= 10)
                result.put(p, Sentiment.POSITIVE);
            else
                result.put(p, Sentiment.NEGATIVE);
        }

        return result;
    }

    private Map<String, WordSentiment> lexiconFileToMapImproved(Path lexiconFile) {
        Map<String, WordSentiment>  lexicon = new HashMap<>();
        try {
            //Example: "word=astounding intensity=strong polarity=positive"
            BufferedReader br = new BufferedReader(new FileReader(lexiconFile.toFile()));

            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(" ");
                String[] wordComp = splitLine[0].split("=");
                String[] intensityComp = splitLine[1].split("=");
                String[] polarityComp = splitLine[2].split("=");
                WordSentiment word;
                if (polarityComp[1].equals("positive")) {
                    if (intensityComp[1].equals("strong"))
                        word = new WordSentiment(wordComp[1], true, Sentiment.POSITIVE);
                    else
                        word = new WordSentiment(wordComp[1], false, Sentiment.POSITIVE);
                }
                else {
                    if (intensityComp[1].equals("strong"))
                        word = new WordSentiment(wordComp[1], true, Sentiment.NEGATIVE);
                    else
                        word = new WordSentiment(wordComp[1], false, Sentiment.NEGATIVE);
                }
                lexicon.put(wordComp[1], word);
            }

            return lexicon;
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("lexiconFileToStringFailed");
        }
        return lexicon;
    }

    public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {
        List<Sentiment> trueList = new LinkedList<>(trueSentiments.values());
        List<Sentiment> predictedList = new LinkedList<>(predictedSentiments.values());

        double correctCount = 0;
        double incorrectCount = 0;

        for(int i = 0; i < trueList.size(); i++) {
            if (trueList.get(i).equals(predictedList.get(i)))
                correctCount++;
            else
                incorrectCount++;
        }
        double percentage = correctCount / (correctCount + incorrectCount);

        return percentage;
    }
}
