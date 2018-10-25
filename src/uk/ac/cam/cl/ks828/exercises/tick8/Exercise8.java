package uk.ac.cam.cl.ks828.exercises.tick8;

import uk.ac.cam.cl.ks828.exercises.tick7.*;
import uk.ac.cam.cl.ks828.exercises.tick7.DiceType;
import uk.ac.cam.cl.ks828.exercises.tick7.HMMDataStore;
import uk.ac.cam.cl.ks828.exercises.tick7.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise8;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by KSarm on 02/03/2018.
 */
public class Exercise8 implements IExercise8 {

    @Override
    public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence) {
        //Stores: index of time step - current hidden state - maximised probability of current hidden state
        List<Map<DiceType, Double>> pathProbs = new ArrayList<>();
        //Stores: index of time step - current hidden state - the most probable previous hidden state
        List<Map<DiceType, DiceType>> helperVars = new ArrayList<>();

        Map<DiceType, Map<DiceType, Double>> transitions = model.getTransitionMatrix();
        Map<DiceType, Map<DiceRoll, Double>> emissions = model.getEmissionMatrix();

        for (int i = 0; i < observedSequence.size(); i++) {
            Map<DiceType, Double> newPathProb = new HashMap<>();
            Map<DiceType, DiceType> newHelpVar = new HashMap<>();
            DiceRoll currRoll = observedSequence.get(i);

            //Generate values for all possible current types
            for (DiceType currType : DiceType.values()) {
                //For first observation, there is no previous type, so the probability is only the emission prob
                if (i == 0) {
                    newPathProb.put(currType, Math.log(emissions.get(currType).get(currRoll)));
                    //Ensure that the time step corresponds with list index
                    newHelpVar.put(currType, null);
                }
                else {
                    //Deduce the previous state which outputs the greatest probability
                    DiceType maxPrevType = null;
                    double maxProbability = Double.NEGATIVE_INFINITY;
                    for (DiceType prevType : DiceType.values()) {
                        double currentProb = pathProbs.get(i-1).get(prevType)
                                        + Math.log(transitions.get(prevType).get(currType))
                                        + Math.log(emissions.get(currType).get(currRoll));
                        if (currentProb > maxProbability || maxPrevType == null) {
                            maxPrevType = prevType;
                            maxProbability = currentProb;
                        }
                    }
                    newPathProb.put(currType, maxProbability);
                    newHelpVar.put(currType, maxPrevType);
                }
            }
            pathProbs.add(newPathProb);
            helperVars.add(newHelpVar);
        }

        //Now backtrack to obtain ordering with maximum probability
        LinkedList<DiceType> result = new LinkedList<>();
        DiceType prevAddedType = DiceType.END;
        result.add(prevAddedType);
        for (int i = helperVars.size() -1; i > 0; i--) {
            result.addFirst(helperVars.get(i).get(prevAddedType));
            prevAddedType = helperVars.get(i).get(prevAddedType);
        }
        return result;
    }

    @Override
    public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model, List<Path> testFiles) throws IOException {
        List<HMMDataStore<DiceRoll, DiceType>> sequences = HMMDataStore.loadDiceFiles(testFiles);
        Map<List<DiceType>, List<DiceType>> result = new HashMap<>();
        for (HMMDataStore hmm : sequences) {
            List<DiceType> predictedSequence  = viterbi(model, hmm.observedSequence);
            result.put(hmm.hiddenSequence, predictedSequence);
        }
        return result;
    }

    @Override
    public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double cCorrect = 0;
        double cWeighted = 0;
        for (List<DiceType> correctSequence : true2PredictedMap.keySet()) {
            List<DiceType> predictedSequence = true2PredictedMap.getOrDefault(correctSequence, new ArrayList<>());
            for (int i = 0; i < correctSequence.size(); i++) {
                if (predictedSequence.get(i).equals(DiceType.WEIGHTED)) {
                    cWeighted++;
                    if (correctSequence.get(i).equals(predictedSequence.get(i)))
                        cCorrect++;
                }
            }
        }
        return cCorrect / cWeighted;
    }

    @Override
    public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double cCorrect = 0;
        double cWeighted = 0;
        for (List<DiceType> correctSequence : true2PredictedMap.keySet()) {
            List<DiceType> predictedSequence = true2PredictedMap.get(correctSequence);
            for (int i = 0; i < correctSequence.size(); i++) {
                if (correctSequence.get(i).equals(DiceType.WEIGHTED)) {
                    cWeighted++;
                    if (correctSequence.get(i).equals(predictedSequence.get(i)))
                        cCorrect++;
                }
            }
        }

        return cCorrect / cWeighted;
    }

    @Override
    public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);

        double result = 2 * (precision * recall)/(precision + recall);
        return result;
    }
}
