package com.example.aurobindo.sherlock;
/**
 * Created by aurobindo on 29/3/16.
 */

/**
 * Author
 █████╗ ██╗   ██╗██████╗  ██████╗
 ██╔══██╗██║   ██║██╔══██╗██╔═══██╗
 ███████║██║   ██║██████╔╝██║   ██║
 ██╔══██║██║   ██║██╔══██╗██║   ██║
 ██║  ██║╚██████╔╝██║  ██║╚██████╔╝
 ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝ ╚═════╝

 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements OnClickListener, SensorEventListener {
    public TextView txtdata;
    public Button btnstart, btnstop, btnsync, btnsave;
    public double GPSLat, GPSLong, GSMLat, GSMLong;
    public float GPSAccuracy, GSMAccuracy;
    public WifiManager mainWifiObj;
    public String wifiinfo = "";
    public String cid = "", lac = "", rssi = "", mccmnc = "", operator = "", neighinfo = "";
    public LocationManager GPSmgr, GSMmgr;
    public SensorManager sensormanager;
    public DBHelper db;
    public TelephonyManager tm;
    public MyPhoneStateListener MyListener;
    public List<NeighboringCellInfo> info;
    public int flag = 0;
    public boolean startFlag=false, saveFlag=false, stopFlag = false;
    public long tStart, tEnd = 0, tDelta = 0;
    public File externalStorageDirectory;
    public FileWriter filewriter;
    public FileWriter filewriterData;
    public String SERVER_IP = "http://10.129.156.212/tracker/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        MyListener = new MyPhoneStateListener();
        tm.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        txtdata = (TextView) findViewById(R.id.txtdata);
        btnstart = (Button) findViewById(R.id.btnstart);
        btnstop = (Button) findViewById(R.id.btnstop);
        btnsave = (Button) findViewById(R.id.btnsave);
        btnsync = (Button) findViewById(R.id.btnsync);
        btnstart.setOnClickListener(this);
        btnstop.setOnClickListener(this);
        btnsave.setOnClickListener(this);
        btnsync.setOnClickListener(this);
        GPSmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        GSMmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        GPSmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GPSlistener);
        GSMmgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,GSMlistener);

        mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        sensormanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        externalStorageDirectory = Environment.getExternalStorageDirectory();
    }

    LocationListener GPSlistener=new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            GPSLat=location.getLatitude();
            GPSLong=location.getLongitude();
            GPSAccuracy=location.getAccuracy();
        }
    };

    LocationListener GSMlistener=new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            GSMLat=location.getLatitude();
            GSMLong=location.getLongitude();
            GSMAccuracy=location.getAccuracy();
        }
    };
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnstart)
        {
            if(stopFlag)    {
                Toast.makeText(getApplicationContext(),"Please Restart the App !!! ",Toast.LENGTH_SHORT).show();
            }
            else if(!startFlag) {
                startFlag = true;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                GPSmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GPSlistener);
                GSMmgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, GSMlistener);
                flag = 1;
                background.start();
                wifi.start();

                Date date = new Date();

                try {
                    filewriter = new FileWriter(new File(externalStorageDirectory,
                            "motionData.csv"), true);
                    filewriterData = new FileWriter(new File(externalStorageDirectory,
                            "Data.csv"), true);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Something unexpected happened", Toast.LENGTH_SHORT).show();
                }

                tStart = System.currentTimeMillis();

                sensormanager.registerListener((SensorEventListener) this,
                        sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_UI);
                sensormanager.registerListener((SensorEventListener) this,
                        sensormanager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                        SensorManager.SENSOR_DELAY_UI);
                sensormanager.registerListener((SensorEventListener) this,
                        sensormanager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                        SensorManager.SENSOR_DELAY_UI);
                sensormanager.registerListener((SensorEventListener) this,
                        sensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                        SensorManager.SENSOR_DELAY_UI);

                sensormanager.registerListener((SensorEventListener) this,
                        sensormanager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                        SensorManager.SENSOR_DELAY_UI);

                Toast.makeText(getApplicationContext(),
                        "Accelerometer, Gyroscope & Barometer Started !!!", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(),
                        "Application Already Started !!!", Toast.LENGTH_SHORT).show();
        }
        else if(v.getId()==R.id.btnstop)
        {
            if(startFlag) {
                stopFlag = true;
                flag = 0;
                GPSmgr.removeUpdates(GPSlistener);
                GSMmgr.removeUpdates(GSMlistener);
                sensormanager.unregisterListener(this);
                try {
                    filewriter.close();
                    filewriterData.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Data Collection Stoppped !!!", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(getApplicationContext(), "Application not Started !!!", Toast.LENGTH_SHORT).show();
        }
        else if(v.getId()==R.id.btnsave)
        {
            if(!saveFlag) {
                saveFlag = true;
                save.start();
                Toast.makeText(getApplicationContext(), "All Data has been saved !!! ", Toast.LENGTH_SHORT).show();
            }
            else    {
                Toast.makeText(getApplicationContext(), "Restart App !!! ", Toast.LENGTH_SHORT).show();
            }
        }
        else if(v.getId()==R.id.btnsync)
        {
            //syncing code here
            //sync.start();
            Toast.makeText(getApplicationContext(),"Please don't press this button. It's Useless !!!",Toast.LENGTH_SHORT).show();
        }
    }
    Thread wifi=new Thread()
    {
        @Override
        public void run()
        {
            while(flag==1)
            {
                try
                {
                    Thread.sleep(2000);
                    mainWifiObj.startScan();
                    List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
                    int length=wifiScanList.size();
                    for(int i = 0; i < length; i++){
                        wifiinfo += wifiScanList.get(i).BSSID.toString()+";"+wifiScanList.get(i).SSID.toString()+";"+Integer.toString(wifiScanList.get(i).level)+",";
                    }
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    };

    Thread background=new Thread()
    {
        @Override
        public void run()
        {
            while(flag==1)
            {
                try {
                    Thread.sleep(2000);
                    GsmCellLocation loc=(GsmCellLocation)tm.getCellLocation();
                    try{
                        cid=String.valueOf(loc.getCid() & 0xffff);
                        lac=String.valueOf(loc.getLac());
                    }
                    catch(NullPointerException ne) {
                        cid = "0";
                        lac = "0";
                    }
                    mccmnc=tm.getNetworkOperator();
                    operator=tm.getNetworkOperatorName();
                    info=tm.getNeighboringCellInfo();
                    int length=info.size();
                    while(length>0)
                    {
                        neighinfo+=String.valueOf(info.get(length-1).getCid())+":"+String.valueOf(info.get(length-1).getLac())+":"+String.valueOf(-113+2*info.get(length-1).getRssi())+"/";
                        length--;
                    }
                    tEnd = System.currentTimeMillis();
                    tDelta = tEnd - tStart;
                    db.insertData(GPSLat, GPSLong, GPSAccuracy, GSMLat, GSMLong, GSMAccuracy, cid, lac, rssi, mccmnc, operator, neighinfo, Long.toString(tDelta), wifiinfo);
                    try {
                        filewriterData.append(GPSLat + "," + GPSLong + "," + GPSAccuracy + "," + GSMLat + "," + GSMLong + "," + GSMAccuracy + "," + cid + "," + lac + "," + rssi + "," + mccmnc + "," + operator + "," + neighinfo + "," + Long.toString(tDelta) + "," + wifiinfo + "\n");
                    } catch (IOException e) {}

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            txtdata.setText(GPSLat + " || " + GPSLong + " || " + cid + " || " + rssi + " || " + tDelta);
                        }
                    });
                    neighinfo="";
                    wifiinfo="";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Thread save=new Thread()
    {
        @Override
        public void run()
        {
            int length=1;
            while(length>0)
            {
                try {
                    List<TrackModel> trackPath=db.getTrackData();
                    String data="";
                    length=trackPath.size();
                    if(length>0)
                    {
                        for(int i=0;i<length;i++)
                        {
                            data+=Double.toString(trackPath.get(i).GPSlatitude)+","+Double.toString(trackPath.get(i).GPSlongitude)+","+
                                    Float.toString(trackPath.get(i).GPSAccuracy)+","+Double.toString(trackPath.get(i).GSMlatitude)+","+
                                    Double.toString(trackPath.get(i).GSMlongitude)+","+Float.toString(trackPath.get(i).GSMAccuracy)+","+
                                    trackPath.get(i).cid+","+trackPath.get(i).lac+","+trackPath.get(i).rssi+","+trackPath.get(i).mccmnc+","+
                                    trackPath.get(i).operator+","+trackPath.get(i).neighinfo+","+trackPath.get(i).created_at+","+trackPath.get(i).wifiinfo+"\n";
                        }
                        if(writeToFile(data))
                        {
                            db.deleteData();
                            data="";
                        }
                    }
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {}
                catch (Exception ex) {}
            }
        }
    };

    Thread sync=new Thread()
    {
        @Override
        public void run()
        {
            int length=1;
            while(length>0)
            {
                try {
                    List<TrackModel> trackPath=db.getTrackData();

                    String url = SERVER_IP+"insertDataArray.php";
                    length=trackPath.size();   // TODO: Consider calling

                    if(length>0)
                    {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost(url);
                        ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
                        for(int i=0;i<length;i++)
                        {
                            param.add(new BasicNameValuePair("GPSlatitude[]",Double.toString(trackPath.get(i).GPSlatitude)));
                            param.add(new BasicNameValuePair("GPSlongitude[]",Double.toString(trackPath.get(i).GPSlongitude)));
                            param.add(new BasicNameValuePair("GPSAccuracy[]",Float.toString(trackPath.get(i).GPSAccuracy)));
                            param.add(new BasicNameValuePair("GSMlatitude[]",Double.toString(trackPath.get(i).GSMlatitude)));
                            param.add(new BasicNameValuePair("GSMlongitude[]",Double.toString(trackPath.get(i).GSMlongitude)));
                            param.add(new BasicNameValuePair("GSMAccuracy[]",Float.toString(trackPath.get(i).GSMAccuracy)));
                            param.add(new BasicNameValuePair("cid[]",trackPath.get(i).cid));
                            param.add(new BasicNameValuePair("lac[]",trackPath.get(i).lac));
                            param.add(new BasicNameValuePair("rssi[]",trackPath.get(i).rssi));
                            param.add(new BasicNameValuePair("mccmnc[]",trackPath.get(i).mccmnc));
                            param.add(new BasicNameValuePair("operator[]",trackPath.get(i).operator));
                            param.add(new BasicNameValuePair("neighinfo[]",trackPath.get(i).neighinfo));
                            param.add(new BasicNameValuePair("created_at[]",trackPath.get(i).created_at));
                        }
                        param.add(new BasicNameValuePair("number","1234567890"));
                        param.add(new BasicNameValuePair("count",Integer.toString(length)));

                        httpPost.setEntity(new UrlEncodedFormEntity(param));
                        HttpResponse httpResponse = httpClient.execute(httpPost);


                        if(httpResponse.getStatusLine().getStatusCode()==200)
                        {
                            BufferedReader in=new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                            StringBuffer sb=new StringBuffer();
                            String line;
                            while((line=in.readLine())!=null)
                                sb.append(line);
                            in.close();
                            String reply=sb.toString();
                            Log.i("Response",reply);
                            if(reply.equals("success"))
                            {
                                db.deleteData();
                            }
                        }
                    }
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {}
                catch (Exception ex) {}
            }
        }
    };


    private class MyPhoneStateListener extends PhoneStateListener
    {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            int val=-113+2*signalStrength.getGsmSignalStrength();
            rssi=String.valueOf(val);
        }
    };


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];


            tEnd = System.currentTimeMillis();
            tDelta = tEnd - tStart;

            try {
                filewriter.write("\nAccl," + x + "," + y + "," + z + "," + tDelta);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long tEnd = System.currentTimeMillis();
            long tDelta = tEnd - tStart;
            try {
                filewriter.write("\nGyro," + x + "," + y + "," + z + "," + tDelta);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {

            float x = event.values[0];
            long tEnd = System.currentTimeMillis();
            long tDelta = tEnd - tStart;

            try {
                filewriter.write("\nBaro," + x + "," + tDelta);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean writeToFile(String data)
    {
        try
        {
            File file=new File(getExternalFilesDir(null).toString());
            file.mkdirs();
            File f=new File(file,"data.csv");
            FileWriter fw=new FileWriter(f,true);
            BufferedWriter out=new BufferedWriter(fw);
            out.append(data);
            out.close();
            return true;
        }
        catch(FileNotFoundException f)
        {
            return false;
        }
        catch(Exception e)
        {
            return false;
        }
    }
}