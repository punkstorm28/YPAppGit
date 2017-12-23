package in.youngpioneer.dps.notificationMessages;

import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.artifex.mupdfdemo.YoungPioneerMain;

/**
 * Created by shanu on 6/6/16.
 */
public class NotificationListActivity extends SingleFragmentActivity {
    public  NotificationListActivity contextStore;

    public NotificationListActivity() {
        contextStore = this;
    }

    protected Fragment createFragment() {
        return new NotificationListFragment();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(21)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }
}
