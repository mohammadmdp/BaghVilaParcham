package mdp.baghvilaparchammanagementapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public final class DB extends SQLiteOpenHelper{
    
    private static       AtomicInteger jobs             = new AtomicInteger(0);
    private static       DB            mInstance        = null;
    private static final String        DATABASE_NAME    = "BaghVilaParcham.db";
    private static final int           DATABASE_VERSION = 1;
    private static final String        DBTYPE_INTEGER   = "INTEGER";
    private static final String        DBTYPE_TEXT      = "TEXT";
//    private static final String        DBTYPE_REAL      = "REAL";
//    private static final String        DBTYPE_BOOLEAN   = "BOOLEAN";
    
    private static final DBTable TABLE_PHONE = new DBTable(
            DBModelPhone.TABLE_NAME,
            "CREATE TABLE IF NOT EXISTS `" + DBModelPhone.TABLE_NAME + "`(`"
            + DBModelPhone.KEY_ID + "` INTEGER NOT NULL,`"
            + DBModelPhone.KEY_NUMBER + "` TEXT NOT NULL,"
            + "PRIMARY KEY (`" + DBModelPhone.KEY_ID + "`)"
            + ");",
            new ArrayList<>(
                    Arrays.asList(
                            new DBTable.Field(DBModelPhone.KEY_ID, DBTYPE_INTEGER),
                            new DBTable.Field(DBModelPhone.KEY_NUMBER, DBTYPE_TEXT)
                                 )
            )
    );
    
    
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private static final ArrayList<DBTable> tables = new ArrayList<>(
            Arrays.asList(
                    TABLE_PHONE
                         ));
    
    private DB(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    synchronized static DB getInstance(Context context){
        if(mInstance == null){
            mInstance = new DB(context);
        }
        
        return mInstance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db){
        
        for(DBTable table : tables){
            db.execSQL(table.getCreateStatement());
        }
        
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        
        if(newVersion != oldVersion){
            for(DBTable table : tables){
                db.execSQL("DROP TABLE IF EXISTS " + table.getName());
                db.execSQL(table.getCreateStatement());
            }
        }
    }

    static synchronized int getJobs(){
        return jobs.get();
    }
    
    static synchronized void addJob(String reference){
        jobs.incrementAndGet();
        App.log("DBOpenHelper job added by: " + reference);
    }
    
    static synchronized void removeJob(String reference){
        jobs.decrementAndGet();
        App.log("DBOpenHelper job removed by: " + reference);
    }
}
