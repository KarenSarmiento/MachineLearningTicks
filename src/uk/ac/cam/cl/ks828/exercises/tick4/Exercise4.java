package uk.ac.cam.cl.ks828.exercises.tick4;

import uk.ac.cam.cl.ks828.exercises.tick1.Exercise1;
import uk.ac.cam.cl.ks828.exercises.tick1.Sentiment;
import uk.ac.cam.cl.ks828.exercises.tick1.Tokenizer;
import uk.ac.cam.cl.ks828.exercises.tick1.WordSentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by KSarm on 31/01/2018.
 */
public class Exercise4 implements IExercise4 {
    private Exercise1 ex1;

    @Override
    public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        //I already did this in exercise1
        if (ex1 == null) {
            ex1 = new Exercise1();
        }
        return ex1.improvedClassifier(testSet, lexiconFile);
    }

    /*
        Count all cases when system 1 is better than system 2, when system 2 is better than system 1,
        and when they are the same.
     */
    @Override
    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA, Map<Path, Sentiment> classificationB) {
        int cPlus = 0;
        int cMinus = 0;
        int cNull = 0;

        for (Path path : actualSentiments.keySet()) {
            Sentiment trueSentiment = actualSentiments.get(path);
            if (classificationA.get(path).equals(classificationB.get(path)))
                cNull++;
            else if (classificationA.get(path).equals(trueSentiment))
                cPlus++;
            else
                cMinus++;
        }

        int n = 2*((int)Math.ceil(cNull/2.0)) + cPlus + cMinus;
        int k = ((int)Math.ceil(cNull/2.0)) + Math.min(cPlus, cMinus);
        double q = 0.5;

        //BigInteger probability = new BigInteger("0");
        BigDecimal probability = new BigDecimal(0.0);
        for (int i = 0; i <= k; i++) {
            BigInteger nChooseI = factorial(n).divide(factorial(i).multiply(factorial(n-i)));
            BigDecimal prob1 = new BigDecimal(Math.pow(q, i));
            BigDecimal prob2 = new BigDecimal(Math.pow(1.0-q, n-i));

            probability = probability.add(prob1.multiply(prob2).multiply(new BigDecimal(nChooseI)));
        }
        probability = probability.multiply(new BigDecimal(2.0));
        return probability.doubleValue();
    }

    private BigInteger factorial(int n) {
        if (n == 0)
            return new BigInteger("1");
        else {
            BigInteger product = new BigInteger("1");
            for (int i = 2; i <= n; i++) {
                product = product.multiply(new BigInteger(String.valueOf(i)));
            }
            return product;
        }
    }

}