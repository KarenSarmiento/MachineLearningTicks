package uk.ac.cam.cl.ks828.exercises.tick7;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by KSarm on 22/02/2018.
 */
public class Exercise7 implements IExercise7{

    private Map<DiceType, Map<DiceType, Double>> transitionTable = new HashMap<>();
    private Map<DiceType, Map<DiceRoll, Double>> emissionsTable = new HashMap<>();

    /*
     * Loads the sequences of visible and hidden states from the sequence files
     * (visible dice rolls on first line and hidden dice types on second) and uses
     * them to estimate the parameters of the Hidden Markov Model that generated
     * them.
     */
    @Override
    public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {
        List<HMMDataStore<DiceRoll, DiceType>> sequences = HMMDataStore.loadDiceFiles(sequenceFiles);

        //This is used to count the *total* of times a dice is rolled from a particular state/type
        Map<DiceType, Integer> fromTypeCount = new HashMap<>();

        //Add 0s for end state since this is never actually encountered
        addZerosForEndState();

        //Start counting
        for (HMMDataStore<DiceRoll, DiceType> currSequence : sequences) {
            DiceType prevType = currSequence.hiddenSequence.get(0);

            for (int i = 0 ; i < currSequence.observedSequence.size(); i++) {
                DiceRoll currRoll = currSequence.observedSequence.get(i);
                DiceType currType = currSequence.hiddenSequence.get(i);

                /* Counts for transition table */
                //Increment count specific: FROM I TO J
                if (i > 0) {
                    Map<DiceType, Double> thisTransMap = transitionTable.getOrDefault(prevType, new HashMap<>());
                    thisTransMap.put(currType, thisTransMap.getOrDefault(currType, 0.0) +1.0);
                    transitionTable.put(prevType, thisTransMap);
                }

                /* Counts for emissions table */
                Map<DiceRoll, Double> thisEmsMap = emissionsTable.getOrDefault(currType, new HashMap<>());
                thisEmsMap.put(currRoll, thisEmsMap.getOrDefault(currRoll, 0.0) +1.0);
                emissionsTable.put(currType, thisEmsMap);

                /* Increment totals count: FROM J */
                fromTypeCount.put(currType, fromTypeCount.getOrDefault(currType, 0) +1);

                //Set prev values
                prevType = currType;
            }
        }
        System.out.println("trans : " + transitionTable.toString());
        System.out.println("ot: " + transitionTable.toString());

        // Calculate transition probability by dividing by total
        for (DiceType fromType : transitionTable.keySet()) {
            Map<DiceType, Double> currInnerMap = transitionTable.get(fromType);
            for (DiceType toType : currInnerMap.keySet()) {
                if (fromTypeCount.containsKey(fromType))
                    currInnerMap.put(toType, currInnerMap.get(toType)/ (double) fromTypeCount.get(fromType));
                else
                    currInnerMap.put(toType, 0.0);
            }
        }

        // Calculate emission probability by dividing by total
        for (DiceType fromType : emissionsTable.keySet()) {
            Map<DiceRoll, Double> currInnerMap = emissionsTable.get(fromType);
            for (DiceRoll toRoll : currInnerMap.keySet()) {
                if (fromTypeCount.containsKey(fromType)) {
                    currInnerMap.put(toRoll, currInnerMap.get(toRoll)/ (double) fromTypeCount.get(fromType));
                }
                else {
                    currInnerMap.put(toRoll, 0.0);
                }
            }
        }

        //Add zeros if there is no count
        addZerosForStatesWithoutCountsInTransitions();
        addZerosForStatesWithoutCountsInEmissions();

        return new HiddenMarkovModel<>(transitionTable, emissionsTable);
    }

    private void addZerosForEndState() {
        Map<DiceType, Double> endStateTypeMap = new HashMap<>();
        Map<DiceRoll, Double> endStateRollMap = new HashMap<>();
        for (DiceType type : DiceType.values()) {
            endStateTypeMap.put(type, 0.0);
        }
        for (DiceRoll roll : DiceRoll.values()) {
            endStateRollMap.put(roll, 0.0);
        }
        transitionTable.put(DiceType.END, endStateTypeMap);
        emissionsTable.put(DiceType.END, endStateRollMap);
    }

    //Nothing ever goes from some state to a start state so within hashmap, all S values should be 0
    private void addZerosForStatesWithoutCountsInTransitions() {
        for (DiceType fromType : transitionTable.keySet()) {
            Map<DiceType, Double> innerMap = transitionTable.get(fromType);
            for (DiceType toType : DiceType.values()) {
                if (!innerMap.containsKey(toType))
                    innerMap.put(toType, 0.0);
            }
        }
    }

    private void addZerosForStatesWithoutCountsInEmissions() {
        for (DiceType fromType : emissionsTable.keySet()) {
            Map<DiceRoll, Double> innerMap = emissionsTable.get(fromType);
            for (DiceRoll toRoll : DiceRoll.values()) {
                if (!innerMap.containsKey(toRoll))
                    innerMap.put(toRoll, 0.0);
            }
        }
    }
}
