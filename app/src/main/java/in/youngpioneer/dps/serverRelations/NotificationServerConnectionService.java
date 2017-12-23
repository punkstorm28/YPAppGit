package in.youngpioneer.dps.serverRelations;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by vyomkeshjha on 6/12/17.
 */

public class NotificationServerConnectionService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    NotificationRefreshAndStore storeUpdater;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            while (true) {
                Log.i("FROM_PEECHE","HI BRO CREATED");
                storeUpdater.getMessagesFromServer();


/*

                if(NotificationRefreshAndStore.getNewMessageFlag()) {
                    displayNotification("New Notification Available!");
                }
*/

                try {
                    Thread.sleep(600000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            // stopSelf(msg.arg1);
        }
    }


    @Override
    public void onCreate() {


        storeUpdater = new NotificationRefreshAndStore(this);

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        Log.i("FROM_PEECHE","HI BRO CREATED");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("FROM_PEECHE","HI BRO STAERED");

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
