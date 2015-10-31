package corp.seedling.news.wordy;

/**
 * Created by Ankur Nigam on 17-09-2015.
 */
public class Meaning {

    private String type;
    private String meaning;

    public Meaning(String type, String meaning) {
        this.type = type;
        this.meaning = meaning;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }
}
