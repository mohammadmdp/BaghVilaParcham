package mdp.baghvilaparchammanagementapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class DBModelPhone{
    
    static final String TABLE_NAME = "Phone";
    static final String KEY_ID     = "Id";
    static final String KEY_NUMBER = "Number";
    
    
    public DBModelPhone(){
    
    }
    
    
    @SuppressLint("Range")
    synchronized static JSONArray getPhones(Context context) throws Exception{
        JSONArray            branches = new JSONArray();
        JSONObject           mObject;
        final SQLiteDatabase sq       = DB.getInstance(context).getReadableDatabase();
        DB.addJob("DBModelPhone.getPhones()");
        Cursor mCursor = sq.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        while(mCursor.moveToNext()){
            mObject = new JSONObject();
            mObject.put(KEY_ID, mCursor.getLong(mCursor.getColumnIndex(KEY_ID)));
            mObject.put(KEY_NUMBER, mCursor.getString(mCursor.getColumnIndex(KEY_NUMBER)));
            branches.put(mObject);
        }
        mCursor.close();
        
        DB.removeJob("DBModelPhone.getPhones()");
        new Timer().schedule(
                new TimerTask(){
                    @Override
                    public void run(){
                        if(DB.getJobs() == 0 && sq.isOpen()){
                            sq.close();
                        }
                    }
                },
                500
                            );
        return branches;
    }
    
    public synchronized static void resetPhones(JSONArray newPhones,
                                                Context context)
            throws JSONException, SQLiteException{
        App.log("resetPhones: " + newPhones.toString());
        ContentValues        values;
        JSONObject           jObject;
        final SQLiteDatabase sq = DB.getInstance(context).getWritableDatabase();
        DB.addJob("DBModelPhone.resetPhones()");
        sq.delete(TABLE_NAME, null, null);
        for(int i = 0; i < newPhones.length(); ++i){
            values = new ContentValues();
            
            jObject = newPhones.getJSONObject(i);
            values.put(KEY_ID, jObject.getLong(KEY_ID));
            values.put(KEY_NUMBER, jObject.getString(KEY_NUMBER));
            
            sq.insert(TABLE_NAME, null, values);
        }
        DB.removeJob("DBModelPhone.resetPhones()");
        new Timer().schedule(
                new TimerTask(){
                    @Override
                    public void run(){
                        if(DB.getJobs() == 0 && sq.isOpen()){
                            sq.close();
                        }
                    }
                },
                500
                            );
    }
}
