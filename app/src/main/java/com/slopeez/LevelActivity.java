package com.slopeez;

import static com.slopeez.DrawableDotImageView.currAngle;
import static com.slopeez.DrawableDotImageView.dotPaint;
import static com.slopeez.DrawableDotImageView.numDots;
import static com.slopeez.DrawableDotImageView.numDotsLines;
import static com.slopeez.DrawableDotImageView.scaleFactor;
import static com.slopeez.DrawableDotImageView.setScaleDots;
import static com.slopeez.DrawableDotImageView.setScaleMode;
import static com.slopeez.FreeformAngleMeasureActivity.thread;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private SensorManager mSensorManager;
    private Toolbar toolbar3;
    private LinearLayout layout;
    private TextView azimuthView;
    public boolean isTareActive = false;
    public Button tareButton;
    public float[] tareVals = new float[3];
    public float azimuth_angle;
    public float roll_angle;
    public float pitch_angle;

    private Sensor mOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        pitchView = (TextView) findViewById(R.id.pitchView);
        rollView = (TextView) findViewById(R.id.Roll_View);
        toolbar3 = (Toolbar) findViewById(R.id.toolbar3);
        azimuthView = (TextView) findViewById(R.id.azimuthView);
        layout =(LinearLayout) findViewById(R.id.freeformangle);
        tareButton = (Button) findViewById(R.id.tareButton);

        setSupportActionBar(toolbar3);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

    }

    public void tare(View view)
    {
        if (!isTareActive) {
            isTareActive = true;
            tareButton.setText("CANCEL");
            tareButton.setBackgroundColor(Color.RED);
            tareVals[0] = azimuth_angle;
            tareVals[1] = pitch_angle;
            tareVals[2] = roll_angle;
        } else {
            isTareActive = false;
            tareButton.setText("TARE");
            tareButton.setBackgroundColor(Color.GRAY);
            tareVals[0] = 0;
            tareVals[1] = 0;
            tareVals[2] = 0;
        }
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

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

    public void onSensorChanged(SensorEvent event) {
        azimuth_angle = event.values[0];
        pitch_angle = event.values[1];
        roll_angle = event.values[2];
        // Do something with these orientation angles.

        if (!isTareActive) {
            pitchView.setText("Pitch: " + String.format("%.1f", -pitch_angle) + "°"); // 0 when phone straight up like in emulator
            rollView.setText("Roll: " + String.format("%.1f", roll_angle) + "°");
            azimuthView.setText("Azimuth: " + String.format("%.1f", azimuth_angle) + "°");
        } else {
            pitchView.setText("Pitch: " + String.format("%.1f", -pitch_angle+tareVals[1]) + "°"); // 0 when phone straight up like in emulator
            rollView.setText("Roll: " + String.format("%.1f", roll_angle-tareVals[2]) + "°");
            azimuthView.setText("Azimuth: " + String.format("%.1f", azimuth_angle-tareVals[0]) + "°");
        }
    }

    float[] mGravity;
    float[] mGeomagnetic;

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

    @Override
    public void onStop() {
        super.onStop();
        DrawableDotImageView.angleList.clear();
        DrawableDotImageView.angles.clear();
        DrawableDotImageView.lineList.clear();
        DrawableDotImageView.currLine = 0;
        DrawableDotImageView.currAngle = 0;
        if (thread != null) {
            thread.interrupt();
        }
        currAngle = 0;
        numDots.clear();
        DrawableDotImageView.numSetScaleDots = 0;
        DrawableDotImageView.setScaleDots.clear();
        setScaleMode = false;
        DrawableDotImageView.touchedDot = null;
        for (int i = 0; i < 200; ++i)
        {
            numDots.add(0);
            numDotsLines.add(0);
        }
        FreeformAngleMeasureActivity.pointsView.setScaleMode = false;
    }
}
