package in.youngpioneer.dps.notificationMessages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.YoungPioneerMain;

import in.youngpioneer.dps.R;
import in.youngpioneer.dps.serverRelations.NotificationRefreshAndStore;

/**
 * Created by shanu on 6/6/16.
 */
public abstract class SingleFragmentActivity extends FragmentActivity {
    protected abstract Fragment createFragment();
    public ImageButton messageRefresh;
    public ImageButton backToMagButton;
    public ImageButton logoutButton;
    Fragment notificationListFragment;
    android.support.v4.app.FragmentManager fm;
    AlertDialog loadingDialog;
    Context localContext;
    TextView noNotifications;
    DBHelper localHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_list_background);
        localContext = getApplicationContext();
        localHelper = new DBHelper(localContext);

        messageRefresh =(ImageButton)findViewById(R.id.refreshButtonNot);
        backToMagButton =(ImageButton)findViewById(R.id.magazineButton);
        noNotifications = (TextView) findViewById(R.id.noNotifications);
        logoutButton = (ImageButton) findViewById(R.id.logoutButton);

        prepareButtons();

        backToMagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleFragmentActivity.super.onBackPressed();
            }
        });


        messageRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRefreshOfNotifications();
            }
        });
        startWithRefresh();
        attachRefreshedFragment();

    }

    void prepareButtons() {

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localHelper.deleteProviderAfterId(0);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(localContext);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("latestIndex");
                editor.remove("userToken");
                editor.remove("userName");
                editor.apply();
                SingleFragmentActivity.super.onBackPressed();
                Toast.makeText(localContext, "Logged Out...", Toast.LENGTH_SHORT).show();



            }
        });

    }

    void startWithRefresh() {
        Intent intent = getIntent();
        if(intent!=null) {
            if (intent.getBooleanExtra("refresh", false)) {
                doRefreshOfNotifications();
            }
        }
    }

    void doRefreshOfNotifications() {
        YoungPioneerMain.mReference.refreshNotificationMessages();
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                LayoutInflater inflater = getLayoutInflater();
                ProgressBar bar = (ProgressBar ) inflater.inflate(R.layout.progress_act, null);
                bar.setVisibility(View.VISIBLE);
                loadingDialog = new AlertDialog.Builder(SingleFragmentActivity.this).setCancelable(false).setView(bar).show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(loadingDialog!=null)
                    loadingDialog.dismiss();

                if(getNewMessageFlag()){
                    attachRefreshedFragment();
                    setNewMessageFlag(false);
                }
            }
        }.execute();
    }

    public boolean getNewMessageFlag() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("messageAvailable",false);
    }

    public void setNewMessageFlag(boolean flagState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("messageAvailable", flagState);
        edit.apply();
    }

    void attachRefreshedFragment() {

        if (localHelper.getAllMessages().size() != 0) {
            noNotifications.setVisibility(View.GONE);
        }

        fm = getSupportFragmentManager();
        notificationListFragment = fm.findFragmentById(R.id.notificationFragContainer);
        if(notificationListFragment==null)
        notificationListFragment = createFragment();
        try {
            fm.beginTransaction().remove(notificationListFragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        notificationListFragment = createFragment();

        fm.beginTransaction().add(R.id.notificationFragContainer, notificationListFragment).commit();
    }


}