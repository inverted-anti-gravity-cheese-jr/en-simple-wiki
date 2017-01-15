package pl.pg.gda.eti.kio.esc.data;

import java.util.Comparator;

/**
 * @author Wojciech Stanisławski
 * @since 08.11.2016
 */
public class WordFeature implements Comparable<WordFeature> {
    private String word;
    private String simpleId;
    private String enId;

    public WordFeature(String word, String simpleId, String enId) {
        this.word = word;
        this.simpleId = simpleId;
        this.enId = enId;
    }

    @Override
    public String toString() {
        return "(" + word + " => (" + simpleId + "," + enId + ")";
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSimpleId() {
        return simpleId;
    }

    public void setSimpleId(String simpleId) {
        this.simpleId = simpleId;
    }

    public String getEnId() {
        return enId;
    }

    public void setEnId(String enId) {
        this.enId = enId;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            WordFeature feature = (WordFeature) obj;
            return word.equals(feature.word);
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public int compareTo(WordFeature o) {
        return this.word.compareTo(o.word);
    }
}
