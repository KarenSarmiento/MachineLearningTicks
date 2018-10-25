package uk.ac.cam.cl.ks828.exercises.tick6;

import uk.ac.cam.cl.ks828.exercises.tick1.Sentiment;

/**
 * Created by KSarm on 05/02/2018.
 */
public class NuancedWordSentiment {
    private String word;
    private boolean strong;
    private NuancedSentiment sentiment;

    public NuancedWordSentiment() {}

    public NuancedWordSentiment(String word, boolean strong, NuancedSentiment sentiment) {
        this.word = word;
        this.strong = strong;
        this.sentiment = sentiment;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public boolean isStrong() {
        return strong;
    }

    public void setStrong(boolean strong) {
        this.strong = strong;
    }

    public NuancedSentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(NuancedSentiment sentiment) {
        this.sentiment = sentiment;
    }
}
