package com.slopeez;

import static com.slopeez.DrawableDotImageView.currAngle;
import static com.slopeez.DrawableDotImageView.dotPaint;
import static com.slopeez.ImageDetailActivity.imgFile;
import static com.slopeez.ImageDetailActivity.imgPath;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ModeSelect extends AppCompatActivity {
    private Toolbar toolbar4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_select);

        toolbar4 = (Toolbar) findViewById(R.id.modeselecttoolbar);
        setSupportActionBar(toolbar4);

        try {
            // on below line getting data which we have passed from our adapter class.
            if (getIntent().getStringExtra("imgPath") != null) {
                imgPath = getIntent().getStringExtra("imgPath");
                // on below line we are getting our image file from its path.
                imgFile = new File(imgPath);
            }

        } catch (Exception e)
        {
            imgFile = FreeformAngleMeasureActivity.imageFile2;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        return true;
    }

    public void goToProtractor(View view)
    {
        DrawableDotImageView.angleList.clear();
        DrawableDotImageView.angles.clear();
        currAngle = 0;
        DrawableDotImageView.numDots.clear();
        DrawableDotImageView.numSetScaleDots = 0;
        DrawableDotImageView.setScaleDots.clear();
        DrawableDotImageView.touchedDot = null;
        dotPaint = null;
        startActivity(new Intent(ModeSelect.this, ImageDetailActivity.class));
    }

    public void goToFreeform(View view)
    {
        startActivity(new Intent(ModeSelect.this, FreeformAngleMeasureActivity.class));
    }

    public void goToLevel(View view)
    {
        startActivity(new Intent(ModeSelect.this, LevelActivity.class));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ModeSelect.this, MainActivity.class));
    }
}
