package uk.ac.cam.cl.ks828.exercises.tick3;

/**
 * Created by KSarm on 26/01/2018.
 */
public class WordFrequency {
    private String word;
    private int frequency;
    private int rank;

    public WordFrequency(String word, int frequency, int rank) {
        this.word = word;
        this.frequency = frequency;
        this.rank = rank;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "WordFrequency{" +
                "word='" + word + '\'' +
                ", frequency=" + frequency +
                ", rank=" + rank +
                '}';
    }
}
