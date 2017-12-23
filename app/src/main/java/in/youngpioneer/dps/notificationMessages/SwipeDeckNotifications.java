package in.youngpioneer.dps.notificationMessages;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artifex.mupdfdemo.AsyncTask;
import com.artifex.mupdfdemo.YoungPioneerMain;
import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import in.youngpioneer.dps.R;

public class SwipeDeckNotifications extends Activity {

    private static final String TAG = "SwipeDeckNotifications";
    private SwipeDeck cardStack;
    private Context context = this;
    private SwipeDeckAdapter adapter;
    private ArrayList<DbDataMap> dataMap;
    private CheckBox dragCheckbox;
    private int initialPosition;
    ImageButton backToMagButton;
    ImageButton refreshDeck;
    TextView studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipedeck);


        backToMagButton = (ImageButton)findViewById(R.id.magazineButtonSwipe);
        backToMagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToPdfIntent = new Intent(SwipeDeckNotifications.this,YoungPioneerMain.class);
                startActivity(backToPdfIntent);
            }
        });


        refreshDeck = (ImageButton)findViewById(R.id.refreshButton);

        refreshDeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unswipeAllCards();
            }
        });

        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        dataMap = new DBHelper(this).getDataFromDB();

        initialPosition = getIntent().getIntExtra("list_position",0);

        adapter = new SwipeDeckAdapter(dataMap, this);

        if(cardStack != null){
            cardStack.setAdapter(adapter);
            cardStack.setAdapterIndex(initialPosition);
        }
        cardStack.setCallback(new SwipeDeck.SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long stableId) {
                Log.i("SwipeDeckNotifications", "card was swiped left, position in adapter: " + stableId);
            }

            @Override
            public void cardSwipedRight(long stableId) {

            }


        });


        ImageButton addCard = (ImageButton) findViewById(R.id.button_center);
        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dataMap.add("a sample string.");
//                adapter.notifyDataSetChanged();
                cardStack.unSwipeCard();
            }
        });

    }

    void unswipeAllCards() {
        if(dataMap!=null) {

                new AsyncTask<Void,Void,Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        for(DbDataMap map :dataMap) {
                            cardStack.unSwipeCard();
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.execute();


        }
    }

    public class SwipeDeckAdapter extends BaseAdapter {

        private List<DbDataMap> data;
        private Context context;
        DbDataMap tempMap;

        public SwipeDeckAdapter(List<DbDataMap> data, Context context) {
            this.data = data;
            this.context = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = getLayoutInflater();
                v = inflater.inflate(R.layout.notification_card, parent, false);
            }
            try {
                tempMap = data.get(position);
                ImageView imageView = (ImageView) v.findViewById(R.id.offer_image);
                if(tempMap.getImageUrl().equals("null"))
                {
                    RelativeLayout localLayout = (RelativeLayout)v.findViewById(R.id.imageHolder);
                    localLayout.removeAllViews();
                }
                Picasso.with(context).load(tempMap.getImageUrl()).fit().centerCrop().into(imageView);
                Log.i("SWIPE","title = "+tempMap.getTitle()+" url = "+tempMap.getImageRedirectUrl());

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!(data.get(position)==null)) {
                            Log.i("TEMPMAP", "=" + data.get(position).getImageRedirectUrl());
                            if (!(data.get(position).getImageRedirectUrl().equals("null"))) {
                                Uri webpage = Uri.parse(data.get(position).getImageRedirectUrl());
                                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }


                            }
                        }
                    }
                });

                TextView textView = (TextView) v.findViewById(R.id.card_title);
                textView.setText(Html.fromHtml(tempMap.getTitle()));

                TextView messageView = (TextView) v.findViewById(R.id.message);
                messageView.setText(Html.fromHtml(tempMap.getMessage()));
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("Layer type: ", Integer.toString(v.getLayerType()));
                    Log.i("Hardware Accel type:", Integer.toString(View.LAYER_TYPE_HARDWARE));
                    /*Intent i = new Intent(v.getContext(), BlankActivity.class);
                    v.getContext().startActivity(i);*/
                }
            });
            return v;
        }
    }
}