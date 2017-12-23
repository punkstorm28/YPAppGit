package in.youngpioneer.dps.notificationMessages;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.artifex.mupdfdemo.YoungPioneerMain;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import in.youngpioneer.dps.R;

/**
 * Created by Vyomkesh on 6/6/16.
 */
public class NotificationListFragment extends ListFragment {

    ArrayList<DbDataMap> map;
    ArrayList<String> topImageUrls;
    ImageView marqueeView;
    TextView marqueeHeader;
    public int currentimageindex=0;
    TextView studentName;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("");

        studentName = (TextView)getActivity().findViewById(R.id.studentNameHeader);
        studentName.setText(getStudentName());

        marqueeView = (ImageView) getActivity().findViewById(R.id.marqueeImage);
        marqueeHeader = (TextView)getActivity().findViewById(R.id.marqueeTitle);


        map=new DBHelper(getContext()).getDataFromDB();

        MessageAdapter adapter = new MessageAdapter(map);
        setListAdapter(adapter);

        topImageUrls = getTopThreeImageUrls();
        createMarqueeInImageView();
    }

    public class MessageAdapter extends ArrayAdapter<DbDataMap> {
        public MessageAdapter(ArrayList<DbDataMap> Messages) {
            super(getActivity(), 0, Messages);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_notifications,null);

            TextView title_text = (TextView) convertView.findViewById(R.id.notificationTitle);
            title_text.setText(map.get(position).getTitle());

            TextView message_text = (TextView) convertView.findViewById(R.id.notification_message);
            message_text.setText(Html.fromHtml(map.get(position).getMessage()));

            ImageView marqueeImage = (ImageView)convertView.findViewById(R.id.listImage);

            if(!map.get(position).getImageUrl().equals(null))
            Picasso.with(this.getContext()).load(map.get(position).getImageUrl()).fit().centerCrop().into(marqueeImage);



            return convertView;
        }
    }
    public void onListItemClick (ListView l, View v, int position, long id) {
        NotificationFloat.pushIndex=position;
        NotificationFloat floatFromHere= new NotificationFloat(getContext());
        floatFromHere.showNotificationInPage();
        //NotificationListActivity.contextStore.finish();
    }

    public void onResume() {
        super.onResume();
        ((MessageAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private void createMarqueeInImageView() {
        final Handler mHandler = new Handler();
        // Create runnable for posting
        final Runnable mUpdateResults = new Runnable() {
            public void run() {

                AnimateandSlideShow();

            }
        };
        int delay = 1000; // delay for 1 sec.
        int period = 8000; // repeat every 4 sec.
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mHandler.post(mUpdateResults);
            }
        }, delay, period);
    }

    private void AnimateandSlideShow() {
        if(currentimageindex<topImageUrls.size()) {
            String fullUntokenizedStore = topImageUrls.get(currentimageindex);
            String[] URLtokens = fullUntokenizedStore.split("<-->");
            Picasso.with(this.getContext()).load(URLtokens[0]).fit().centerCrop().into(marqueeView);
            marqueeHeader.setText(map.get(Integer.parseInt(URLtokens[1])).getTitle());
            currentimageindex++;
        } else {
            currentimageindex = 0;
        }

    }


    private ArrayList<String> getTopThreeImageUrls() {
        ArrayList<String> returnList = new ArrayList();
        for(DbDataMap currentMap : map) {
            Log.i("PRINTER","URL= "+currentMap.getImageUrl());
            if(!currentMap.getImageUrl().equals("null")) {
                returnList.add(currentMap.getImageUrl() + "<-->" + map.indexOf(currentMap));
            }
                if(returnList.size()==3)
                    break;
        }
        return returnList;
    }

    String getStudentName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString("userName","");
    }


}
