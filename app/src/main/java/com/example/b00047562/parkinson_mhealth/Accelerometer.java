package com.example.b00047562.parkinson_mhealth;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.google.gson.Gson;
import com.mariux.teleport.lib.TeleportClient;
import com.parse.ParseUser;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

import almadani.com.shared.AccelData;


public class Accelerometer extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private TextView txtXValue, txtYValue, txtZValue, tv_shakeAlert;
    private SensorManager MySensorManager;
    private Sensor MyAclmeter;
    private float ax, ay, az, lastx, lasty, lastz;
    private long lastUpdate;
    private static final int SHAKE_THRESHOLD = 1700;
    private static boolean output_upToDate = true;

    private LinearLayout SensorGraph;
    private ArrayList<AccelData> sensorData;
    private View mChart;
    private TeleportClient mTeleportClient;
    private Button BtnShowGraph, BtnReadAccel, BtnShowAnalysis;
    private ParseFunctions customParse; //for custom parse functions from ParseFunctions class
    //private int i=0;

    /* Handles the refresh */
    private Handler outputUpdater = new Handler();

    /* Adjust this value for your purpose */
    public static final long REFRESH_INTERVAL = 100;      // in milliseconds

    /* This object is used as a lock to avoid data loss in the last refresh */
    private static final Object lock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        txtXValue = (TextView) findViewById(R.id.txtXValue);
        txtYValue = (TextView) findViewById(R.id.txtYValue);
        txtZValue = (TextView) findViewById(R.id.txtZValue);
        tv_shakeAlert = (TextView) findViewById(R.id.tv_shake);


        mTeleportClient.setOnSyncDataItemTask(new ShowToastOnSyncDataItemTask());


        SensorGraph = (LinearLayout) findViewById(R.id.Layout_Graph_Container);
        sensorData = new ArrayList();

        BtnReadAccel = (Button) findViewById(R.id.read_btn);
        BtnShowGraph = (Button) findViewById(R.id.show_btn);
        BtnShowAnalysis = (Button) findViewById(R.id.analysis_btn);

        BtnReadAccel.setOnClickListener(this);
        BtnShowGraph.setOnClickListener(this);
        BtnShowAnalysis.setOnClickListener(this);

        BtnShowGraph.setEnabled(false);
        BtnShowAnalysis.setEnabled(false);

        //Get SensorManager and accelerometer
        MySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (MySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            MyAclmeter = MySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            MySensorManager.registerListener(this, MyAclmeter, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d("Accelerometer not found", "Accelerometer not found");
        }
        Thread t = new Thread() {//http://stackoverflow.com/questions/14814714/update-textview-every-second

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(3000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateTextView();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        customParse = new ParseFunctions(getApplicationContext()); //initialized
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ShowToastOnSyncDataItemTask extends TeleportClient.OnSyncDataItemTask {

        @Override
        protected void onPostExecute(DataMap dataMap) {

            //let`s get the String from the DataMap, using its identifier key
            String string = dataMap.getString("x");
            String str = dataMap.getString("y");
            String sg = dataMap.getString("z");

            //let`s create a pretty Toast with our string!
            Toast.makeText(getApplicationContext(),string+str+sg, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // outputUpdater.post(outputUpdaterTask);
                long curTime = System.currentTimeMillis();
                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;

                    //Get accelerometer values
                    ax = event.values[0];
                    ay = event.values[1];
                    az = event.values[2];
                    float speed = Math.abs(ax + ay + az - lastx - lasty - lastz) / diffTime * 10000;

                    //record accelerometer values and store in arraylist of data

                    AccelData data = new AccelData(curTime, ax, ay, az);
                    sensorData.add(data);


                    if (speed > SHAKE_THRESHOLD) {
                        //Log.d("sensor", "shake detected w/ speed: " + speed);
                        //Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                        tv_shakeAlert.setText("Hold Steady !");
                        tv_shakeAlert.setTextColor(Color.RED);
                    }

                    lastx = ax;
                    lasty = ay;
                    lastz = az;
                }
                String x = String.format("%.1f", ax);
                String y = String.format("%.1f", ay);
                String z = String.format("%.1f", az);
                //change display values
                txtXValue.setText(x);
                txtYValue.setText(y);
                txtZValue.setText(z);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do something
    }

    private void updateTextView() {
        tv_shakeAlert.setText("Steady");
        tv_shakeAlert.setTextColor(Color.GREEN);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.read_btn:
                sensorData = new ArrayList();
                BtnReadAccel.setEnabled(false);
                BtnShowGraph.setEnabled(true);
                BtnShowAnalysis.setEnabled(false);
                break;
            case R.id.show_btn:
                BtnReadAccel.setEnabled(true);
                BtnShowGraph.setEnabled(false);
                BtnShowAnalysis.setEnabled(true);

                SensorGraph.removeAllViews(); //reset graph
                //push accel data to Parse
                String json = new Gson().toJson(sensorData);
                /*ParseObject acc = new ParseObject("AccelData");
                acc.put("ArrayList",json);
                acc.put("username", ParseUser.getCurrentUser().getUsername());
                acc.put("createdBy",ParseUser.getCurrentUser());
                acc.saveInBackground();*/

                /*TODO
                instead of using all these lines of code above use this line below:
                 */
                customParse.pushParseData(ParseUser.getCurrentUser(),"AccelData","ArrayList",json,"",""); //user pointer
                openChart();
                break;
            case R.id.analysis_btn:
                /*TODO
                Intent and go to new activity
                */
                break;
        }
    }

    private void openChart() {
        if (sensorData != null || sensorData.size() > 0) {
            long t = 0;
            XYMultipleSeriesDataset dataset = null;
            XYSeries xSeries = null;
            XYSeries ySeries = null;
            XYSeries zSeries = null;

            t = sensorData.get(0).getTimestamp();
            dataset = new XYMultipleSeriesDataset();

            xSeries = new XYSeries("X");
            ySeries = new XYSeries("Y");
            zSeries = new XYSeries("Z");


            for (AccelData data : sensorData) {
                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                zSeries.add(data.getTimestamp() - t, data.getZ());
            }

            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);

            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setYTitle("Values of Acceleration");
            multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < sensorData.size(); i++) {

                multiRenderer.addXTextLabel(i + 1, ""
                        + (sensorData.get(i).getTimestamp() - t));
            }
            for (int i = 0; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, "" + i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);

            // Getting a reference to LinearLayout of the MainActivity Layout

            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);


            // Adding the Line Chart to the LinearLayout
            SensorGraph.addView(mChart);

        }
    }

}
