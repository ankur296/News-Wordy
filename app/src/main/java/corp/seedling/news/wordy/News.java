package corp.seedling.news.wordy;

/**
 * Created by Ankur Nigam on 17-09-2015.
 */
public class News {

    private String webUrl;
    private String headline;
    private String snippet;

    public News(String webUrl, String headline, String snippet) {
        this.webUrl = webUrl;
        this.headline = headline;
        this.snippet = snippet;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
