package com.slopeez;

import static com.slopeez.DrawableDotImageView.dotPaint;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LevelActivity extends AppCompatActivity implements SensorEventListener {
    private TextView pitchView;
    private TextView rollView;
    private Thread thread2;
    private SensorManager sManager;
    private Toolbar toolbar3;

    // Gravity rotational data
    private float gravity[];
    // Magnetic rotational data
    private float magnetic[]; //for magnetic rotational data
    private float accels[] = new float[3];
    private float mags[] = new float[3];
    private float[] values = new float[3];

    // azimuth, pitch and roll
    private float azimuth;
    private float pitch;
    private float roll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        pitchView = (TextView) findViewById(R.id.pitchView);
        rollView = (TextView) findViewById(R.id.Roll_view);
        toolbar3 = (Toolbar) findViewById(R.id.toolbar3);

        setSupportActionBar(toolbar3);

        sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_GRAVITY), sManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LevelActivity.this, ModeSelect.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);

        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mags = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accels = event.values.clone();
                break;
        }

        if (mags != null && accels != null) {
            gravity = new float[9];
            magnetic = new float[9];
            SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
            float[] outGravity = new float[9];
            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Z, outGravity);
            SensorManager.getOrientation(outGravity, values);

            azimuth = (values[0]) * 57.2957795f; // prev: azimuth = values[0]*57.29f
            pitch = (values[1]) * 57.2957795f;
            roll = (values[2]) * 57.2957795f;
            mags = null;
            accels = null;
        }

        // make textview rotate with phone
        pitchView.setText("Pitch: " + String.format("%.2f", pitch) + "°"); // 0 when phone straight up like in emulator
        rollView.setText("Roll: " + String.format("%.2f", roll) + "°"); // 0 when phone straight up but landscape like in emulator but landscape
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_LIGHT), sManager.SENSOR_DELAY_NORMAL);
    }

    public void saveImage() {
        /*
        pointsView.buildDrawingCache();
        Bitmap bm = pointsView.getDrawingCache();
         */

        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bm = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        File file = com.slopeez.flying.stickerview.util.FileUtil.getNewFile(LevelActivity.this, "SlopeEZ");

        try (FileOutputStream out = new FileOutputStream(file)) {
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            Toast.makeText(LevelActivity.this, "saved in " + file.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_save) {
            saveImage();
        }
        return super.onOptionsItemSelected(item);
    }
}
