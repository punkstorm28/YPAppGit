package in.youngpioneer.dps.serverRelations;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.artifex.mupdfdemo.YoungPioneerMain;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import in.youngpioneer.dps.R;
import in.youngpioneer.dps.notificationMessages.DBHelper;
import in.youngpioneer.dps.notificationMessages.FileIO.LoadStrings;

/**
 * Created by vyomkeshjha on 30/07/16.
 * gets the key value stored on the server url and populates a hash-map based on it
 *
 */
public class NotificationRefreshAndStore
{
    private String LOG_TAG = "NotificationRef";
    private String URLdirectory;
    private DBHelper pushstore;
    private  Context localContext;
    private String imageBase;
    InputStream is;
    StringBuilder sb;

    public NotificationRefreshAndStore(Context ctx) {
        localContext = ctx;
        URLdirectory =new  LoadStrings(localContext).RuntimeStrings.getBaseURL()+"Messages/notification.php";
        imageBase = new  LoadStrings(localContext).RuntimeStrings.getBaseURL()+"msgimage/";
        pushstore = new DBHelper(ctx);

    }

    public static long getUserToken(Context ctx) {
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getLong("userToken",-1);
    }


    /*
* getStringsFromServer gets the data required for the hashmap
* the input arguments are:
*
* 1. String correspondingInput
*
* */
    @SuppressLint("StaticFieldLeak")
    public ArrayList<String> getMessagesFromServer()
    {
        final ArrayList<String> returnList=new ArrayList<String>();

        new AsyncTask<Void,String,String>(){
            @Override
            protected String doInBackground(Void... params) {
                try {
                    int timeout= 7000;
                    HttpGet httpGet= null;
                    HttpClient httpclient = new DefaultHttpClient();
                    httpclient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout);
                    httpGet = new HttpGet(URLdirectory+"?timestamp="+String.valueOf(pushstore.getLatestIndex())+"&usertoken="+getUserToken(localContext));
                    HttpResponse response = httpclient.execute(httpGet);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                }
                catch(Exception e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG,"HTTP exception caught :"+e.toString());
                }
                String result = null;
                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"),8);
                    sb = new StringBuilder();
                    sb.append(reader.readLine() + "\n");
                    String line="0";

                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    is.close();
                    result=sb.toString();
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                return result;
            }


            @Override
            protected void onPostExecute(String messageList) {
                super.onPostExecute(messageList);
                boolean FLUSH_FLAG = false;
                int flushTime=0;
                String result=null;
                try {
                    result = messageList.substring(messageList.indexOf("<H4>") + 4, messageList.indexOf("</H4>"));
                }
                catch (NullPointerException f)
                {
                    f.printStackTrace();
                }
                catch (StringIndexOutOfBoundsException e)
                {
                e.printStackTrace();
                }
                if(result!=null) {
                    List<String> messages = Arrays.asList(result.split("<->"));
                    Log.i(LOG_TAG, "message list is  " + messages);

                    for (String iterator : messages) {
                        if (iterator != null) {

                            String[] tokens = iterator.split("&&");

                            Log.i(LOG_TAG,"PRINTING tokens start\n");

                            for (String item:tokens) {
                                Log.i(LOG_TAG,item);
                            }

                            Log.i(LOG_TAG,"PRINTING tokens end\n");

                            if (tokens.length > 2) {

                                Log.i(LOG_TAG, "local timestamp =" + pushstore.getLatestIndex() + " server stamp " + tokens[1]);


                                    if(!getNewMessageFlag()) {
                                        displayNotification("New Message Available!");
                                        setNewMessageFlag(true);

                                    } if(tokens[0].contains("flush")){
                                        try {
                                            FLUSH_FLAG = true;
                                            flushTime = Integer.parseInt(tokens[0].split("_")[1]);
                                        } catch (ArrayIndexOutOfBoundsException e){
                                            e.printStackTrace();
                                        }
                                    } if (tokens.length == 3) {

                                        pushstore.insertProvider(tokens[0], tokens[2], Integer.parseInt(tokens[1]));
                                        Log.i(LOG_TAG,"size= "+tokens.length+ " message topic is = " + tokens[0] + " time = " + tokens[1] );

                                    } if (tokens.length == 4) {

                                        pushstore.insertProvider(tokens[0], tokens[2], Integer.parseInt(tokens[1]), imageBase+tokens[3]);
                                        Log.i(LOG_TAG, "size= "+tokens.length+" message topic= " + tokens[0] + " time = " + tokens[1] );

                                    } if (tokens.length == 5) {

                                    pushstore.insertProvider(tokens[0], tokens[2], Integer.parseInt(tokens[1]), imageBase+tokens[3],tokens[4]);
                                    Log.i(LOG_TAG, "size= "+tokens.length+" message topic = " + tokens[0] + ", time " + tokens[1] + " gotoURL = "+tokens[4]);

                                    }

                                    Log.i(LOG_TAG, "LAST TIMESTAMP is :" + pushstore.getLatestIndex());

                            }
                        }
                    }
                    if(FLUSH_FLAG) {
                        pushstore.deleteProviderAfterId(flushTime);
                    }
                }
            }
        }.execute();
        return returnList;
    }
    public void setNewMessageFlag(boolean flagState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(localContext);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("messageAvailable", flagState);
        edit.apply();
    }

    public  boolean getNewMessageFlag() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(localContext);
        return prefs.getBoolean("messageAvailable",false);
    }



    private void displayNotification(final String message) {
        Intent notificationIntent = new Intent(localContext, YoungPioneerMain.class);
        notificationIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int requestID = (int) System.currentTimeMillis();
        PendingIntent contentIntent = PendingIntent.getActivity(localContext, requestID, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Display a notification with an icon, message as content, and default sound. It also
        // opens the app when the notification is clicked.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(localContext).setSmallIcon(
                R.mipmap.push)
                .setContentTitle("Dps Chronicle")
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) localContext.getSystemService(
                Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, builder.build());
    }


}
