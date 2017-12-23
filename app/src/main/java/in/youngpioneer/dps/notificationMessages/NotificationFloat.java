package in.youngpioneer.dps.notificationMessages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.YoungPioneerMain;
import com.artifex.mupdfdemo.downloadManager.DownloadHandler;

import java.util.ArrayList;

import in.youngpioneer.dps.R;
import in.youngpioneer.dps.notificationMessages.DbDataMap;
import in.youngpioneer.dps.notificationMessages.NotificationListActivity;
import in.youngpioneer.dps.serverRelations.NotificationRefreshAndStore;

/**
 * Created by vyomkeshjha on 31/08/16.
 */
public class NotificationFloat {
    ArrayList<DbDataMap> map;
    public static int pushIndex=0;
    WebView web;
    final int width;
    final int height;
    AlertDialog notificationDialog;
    private Context localContext ;
    NotificationRefreshAndStore url;

    public NotificationFloat(Context ctx)
    {
        url = new NotificationRefreshAndStore(ctx);
        ConnectivityManager cm =
                (ConnectivityManager) YoungPioneerMain.mReference.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            url.getMessagesFromServer();
        }
        this.localContext=ctx;
        width = YoungPioneerMain.mReference.getWindowManager().getDefaultDisplay().getWidth();
        height = YoungPioneerMain.mReference.getWindowManager().getDefaultDisplay().getHeight();




      // showNotificationInPage();
    }
   public void showNotificationInList()
    {
        Intent ActivityIntent = new Intent(YoungPioneerMain.mReference, NotificationListActivity.class);
        localContext.startActivity(ActivityIntent);


    }
   public void showNotificationInPage()
    {
        Intent ActivityIntent = new Intent(YoungPioneerMain.mReference, SwipeDeckNotifications.class);
        ActivityIntent.putExtra("list_position",pushIndex);
        localContext.startActivity(ActivityIntent);

    }
}
