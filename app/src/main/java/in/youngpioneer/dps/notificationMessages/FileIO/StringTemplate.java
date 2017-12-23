package in.youngpioneer.dps.notificationMessages.FileIO;

/**
 * Created by vyomkeshjha on 24/08/16.
 */
public class StringTemplate {
    private String baseURL =null;
    private String siteForWebView=null;
    private String InfoMessage=null;
    private String reviewURL =null;

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getSiteForWebView() {
        return siteForWebView;
    }

    public void setSiteForWebView(String siteForWebView) {
        this.siteForWebView = siteForWebView;
    }

    public String getInfoMessage() {
        return InfoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        InfoMessage = infoMessage;
    }

    public String getReviewURL() {
        return reviewURL;
    }

    public void setReviewURL(String reviewURL) {
        this.reviewURL = reviewURL;
    }
}
