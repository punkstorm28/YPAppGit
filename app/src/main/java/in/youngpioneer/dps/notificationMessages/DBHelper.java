package in.youngpioneer.dps.notificationMessages;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by vyomkeshjha on 23/04/16.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Notifications.db";
    public static final String NotificationsTable = "Notifications";
    public static final String Message = "Message";
    public static final String Timestamp = "Timestamp";
    Context localContext;

//The table name is Notifications

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.localContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table Notifications " +
                        "(id integer primary key, Title text,Timestamp int,Message text, ImageURL text, ImageClickRedirect text)"
        );
        //Added Image URL here
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS Notifications");
        onCreate(db);
    }

    public boolean insertProvider(String Title, String Message, int Timestamp) {

        if (Integer.parseInt(getLatestIndex())<Timestamp)
            updateLatestIndexOnAvailable(Timestamp);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Title", Title);
        contentValues.put("Timestamp", Timestamp);
        contentValues.put("Message", Message);
        contentValues.put("ImageURL", "null");
        contentValues.put("ImageClickRedirect","null");


        db.insert("Notifications", null, contentValues);
        return true;
    }

    // copy method, to be used when an imageURL is present
    public boolean insertProvider(String Title, String Message, int Timestamp, String ImageURL) {
        if (Integer.parseInt(getLatestIndex())<Timestamp)
            updateLatestIndexOnAvailable(Timestamp);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Title", Title);
        contentValues.put("Timestamp", Timestamp);
        contentValues.put("Message", Message);
        contentValues.put("ImageURL", ImageURL);
        contentValues.put("ImageClickRedirect","null");


        db.insert("Notifications", null, contentValues);
        return true;
    }

    public void insertProvider(String Title, String Message, int Timestamp, String ImageURL, String imageClickRedirect) {
        if (Integer.parseInt(getLatestIndex())<Timestamp)
            updateLatestIndexOnAvailable(Timestamp);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Title", Title);
        contentValues.put("Timestamp", Timestamp);
        contentValues.put("Message", Message);
        contentValues.put("ImageURL", ImageURL);
        contentValues.put("ImageClickRedirect",imageClickRedirect);

        db.insert("Notifications", null, contentValues);
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from Notifications where id=" + id + "", null);
        res.close();
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, NotificationsTable);
        return numRows;
    }

    public boolean updateProvider(Integer id, String Title, String Timestamp, String Message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Title", Title);
        contentValues.put("Timestamp", Timestamp);
        contentValues.put("Message", Message);

        db.update("Notifications", contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public void deleteProviderAfterId(Integer id) {
        Log.i("FLUSH","from= "+id+" to= "+getLatestIndex());
        for (int i = id; i <Integer.parseInt(getLatestIndex())+1 ; i++) {
            try {
                SQLiteDatabase db = this.getWritableDatabase();
                db.delete("Notifications",
                        "Timestamp = ? ",
                        new String[]{Integer.toString(i)});
            } catch (Exception e) {
                e.printStackTrace();
                //FIXME: to a more specific catch
            }
        }
    }

    public ArrayList<String> getAllMessages() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        // Cursor res =  db.rawQuery( "select * from Notifications", null );
        Cursor res = db.rawQuery("select * from Notifications ORDER BY Timestamp ", null);

        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getString(res.getColumnIndex(Message)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public ArrayList<DbDataMap> getDataFromDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<DbDataMap> dataFromDB = new ArrayList<>();

        Cursor res = db.rawQuery("select * from Notifications ORDER BY Timestamp DESC", null);
        res.moveToFirst();


        while (!res.isAfterLast()) {
            DbDataMap map = new DbDataMap();
            map.setMessage(res.getString(res.getColumnIndex(Message)));
            map.setTimestamp(res.getString(res.getColumnIndex(Timestamp)));
            map.setTitle(res.getString(res.getColumnIndex("Title")));
            if (res.getString(res.getColumnIndex("ImageURL")) != null) {
                map.setImageUrl(res.getString(res.getColumnIndex("ImageURL")));
                map.setImageRedirectUrl(res.getString(res.getColumnIndex("ImageClickRedirect")));
            }

            Log.i("FROMDB", "Title= " + res.getString(res.getColumnIndex("Title")) +
                    "Timestamp= " + res.getString(res.getColumnIndex(Timestamp))+" redirect URL = "+ map.getImageRedirectUrl());

            dataFromDB.add(map);
            res.moveToNext();
        }
        res.close();

        return dataFromDB;
    }

    void updateLatestIndexOnAvailable(int newLatestIndex) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(localContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("latestIndex",newLatestIndex);
        editor.apply();
    }

    //FIXME: this will cause issue on db size limiting
    public String getLatestIndex() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(localContext);
        return String.valueOf(preferences.getInt("latestIndex",0));
    }

}