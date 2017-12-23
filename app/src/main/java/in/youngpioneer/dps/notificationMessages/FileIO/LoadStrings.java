package in.youngpioneer.dps.notificationMessages.FileIO;

import android.content.Context;

import com.artifex.mupdfdemo.YoungPioneerMain;

import in.youngpioneer.dps.R;

/**
 * Created by vyomkeshjha on 24/08/16.
 */
public class LoadStrings {
    public StringTemplate RuntimeStrings;
    private Context local;
    public LoadStrings(Context ctx)
    {
        local = ctx;
        RuntimeStrings = new StringTemplate();
        initialize();
    }
    private void initialize()
    {
        String BaseURL=local.getString(R.string.DownloadBaseURL);
        RuntimeStrings.setBaseURL(BaseURL);
        String InfoMessage=local.getString(R.string.infoMesage);
        RuntimeStrings.setInfoMessage(InfoMessage);
        String ReviewURL=local.getString(R.string.ReviewURL);
        RuntimeStrings.setReviewURL(ReviewURL);
        String SiteForWebView=local.getString(R.string.WebviewSiteURL);
        RuntimeStrings.setSiteForWebView(SiteForWebView);
    }
}
