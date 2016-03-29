package com.example.aurobindo.sherlock;

/**
 * Created by aurobindo on 29/3/16.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "tracker", null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE="CREATE TABLE trackdata (_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "GPSlatitude TEXT, GPSlongitude TEXT, GPSAccuracy TEXT, "+
                "GSMlatitude TEXT, GSMlongitude TEXT, GSMAccuracy TEXT, "+
                "cid TEXT, lac TEXT,rssi TEXT,mccmnc TEXT,operator TEXT,neighinfo TEXT,"+
                "created_at TEXT, wifiinfo TEXT)";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS trackdata");
        onCreate(db);
    }

    public void insertData(double gpslatitude,double gpslongitude,float gpsaccuracy,double gsmlatitude,double gsmlongitude,float gsmaccuracy,String cid,String lac,String rssi,String mccmnc,String operator, String neighinfo, String created_at, String wifiinfo)
    {
        try
        {
            SQLiteDatabase db=this.getWritableDatabase();
            ContentValues values=new ContentValues();
            values.put("GPSlatitude", gpslatitude);
            values.put("GPSlongitude", gpslongitude);
            values.put("GPSAccuracy", gpsaccuracy);

            values.put("GSMlatitude", gsmlatitude);
            values.put("GSMlongitude", gsmlongitude);
            values.put("GSMAccuracy", gsmaccuracy);
            values.put("cid", cid);
            values.put("lac", lac);
            values.put("rssi",rssi);
            values.put("mccmnc",mccmnc);
            values.put("operator", operator);
            values.put("neighinfo",neighinfo);
            values.put("created_at",created_at);
            values.put("wifiinfo",wifiinfo);

            db.insert("trackdata", null, values);
            db.close();
        }
        catch(Exception e) {}
    }

    public void deleteData()
    {
        try
        {
            SQLiteDatabase db=this.getWritableDatabase();
            String query="DELETE from trackdata WHERE _id IN (SELECT _id FROM trackdata ORDER BY created_at ASC LIMIT 100)";
            db.execSQL(query);
        }
        catch(Exception e)
        {
            Log.i("Delete Error", e.getMessage().toString());
        }
    }

    public void deleteData(int i)
    {
        try
        {
            SQLiteDatabase db=this.getWritableDatabase();
            String query="DELETE from trackdata WHERE _id IN (SELECT _id FROM trackdata ORDER BY created_at ASC LIMIT "+i+")";
            db.execSQL(query);
        }
        catch(Exception e)
        {
            Log.i("Delete Error", e.getMessage().toString());
        }
    }

    public List<TrackModel> getTrackData()
    {
        List<TrackModel> trackPath=new ArrayList<TrackModel>();
        //Take at most 100 rows.
        String query="SELECT GPSlatitude,GPSlongitude,GPSAccuracy,GSMlatitude,GSMlongitude,GSMAccuracy,"+
                "cid,lac,rssi,mccmnc,operator,neighinfo,created_at,wifiinfo FROM trackdata ORDER BY created_at ASC LIMIT 100";
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor c=db.rawQuery(query, null);

        if(c.moveToFirst())
        {
            do
            {
                TrackModel model=new TrackModel();
                model.GPSlatitude=Double.parseDouble(c.getString(0));
                model.GPSlongitude=Double.parseDouble(c.getString(1));
                model.GPSAccuracy=Float.parseFloat(c.getString(2));
                model.GSMlatitude=Double.parseDouble(c.getString(3));
                model.GSMlongitude=Double.parseDouble(c.getString(4));
                model.GSMAccuracy=Float.parseFloat(c.getString(5));
                model.cid=c.getString(6);
                model.lac=c.getString(7);
                model.rssi=c.getString(8);
                model.mccmnc=c.getString(9);
                model.operator=c.getString(10);
                model.neighinfo=c.getString(11);
                model.created_at=c.getString(12);
                model.wifiinfo=c.getString(13);
                trackPath.add(model);
            }while(c.moveToNext());
        }
        c.close();
        db.close();
        return trackPath;
    }
}

class TrackModel
{
    double GPSlatitude;
    double GPSlongitude;
    float GPSAccuracy;
    double GSMlatitude;
    double GSMlongitude;
    float GSMAccuracy;
    String cid;
    String lac;
    String rssi;
    String mccmnc;
    String operator;
    String neighinfo;
    String created_at;
    String wifiinfo;
}