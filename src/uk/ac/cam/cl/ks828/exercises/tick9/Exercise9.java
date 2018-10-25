package uk.ac.cam.cl.ks828.exercises.tick9;

import uk.ac.cam.cl.ks828.exercises.tick7.DiceRoll;
import uk.ac.cam.cl.ks828.exercises.tick7.DiceType;
import uk.ac.cam.cl.ks828.exercises.tick7.HMMDataStore;
import uk.ac.cam.cl.ks828.exercises.tick7.HiddenMarkovModel;
import uk.ac.cam.cl.mlrd.exercises.markov_models.IExercise9;

import java.io.IOException;
import java.util.*;

/**
 * Created by KSarm on 05/03/2018.
 */
public class Exercise9 implements IExercise9 {
    @Override
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException {
        BioEmissionTransitionTables bioEmTrans = new BioEmissionTransitionTables();
        HiddenMarkovModel<AminoAcid, Feature> result = bioEmTrans.estimateHMM(sequencePairs);
        return result;
    }

    @Override
    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
        //Stores: index of time step - current hidden state - maximised probability of current hidden state
        List<Map<Feature, Double>> pathProbs = new ArrayList<>();
        //Stores: index of time step - current hidden state - the most probable previous hidden state
        List<Map<Feature, Feature>> helperVars = new ArrayList<>();

        Map<Feature, Map<Feature, Double>> transitions = model.getTransitionMatrix();
        Map<Feature, Map<AminoAcid, Double>> emissions = model.getEmissionMatrix();

        for (int i = 0; i < observedSequence.size(); i++) {
            Map<Feature, Double> newPathProb = new HashMap<>();
            Map<Feature, Feature> newHelpVar = new HashMap<>();
            AminoAcid currAcid = observedSequence.get(i);

            //Generate values for all possible current types
            for (Feature currFeature : Feature.values()) {
                //For first observation, there is no previous type, so the probability is only the emission prob
                if (i == 0) {
                    newPathProb.put(currFeature, Math.log(emissions.get(currFeature).get(currAcid)));
                    //Ensure that the time step corresponds with list index
                    newHelpVar.put(currFeature, null);
                }
                else {
                    //Deduce the previous state which outputs the greatest probability
                    Feature maxPrevFeature = null;
                    double maxProbability = Double.NEGATIVE_INFINITY;
                    for (Feature prevFeature : Feature.values()) {
                        double currentProb = pathProbs.get(i-1).get(prevFeature)
                                + Math.log(transitions.get(prevFeature).get(currFeature))
                                + Math.log(emissions.get(currFeature).get(currAcid));
                        if (currentProb > maxProbability || maxPrevFeature == null) {
                            maxPrevFeature = prevFeature;
                            maxProbability = currentProb;
                        }
                    }
                    newPathProb.put(currFeature, maxProbability);
                    newHelpVar.put(currFeature, maxPrevFeature);
                }
            }
            pathProbs.add(newPathProb);
            helperVars.add(newHelpVar);
        }

        //Now backtrack to obtain ordering with maximum probability
        LinkedList<Feature> result = new LinkedList<>();
        Feature prevAddedType = Feature.END;
        result.add(prevAddedType);
        for (int i = helperVars.size() -1; i > 0; i--) {
            result.addFirst(helperVars.get(i).get(prevAddedType));
            prevAddedType = helperVars.get(i).get(prevAddedType);
        }
        return result;
    }

    @Override
    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
        Map<List<Feature>, List<Feature>> result = new HashMap<>();
        for (HMMDataStore hmm : testSequencePairs) {
            List<Feature> predictedSequence  = viterbi(model, hmm.observedSequence);
            result.put(hmm.hiddenSequence, predictedSequence);
        }
        return result;
    }

    @Override
    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double cCorrect = 0;
        double cWeighted = 0;
        for (List<Feature> correctSequence : true2PredictedMap.keySet()) {
            List<Feature> predictedSequence = true2PredictedMap.getOrDefault(correctSequence, new ArrayList<>());
            for (int i = 0; i < correctSequence.size(); i++) {
                if (predictedSequence.get(i).equals(Feature.MEMBRANE)) {
                    cWeighted++;
                    if (correctSequence.get(i).equals(predictedSequence.get(i)))
                        cCorrect++;
                }
            }
        }
        return cCorrect / cWeighted;
    }

    @Override
    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double cCorrect = 0;
        double cMembrane = 0;
        for (List<Feature> correctSequence : true2PredictedMap.keySet()) {
            for (int i = 0; i < correctSequence.size(); i++) {
                List<Feature> predictedSequence = true2PredictedMap.get(correctSequence);
                if (correctSequence.get(i).equals(Feature.MEMBRANE)) {
                    cMembrane++;
                    if (correctSequence.get(i).equals(predictedSequence.get(i)))
                        cCorrect++;
                }
            }
        }

        return cCorrect / cMembrane;
    }

    @Override
    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);

        double result = 2 * (precision * recall)/(precision + recall);
        return result;
    }
}
