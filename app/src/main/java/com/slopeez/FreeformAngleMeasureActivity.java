package com.slopeez;

import static android.content.ContentValues.TAG;

import static com.slopeez.DrawableDotImageView.dotPaint;
import static com.slopeez.DrawableDotImageView.linePaint;
import static com.slopeez.DrawableDotImageView.paintColor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;
import com.xiaopo.flying.sticker.BitmapStickerIcon;
import com.xiaopo.flying.sticker.DeleteIconEvent;
import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.FlipHorizontallyEvent;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;
import com.xiaopo.flying.sticker.ZoomIconEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class FreeformAngleMeasureActivity extends AppCompatActivity {

    public int count = 0;
    private DrawableDotImageView pointsView;
    public static File imageFile2;
    public TextView angleView;
    public static Thread thread;
    Toolbar toolbar2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up everything ---------------------------------------------------------------------------
        setContentView(R.layout.freeform_angle_measure);
        pointsView = (DrawableDotImageView) findViewById(R.id.dot_view);
        toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        angleView = (TextView) findViewById(R.id.angleView);

        // TODO: work on save function and on identification of angle
        // TODO: add perspective correction

        setSupportActionBar(toolbar2);

        // ---------------------------------------------------------------------------------------
/*
        // on below line getting data which we have passed from our adapter class.
        imgPath = getIntent().getStringExtra("imgPath");
        // on below line we are getting our image file from its path.
        File imgFile = new File(imgPath);

 */
        // if the file exists then we are loading that image in our image view.
        if (ImageDetailActivity.imgFile.exists()) {
            Picasso.get().load(ImageDetailActivity.imgFile).placeholder(R.drawable.ic_launcher_background).into(pointsView);
            imageFile2 = ImageDetailActivity.imgFile;
        }

        angleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!this.isInterrupted()) {
                        Thread.sleep(10);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pointsView.invalidate();
                                double d = calcAngle();
                                if (d == -1800000) {
                                    angleView.setText("Angle: -");
                                } else {
                                    angleView.setText("Inner angle: " + String.format("%.2f", d) + "° | Outer: " + String.format("%.2f", 360 - d) + "°");
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("count_value", count);
    }

    // TODO: store the image thumbnails in an array instead of remaking them each time: will make app faster. check if u can move image

    public void reset(View view) {
        DrawableDotImageView.dots.clear();
        DrawableDotImageView.numDots = 0;
        DrawableDotImageView.touchedDot = null;
        startActivity(new Intent(FreeformAngleMeasureActivity.this, FreeformAngleMeasureActivity.class));
        overridePendingTransition(0,0);
    }

    @Override
    public void onBackPressed() {
        DrawableDotImageView.dots.clear();
        DrawableDotImageView.numDots = 0;
        DrawableDotImageView.touchedDot = null;
        dotPaint = null;
        startActivity(new Intent(FreeformAngleMeasureActivity.this, ModeSelect.class));
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


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_save) {
            saveImage();
        }
        return super.onOptionsItemSelected(item);
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

        File file = com.slopeez.flying.stickerview.util.FileUtil.getNewFile(FreeformAngleMeasureActivity.this, "SlopeEZ");

        try (FileOutputStream out = new FileOutputStream(file)) {
            bm.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            Toast.makeText(FreeformAngleMeasureActivity.this, "saved in " + file.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double calcAngle() {

        if (DrawableDotImageView.dots.size() != 3) {
            return -1800000;
        }

        double p1x = DrawableDotImageView.dots.get(0).getX();
        double p1y = DrawableDotImageView.dots.get(0).getY();

        double p2x = DrawableDotImageView.dots.get(1).getX();
        double p2y = DrawableDotImageView.dots.get(1).getY();

        double p3x = DrawableDotImageView.dots.get(2).getX();
        double p3y = DrawableDotImageView.dots.get(2).getY();

        double deg1 = (360 + Math.toDegrees(Math.atan2(p1x - p2x, p1y - p2y))) % 360;
        double deg2 = (360 + Math.toDegrees(Math.atan2(p3x - p2x, p3y - p2y))) % 360;

        if (deg1 <= deg2) {
            if ((deg2 - deg1) > 180) {
                return (360 - (deg2 - deg1)); // smaller
            } else {
                return deg2 - deg1;
            }
        } else {
            if ((360 - (deg1 - deg2) > 180)) {
                return (360 - (360 - (deg1 - deg2)));
            } else {
                return 360 - (deg1 - deg2);
            }
        }
    }

    public void changeColor(View view) {

        if (DrawableDotImageView.paintColor == 0) {
            dotPaint.setColor(Color.RED);
            linePaint.setColor(Color.RED);
        }

        if (DrawableDotImageView.paintColor == 1) {
            dotPaint.setColor(Color.GRAY);
            linePaint.setColor(Color.GRAY);
        }

        if (DrawableDotImageView.paintColor == 2) {
            dotPaint.setColor(Color.BLACK);
            linePaint.setColor(Color.BLACK);
        }

        if (DrawableDotImageView.paintColor == 3) {
            dotPaint.setColor(Color.WHITE);
            linePaint.setColor(Color.WHITE);
        }

        ++paintColor;
        pointsView.invalidate();

        if (paintColor == 4)
        {
            paintColor = 0;
        }
    }
}
