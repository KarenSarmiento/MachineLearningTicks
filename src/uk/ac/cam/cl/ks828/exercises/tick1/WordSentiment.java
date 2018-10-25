package uk.ac.cam.cl.ks828.exercises.tick1;

/**
 * Created by KSarm on 19/01/2018.
 */
public class WordSentiment {
    private String word;
    private boolean strong;
    private Sentiment sentiment;

    public WordSentiment() {}

    public WordSentiment(String word, boolean strong, Sentiment sentiment) {
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

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }
}
