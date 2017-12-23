package in.youngpioneer.dps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import in.youngpioneer.dps.serverRelations.NotificationServerConnectionService;

/**
 * Created by vyomkeshjha on 11/06/16.
 */
public class BootStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "MAKING" +intent.getAction(), Toast.LENGTH_LONG).show();

        Log.i("FROM_PEECHE","RCVD"+intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent pollIntent = new Intent(context.getApplicationContext(), NotificationServerConnectionService.class);
            context.getApplicationContext().startService(pollIntent);
            Log.i("BOOT__","STARTING SERVICE POLLER");
        }
    }
}
