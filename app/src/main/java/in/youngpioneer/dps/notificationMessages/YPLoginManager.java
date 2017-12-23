package in.youngpioneer.dps.notificationMessages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import in.youngpioneer.dps.R;
import in.youngpioneer.dps.notificationMessages.FileIO.LoadStrings;

import static in.youngpioneer.dps.serverRelations.NotificationRefreshAndStore.getUserToken;


/**
 * A login screen that offers login via email/password.
 */
public class YPLoginManager  {

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    AlertDialog dialog;
    ProgressBar progressBar;
    Context localContext;
    TextView statusText;
    private String URLdirectory;

    public YPLoginManager(Context applicationContext) {
        localContext = applicationContext;
        URLdirectory = new LoadStrings(localContext).RuntimeStrings.getBaseURL()+"Messages/loginManager.php";
    }

    public void createLoginDialog() {
        dialog = new AlertDialog.Builder(localContext).show();
        dialog.setContentView(R.layout.activity_yplogin_manager);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        // Set up the login form.
        mEmailView = (EditText) dialog.findViewById(R.id.email);
        progressBar = (ProgressBar) dialog.findViewById(R.id.login_progress);

        mPasswordView = (EditText) dialog.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        statusText = (TextView) dialog.findViewById(R.id.login_status);
        Button mEmailSignInButton = (Button) dialog.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    private void updateUserToken(String currentUserToken) {
        Log.i("LOGIN","CurrentUserToken is "+currentUserToken);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(localContext);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putLong("userToken" ,Long.parseLong(currentUserToken));
        edit.apply();
    }

    private void updateUserName(String currentUserName) {
        Log.i("LOGIN","CurrentUserName is "+currentUserName);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(localContext);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("userName" ,currentUserName);
        edit.apply();
    }


    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(localContext.getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            new UserLoginTask(email,password).execute();
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mID;
        private final String mPassword;
        InputStream is;
        StringBuilder sb;

        UserLoginTask(String mID, String password) {
            this.mID = mID;
            this.mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            statusText.setText("Logging in...");

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                int timeout= 7000;
                HttpGet httpGet= null;
                HttpClient httpclient = new DefaultHttpClient();
                httpclient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout);
                httpGet = new HttpGet(URLdirectory+"?userID="+mID+"&password="+mPassword);
                HttpResponse response = httpclient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }
            catch(Exception e) {
                e.printStackTrace();
                cancel(true);
            }
            if(!isCancelled()) {
                String result = null;
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    sb = new StringBuilder();
                    sb.append(reader.readLine() + "\n");
                    String line = "0";

                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    is.close();
                    result = sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("LOGIN", result);
                return result;
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String messageList) {
            try {
                String result = messageList.substring(messageList.indexOf("<H4>") + 4, messageList.indexOf("</H4>"));
                if (!result.equals("-1")) {
                    String[] userInfo = result.split("_");
                    updateUserToken(userInfo[0]);
                    updateUserName(userInfo[1]);
                    Intent ListNotificationIntent = new Intent(dialog.getContext(), NotificationListActivity.class);
                    ListNotificationIntent.putExtra("refresh", true);
                    dialog.getContext().startActivity(ListNotificationIntent);
                    dialog.dismiss();
                } else {
                    statusText.setText("Username/Password Incorrect.");

                }
                progressBar.setVisibility(View.GONE);

            } catch (Exception e) {
                cancel(true);
            }
        }

        @Override
        protected void onCancelled() {
            progressBar.setVisibility(View.GONE);
            statusText.setText("Connection Error!");

        }
    }
}

