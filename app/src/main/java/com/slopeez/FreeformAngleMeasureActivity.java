package com.slopeez;

import static android.content.ContentValues.TAG;

import static com.slopeez.DrawableDotImageView.angles;
import static com.slopeez.DrawableDotImageView.currAngle;
import static com.slopeez.DrawableDotImageView.degree;
import static com.slopeez.DrawableDotImageView.dotPaint;
import static com.slopeez.DrawableDotImageView.linePaint;
import static com.slopeez.DrawableDotImageView.numSetScaleDots;
import static com.slopeez.DrawableDotImageView.paintColor;
import static com.slopeez.DrawableDotImageView.scaleFactor;
import static com.slopeez.DrawableDotImageView.setScaleMode;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

import org.w3c.dom.Text;

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
    public static EditText angleView;
    public static Thread thread;
    Toolbar toolbar2;
    public ScrollView freeformscroll;
    public static double scaleDist = 0;
    public static boolean removeCurr = false;
    ToggleButton degRad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up everything ---------------------------------------------------------------------------
        setContentView(R.layout.freeform_angle_measure);
        pointsView = (DrawableDotImageView) findViewById(R.id.dot_view);
        toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        angleView = (EditText) findViewById(R.id.angleView);
        freeformscroll = (ScrollView) findViewById(R.id.scrollView);
        degRad = (ToggleButton) findViewById(R.id.degRadToggle);

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

        // TODO: why only rotate from middle of screen? unintuituve
        // TODO: keep some of the view in the screen so that it doesnt get lost

        angleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);

        /*
        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!this.isInterrupted()) {
                        Thread.sleep(1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pointsView.invalidate();
                                double d = calcAngle();
                                if (d == -1800000) {
                                    angleView.setText("Tap to place the three dots:");
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
         */

        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!this.isInterrupted()) {
                        Thread.sleep(1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < angles.size(); ++i)
                                {
                                    Double d = calcAngle(i);
                                    angles.set(i, d);
                                }
                                pointsView.invalidate();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        thread.start();

        angleView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                System.out.println("In beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    System.out.println("Setting scale");
                    scaleDist = Double.parseDouble(angleView.getText().toString());
                    if (scaleDist != 0) {
                        System.out.println(angleView.getText());
                        double dis = Math.sqrt((Math.pow((float) (DrawableDotImageView.setScaleDots.get(0).getX() - DrawableDotImageView.setScaleDots.get(1).getX()), 2)) + (Math.pow((float) (DrawableDotImageView.setScaleDots.get(0).getY() - DrawableDotImageView.setScaleDots.get(1).getY()), 2)));
                        scaleFactor = scaleDist / dis; // in (distance/pixel)
                        System.out.println("-------------------------- ScaleFactor: " + scaleFactor);
                    }
                } catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        degRad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    degree = false;
                    pointsView.invalidate();
                } else {
                    // The toggle is disabled
                    degree = true;
                    pointsView.invalidate();
                }
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("count_value", count);
    }

    // TODO: store the image thumbnails in an array instead of remaking them each time: will make app faster. check if u can move image

    public void reset(View view) {
        DrawableDotImageView.angleList.clear();
        DrawableDotImageView.angles.clear();
        currAngle = 0;
        DrawableDotImageView.numDots.clear();
        DrawableDotImageView.numSetScaleDots = 0;
        DrawableDotImageView.setScaleDots.clear();
        DrawableDotImageView.touchedDot = null;
        startActivity(new Intent(FreeformAngleMeasureActivity.this, FreeformAngleMeasureActivity.class));
        overridePendingTransition(0,0);
        pointsView.setScaleMode = false;
    }

    @Override
    public void onBackPressed() {
        DrawableDotImageView.angleList.clear();
        DrawableDotImageView.angles.clear();
        currAngle = 0;
        DrawableDotImageView.numDots.clear();
        DrawableDotImageView.numSetScaleDots = 0;
        DrawableDotImageView.setScaleDots.clear();
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

    public void remove (View view)
    {
        removeCurr = true;
    }

    public static double calcAngle(int position) {

        if (DrawableDotImageView.angleList.get(position).size() != 3) {
            return -1800000;
        }

        double p1x = DrawableDotImageView.angleList.get(position).get(0).getX();
        double p1y = DrawableDotImageView.angleList.get(position).get(0).getY();

        double p2x = DrawableDotImageView.angleList.get(position).get(1).getX();
        double p2y = DrawableDotImageView.angleList.get(position).get(1).getY();

        double p3x = DrawableDotImageView.angleList.get(position).get(2).getX();
        double p3y = DrawableDotImageView.angleList.get(position).get(2).getY();

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

    public void setScale(View view)
    {
        if (setScaleMode)
        {
            setScaleMode = false;
        }
        setScaleMode = true;
        angleView.setText("Tap to place two dots to set scale");
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