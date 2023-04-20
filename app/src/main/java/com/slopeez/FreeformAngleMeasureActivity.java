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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FreeformAngleMeasureActivity extends AppCompatActivity {
    public int count = 0;
    public static DrawableDotImageView pointsView;
    public static Toast toast;
    public static File imageFile2;
    public static EditText angleView;
    public static Context context;
    public static Thread thread;
    double dis = 0;
    public static Bitmap img;
    Toolbar toolbar2;
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
    public static boolean aiActive = false;
    public static Button aiButton;

    // AI popup window
    Button popupButton;
    PopupWindow popupWindow;
    EditText textBox1, textBox2, textBox3;
    TextView label1, label2, label3;
    public static int[] aiVals = new int[] {150, 50, 17};


    static {
        System.loadLibrary("opencv_java4");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up everything ---------------------------------------------------------------------------
        setContentView(R.layout.freeform_angle_measure);
        pointsView = (DrawableDotImageView) findViewById(R.id.dot_view);
        toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        angleView = (EditText) findViewById(R.id.angleView);
        degRad = (Switch) findViewById(R.id.degRadToggle);
        lineAngle = (Switch) findViewById(R.id.lineAngleToggle);
        aiButton = (Button) findViewById(R.id.aiButton);

        context = getApplicationContext();

        // TODO: work on save function and on identification of angle
        // TODO: add perspective correction

        setSupportActionBar(toolbar2);

        // ---------------------------------------------------------------------------------------
        // if the file exists then we are loading that image in our image view.
        if (imgFile.exists()) {
            Picasso.get().load(imgFile).placeholder(R.drawable.ic_launcher_background).into(pointsView);
            imageFile2 = imgFile;
            img = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
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
                            pointsView.invalidate();
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

        final SeekBar[] aiSlider = new SeekBar[1];
        aiSlider[0] = new SeekBar(pointsView.getContext());
        Handler handler = new Handler();
        final CountDownTimer[] timer = new CountDownTimer[1];
        timer[0] = new CountDownTimer(1, 1) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

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
        DrawableDotImageView.angleList.clear();
        DrawableDotImageView.angles.clear();
        DrawableDotImageView.lineList.clear();
        DrawableDotImageView.currLine = 0;
        DrawableDotImageView.currAngle = 0;
        LineDetector.reset();
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
        LineDetector.reset();
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
        if (angleList.size() == 0 && setScaleDots.size() == 0 && LineDetector.aiLinesList.size() == 0) {
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
        LineDetector.reset();
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

    public void showAIPopupWindow(View view)
    {
        PopupWindow popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT,  RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        //     LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_window, null);

        popupView.setBackground(ContextCompat.getDrawable(this, R.drawable.popup_border));

        popupWindow.setContentView(popupView);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        popupView.setOnTouchListener(new View.OnTouchListener() {
            private float mDx;
            private float mDy;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        int[] location = new int[2];
                        popupWindow.getContentView().getLocationOnScreen(location);

                        mDx = location[0] - startX;
                        mDy = location[1] - startY;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + mDx;
                        float newY = event.getRawY() + mDy;
                        popupWindow.update((int) (newX - startX), (int) (newY - startY), -1, -1, true);
                        return true;
                }
                return false;
            }
        });

        // dim the background
        View container = popupWindow.getContentView().getRootView();
        Context context = popupWindow.getContentView().getContext();
        ColorDrawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, container.getWidth(), container.getHeight());
        dim.setAlpha(100);
        container.getOverlay().add(dim);

        // get the text boxes and labels
        textBox1 = popupView.findViewById(R.id.text_box1);
        label1 = popupView.findViewById(R.id.label1);
        textBox2 = popupView.findViewById(R.id.text_box2);
        label2 = popupView.findViewById(R.id.label2);
        textBox3 = popupView.findViewById(R.id.text_box3);
        label3 = popupView.findViewById(R.id.label3);

        textBox1.setText("" + aiVals[0]);
        textBox2.setText("" + aiVals[1]);
        textBox3.setText("" + aiVals[2]);

        // create a submit button
        Button submitButton = popupView.findViewById(R.id.submit_button);
        Button noAIButton = popupView.findViewById(R.id.noAI_button);

        // add a listener to the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitValues();
                popupWindow.dismiss();
                LineDetector.reset();
                LineDetector.detectEdges(img);
                aiActive = true;
                pointsView.invalidate();
            }
        });

        noAIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!aiActive)
                {
                    aiActive = true;
                } else {
                    aiActive = false;
                    LineDetector.reset();
                }
                popupWindow.dismiss();
                pointsView.invalidate();
            }
        });
    }

    public void submitValues()
    {
        aiVals = new int[3];
        aiVals[0] = Integer.parseInt(textBox1.getText().toString()); // threshold
        aiVals[1] = Integer.parseInt(textBox2.getText().toString()); // minLineLength
        aiVals[2] = Integer.parseInt(textBox3.getText().toString()); // maxLineGap
    }

    public void aiAnalyze(View view)
    {
        showAIPopupWindow(view);
    }
}

// TODO: make the ai parameter window handle incorrect input without the app crashing
// TODO: remove the submit button and make changes to the ai parameters real-time.
// TODO: implement a slider for the values or say what value bounds are
// TODO: explain what each value does (maybe in a toast or popup)
// ignore this line
