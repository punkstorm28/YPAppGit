package in.youngpioneer.dps.serverRelations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import in.youngpioneer.dps.notificationMessages.FileIO.LoadStrings;

/**
 * Created by vyomkeshjha on 29/06/16.
 */
public class UpdateManager {
    //String URLdirectory="http://youngpioneer.in/dpsbokaro/content/current/getData.php";
    //String URLdirectory= "http://unnayan.co.in/unnayan.co.in/vyomkesh/yp/CurrentIssue/getData.php";
    String URLdirectory;

    String CompulsoryTag="pdf";
    String whenNotFound="NotFound";
    private String LOG_TAG="FileLister";
    String latestRelease ="_latestRelease";
    String currentRelease="_currentRelease";
    String LatestReleaseOnServer=null;
    InputStream is;
    StringBuilder sb;
    String result;
    Context context;
    String update ="isUpdateAvailable";
    //TODO: use common initialisation of shared preferences
    public UpdateManager(Context ctx)
    {
        this.context=ctx;
        URLdirectory =new LoadStrings(ctx).RuntimeStrings.getBaseURL()+"CurrentIssue/getData.php";
        initialisePreferences();
        getFilesOnServer();
        Log.i(LOG_TAG,"File Lister Constructor called");

    }

    private void initialisePreferences()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        if(!prefs.contains(latestRelease))
        edit.putString(latestRelease,"def");
        if(!prefs.contains(currentRelease))
            edit.putString(currentRelease,"def");
        if(!prefs.contains(update))
            edit.putBoolean(update,false);
        edit.apply();
    }
    public String getLatestNameFromPreferences( )
    {
        //TODO: Try before updating or not?
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.contains(latestRelease))
            return prefs.getString(latestRelease,"empty");
        return null;
    }

    private void addLatestNameToPreferences(String latestName)
    {
        Log.i("UPDATE","added to preferences");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(latestRelease,latestName);
        edit.apply();
        String currentName=prefs.getString(currentRelease,"null");
        Log.i("UPDATE","currentVersion ="+currentName);
        Log.i("UPDATE","latestRelease ="+latestName);
        //Log.i("UPDATE","update avaibility is"+isUpdateAvailable());
        if(!currentName.equals(latestName)) {
            setUpdateAvailable(true);
            Log.i("UPDATE","update available");
        }
        edit.apply();

    }
    public boolean isFirstRun() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return !prefs.contains("runNumber");
    }

    public boolean isUpdateAvailable() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(update,false);
    }

  private void setUpdateAvailable(boolean isAvailable)
  {

      //TODO: check if this test is required
      if(!isFirstRun()) {
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
          SharedPreferences.Editor edit = prefs.edit();
          edit.putBoolean(update, isAvailable);
          edit.apply();
      }
  }


    @SuppressLint("StaticFieldLeak")
    private ArrayList<String> getFilesOnServer()
    {
        final ArrayList<String> returnList=new ArrayList<String>();

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    int timeout= 7000;
                    HttpPost httppost= null;
                    HttpClient httpclient = new DefaultHttpClient();
                    httpclient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout);
                    httppost = new HttpPost(URLdirectory);
                    //httppost.setEnstity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                }
                catch(Exception e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG,"HTTP exception caught :"+e.toString());
                }
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
                if(result!=null)
                    for (String getFile: result.split("-")){
                       // Log.d("FROMSERVER","String is:" + getFile);
                        returnList.add(getFile);
                       // Log.d("FROMSERVER","Array is:" + returnList);

                    }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                ArrayList<String> fileList=returnList;
                for(String iterator:fileList)
                {
                    if(iterator!=null && iterator.contains(CompulsoryTag))
                    {
                        addLatestNameToPreferences(iterator); //This also checks for a difference and switches the update available flag
                        Log.i("FROMSERVER","The current release is named "+iterator);
                        runAfterFilenameIsRetrieved();
                    }
                }
            }
        }.execute();
        return returnList;
    }
    public void runAfterFilenameIsRetrieved()
    {

    }
}

