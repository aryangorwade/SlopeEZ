package com.slopeez;

import static com.slopeez.DrawableDotImageView.dotPaint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;

public class ModeSelect extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_select);
    }

    public void goToProtractor(View view)
    {
        DrawableDotImageView.dots.clear();
        DrawableDotImageView.numDots = 0;
        DrawableDotImageView.touchedDot = null;
        dotPaint = null;
        startActivity(new Intent(ModeSelect.this, ImageDetailActivity.class));
    }
}

