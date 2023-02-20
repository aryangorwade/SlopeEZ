package com.slopeez;

import static com.slopeez.DrawableDotImageView.dotPaint;
import static com.slopeez.ImageDetailActivity.imgFile;
import static com.slopeez.ImageDetailActivity.imgPath;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ModeSelect extends AppCompatActivity {
    public boolean firstTime = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_select);

        try {
            // on below line getting data which we have passed from our adapter class.
            if (firstTime) {
                imgPath = getIntent().getStringExtra("imgPath");
                firstTime = false;
            }
            // on below line we are getting our image file from its path.
            imgFile = new File(imgPath);

        } catch (Exception e)
        {
            imgFile = FreeformAngleMeasureActivity.imageFile2;
        }
    }

    public void goToProtractor(View view)
    {
        DrawableDotImageView.dots.clear();
        DrawableDotImageView.numDots = 0;
        DrawableDotImageView.touchedDot = null;
        dotPaint = null;
        startActivity(new Intent(ModeSelect.this, ImageDetailActivity.class));
    }

    public void goToFreeform(View view)
    {
        startActivity(new Intent(ModeSelect.this, FreeformAngleMeasureActivity.class));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ModeSelect.this, MainActivity.class));
    }
}

