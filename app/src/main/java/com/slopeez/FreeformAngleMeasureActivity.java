package com.slopeez;

import static android.content.ContentValues.TAG;

import static com.slopeez.DrawableDotImageView.angleList;
import static com.slopeez.DrawableDotImageView.anglePaint;
import static com.slopeez.DrawableDotImageView.angles;
import static com.slopeez.DrawableDotImageView.currAngle;
import static com.slopeez.DrawableDotImageView.degree;
import static com.slopeez.DrawableDotImageView.dotPaint;
import static com.slopeez.DrawableDotImageView.linePaint;
import static com.slopeez.DrawableDotImageView.numDots;
import static com.slopeez.DrawableDotImageView.numDotsLines;
import static com.slopeez.DrawableDotImageView.numSetScaleDots;
import static com.slopeez.DrawableDotImageView.paintColor;
import static com.slopeez.DrawableDotImageView.scale;
import static com.slopeez.DrawableDotImageView.scaleFactor;
import static com.slopeez.DrawableDotImageView.scalelinePaint;
import static com.slopeez.DrawableDotImageView.setScaleDots;
import static com.slopeez.DrawableDotImageView.setScaleMode;
import static com.slopeez.DrawableDotImageView.showMeasure;
import static com.slopeez.DrawableDotImageView.toastCalled;
import static com.slopeez.ImageDetailActivity.imgFile;

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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
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
    public static DrawableDotImageView pointsView;
    public static Toast toast;
    public static File imageFile2;
    public static EditText angleView;
    public static Context context;
    public static Thread thread;
    double dis = 0;
    Toolbar toolbar2;
    public ScrollView freeformscroll;
    public static double scaleDist = 0;
    public static int currRot = 0;
    public static boolean removeCurr = false;
    public static int angleListSize = 0;
    // boolean to determine whether to create lines or angles
    public static boolean line = false;
    public static Switch degRad;
    public static Switch lineAngle;
    public static int width;
    public static int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up everything ---------------------------------------------------------------------------
        setContentView(R.layout.freeform_angle_measure);
        pointsView = (DrawableDotImageView) findViewById(R.id.dot_view);
        toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        angleView = (EditText) findViewById(R.id.angleView);
        freeformscroll = (ScrollView) findViewById(R.id.scrollView);
        degRad = (Switch) findViewById(R.id.degRadToggle);
        lineAngle = (Switch) findViewById(R.id.lineAngleToggle);

        context = getApplicationContext();

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
        if (imgFile.exists()) {
            Picasso.get().load(imgFile).placeholder(R.drawable.ic_launcher_background).into(pointsView);
            imageFile2 = imgFile;
        }

        // TODO: why only rotate from middle of screen? unintuituve
        // TODO: keep some of the view in the screen so that it doesnt get lost

        angleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);

        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!this.isInterrupted()) {
                        Thread.sleep(1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (setScaleDots.size() == 2 && scaleDist != 0) {
                                    dis = Math.sqrt((Math.pow((DrawableDotImageView.setScaleDots.get(0).getX() - DrawableDotImageView.setScaleDots.get(1).getX()), 2)) + (Math.pow((float) (DrawableDotImageView.setScaleDots.get(0).getY() - DrawableDotImageView.setScaleDots.get(1).getY()), 2)));
                                    scaleFactor = scaleDist / dis; // in (distance/pixel)
                                }
                                width = pointsView.getWidth();
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
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println(s);
                if (setScaleMode) {
                    try {
                        String text = angleView.getText().toString().substring(20);
                        scaleDist = Double.parseDouble(text);
                        if (scaleDist != 0 && scaleDist > 0 && setScaleDots.size() == 2) {
                            dis = Math.sqrt((Math.pow((float) (DrawableDotImageView.setScaleDots.get(0).getX() - DrawableDotImageView.setScaleDots.get(1).getX()), 2)) + (Math.pow((float) (DrawableDotImageView.setScaleDots.get(0).getY() - DrawableDotImageView.setScaleDots.get(1).getY()), 2)));
                            scaleFactor = scaleDist / dis; // in (distance/pixel)
                        }
                    } catch (Exception e) {

                    }
                }
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

        lineAngle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    line = false;
                    pointsView.invalidate();
                } else {
                    // The toggle is disabled
                    line = true;
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
        DrawableDotImageView.lineList.clear();
        DrawableDotImageView.currLine = 0;
        DrawableDotImageView.currAngle = 0;
        thread.interrupt();
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
        startActivity(new Intent(FreeformAngleMeasureActivity.this, FreeformAngleMeasureActivity.class));
        overridePendingTransition(0, 0);
        pointsView.setScaleMode = false;
    }

    @Override
    public void onBackPressed() {

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
        startActivity(new Intent(FreeformAngleMeasureActivity.this, FreeformAngleMeasureActivity.class));
        overridePendingTransition(0, 0);

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

    public void remove(View view) {
        if (angleList.size() == 0 && setScaleDots.size() == 0) {
            return;
        }

        if (removeCurr == false) {
            removeCurr = true;
            angleView.setText("Tap dot/line to remove");
        } else {
            if (setScaleMode || setScaleDots.size() == 2) {
                angleView.setText("Enter distance here→");
                angleView.setSelection(angleView.getText().length());
            } else {
                angleView.setText("Tap to place dots for angles");
            }
            removeCurr = false;
        }
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

    public void setScale(View view) {

        if (setScaleMode == false) {
            setScaleMode = true;
            angleView.setText("Enter distance here→");
            angleView.setSelection(angleView.getText().length());
            CharSequence text = "Tap to place scale dots. Specify distance at end of bar above";
                int duration = Toast.LENGTH_LONG;

                FreeformAngleMeasureActivity.toast = Toast.makeText(FreeformAngleMeasureActivity.context, text, duration);
                FreeformAngleMeasureActivity.toast.show();
        }
    }

    public void changeOrientation(View view)
    {
        currRot = currRot + 90;
        if (currRot >= 360)
        {
            currRot = 0;
        }
        pointsView.setRotation(currRot);
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

    @Override
    public void onStop() {
        super.onStop();
        DrawableDotImageView.angleList.clear();
        DrawableDotImageView.angles.clear();
        DrawableDotImageView.lineList.clear();
        DrawableDotImageView.currLine = 0;
        DrawableDotImageView.currAngle = 0;
        thread.interrupt();
        currAngle = 0;
        numDots.clear();
        DrawableDotImageView.numSetScaleDots = 0;
        DrawableDotImageView.setScaleDots.clear();
        for (int i = 0; i < 200; ++i) {
            numDots.add(0);
            numDotsLines.add(0);
        }
        setScaleMode = false;
        DrawableDotImageView.touchedDot = null;
        pointsView.setScaleMode = false;
    }
}