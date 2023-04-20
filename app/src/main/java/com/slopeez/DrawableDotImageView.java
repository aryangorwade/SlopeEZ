package com.slopeez;

import static com.slopeez.FreeformAngleMeasureActivity.aiActive;
import static com.slopeez.FreeformAngleMeasureActivity.angleListSize;
import static com.slopeez.FreeformAngleMeasureActivity.angleView;
import static com.slopeez.FreeformAngleMeasureActivity.calcAngle;
import static com.slopeez.FreeformAngleMeasureActivity.height;
import static com.slopeez.FreeformAngleMeasureActivity.line;
import static com.slopeez.FreeformAngleMeasureActivity.pointsView;
import static com.slopeez.FreeformAngleMeasureActivity.removeCurr;
import java.io.*;
import static com.slopeez.FreeformAngleMeasureActivity.scaleDist;
import static com.slopeez.FreeformAngleMeasureActivity.thread;
import static com.slopeez.FreeformAngleMeasureActivity.toast;
import static com.slopeez.FreeformAngleMeasureActivity.width;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.opencv.core.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DrawableDotImageView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {

    public static ArrayList<Dot> setScaleDots = new ArrayList<>();
    public static ArrayList<Double> angles = new ArrayList<>();
    public static Paint dotPaint;
    public static Paint linePaint;
    public static Paint distPaint;
    public static Dot touchedDot;
    public static Paint anglePaint;
    public static Paint scaledotPaint;
    public static int numSetScaleDots = 0;
    public static Paint indiLinePaint;
    public static Paint scalelinePaint;
    private final int MAX_DOTS = 3;
    public static int paintColor = 0;
    public static float scale = 1;
    public static int x1 = 0;
    public static int y1 = 0;
    public static double scaleFactor = 0;
    public static boolean degree;
    public static boolean dragged;
    public static boolean toastCalled;
    public static int count = 0;
    public static Bitmap bmp;

    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;
    private boolean isZoomAndRotate;
    private boolean isOutSide;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    public float getRotation = 0;
    private int mode = NONE;
    private PointF start = new PointF();
    public boolean justDeleted = false;
    private PointF mid = new PointF();
    public static boolean showMeasure = false;
    float oldDist = 1f;
    private float xCoOrdinate, yCoOrdinate;
    public static boolean setScaleMode = false;
    // -------------------------------------------
    public static ArrayList<Integer> numDots = new ArrayList<>();
    public static int currAngle = 0;
    public static final ArrayList<ArrayList<Dot>> angleList = new ArrayList<ArrayList<Dot>>();
    // -----------------------------------------
    public static ArrayList<ArrayList<Dot>> lineList = new ArrayList<>();
    public static int currLine = 0;
    public static int currAILine = 0;
    public static ArrayList<Integer> numDotsLines = new ArrayList<>();
    public static ArrayList<Integer> numAiDotsLines = new ArrayList<>();

    public DrawableDotImageView(@NonNull Context context) {
        super(context);
        setup();
    }

    public DrawableDotImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public DrawableDotImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        setOnTouchListener(this);
        dotPaint = new Paint();
        dotPaint.setColor(Color.WHITE);
        scaledotPaint = new Paint();
        scaledotPaint.setColor(Color.BLUE);
        linePaint = new Paint();
        linePaint.setStrokeWidth(5/scale);
        linePaint.setColor(Color.WHITE);
        scalelinePaint = new Paint();
        scalelinePaint.setStrokeWidth(5/scale);
        scalelinePaint.setColor(Color.BLUE);
        anglePaint = new Paint();
        anglePaint.setColor(Color.RED);
        anglePaint.setTextSize(50/scale);
        anglePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        distPaint = new Paint();
        distPaint.setTextSize(40/scale);
        distPaint.setStrokeWidth(5/scale);
        distPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        indiLinePaint = new Paint();
        indiLinePaint.setStrokeWidth(5/scale);
        indiLinePaint.setColor(Color.GREEN);
        indiLinePaint.setAlpha(100);

        invalidate();

        for (int i = 0; i < 200; ++i)
        {
            numDots.add(0);
            numDotsLines.add(0);
            numAiDotsLines.add(0);
        }

        width = this.getMeasuredWidth();
        height = this.getMeasuredHeight();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*
        if (width != 0) {
            bmp = getBitmapFromView(this);
        }

         */

        for (int k = 0; k < angleList.size(); ++k)
        {
            ArrayList<Dot> dots = angleList.get(k);
            for (int i = 0; i < dots.size(); ++i)
            {
                Dot dot = dots.get(i);
                dot.radius = 35/ (2.5f*scale);
                dots.set(i, dot);
            }
            angleList.set(k, dots);
        }

        for (int k = 0; k < lineList.size(); ++k)
        {
            ArrayList<Dot> dots = lineList.get(k);
            for (int i = 0; i < dots.size(); ++i)
            {
                Dot dot = dots.get(i);
                dot.radius = 35/ (2.5f*scale);
                dots.set(i, dot);
            }
            lineList.set(k, dots);
        }

        if (dotPaint != null && linePaint != null) {
            if (DrawableDotImageView.paintColor == 0) {
                dotPaint.setColor(Color.RED);
                linePaint.setColor(Color.RED);
                anglePaint.setColor(Color.RED);
                distPaint.setColor(Color.BLACK);
                indiLinePaint.setColor(Color.GREEN);
            }

            if (DrawableDotImageView.paintColor == 1) {
                dotPaint.setColor(Color.GRAY);
                linePaint.setColor(Color.GRAY);
                anglePaint.setColor(Color.GRAY);
                distPaint.setColor(Color.RED);
                indiLinePaint.setColor(Color.YELLOW);
            }

            if (DrawableDotImageView.paintColor == 2) {
                dotPaint.setColor(Color.BLACK);
                linePaint.setColor(Color.BLACK);
                anglePaint.setColor(Color.BLACK);
                distPaint.setColor(Color.RED);
                indiLinePaint.setColor(Color.GRAY);
            }

            if (DrawableDotImageView.paintColor == 3) {
                dotPaint.setColor(Color.WHITE);
                linePaint.setColor(Color.WHITE);
                anglePaint.setColor(Color.WHITE);
                distPaint.setColor(Color.RED);
                indiLinePaint.setColor(Color.GRAY);
            }
        }

        if (angleList.size() != 0) {
            for (int j = 0; j < angleList.size(); ++j) {
                if (numDots.get(j) != 0) {
                    for (int i = 0; i < angleList.get(j).size(); ++i) {
                        canvas.drawCircle(angleList.get(j).get(i).getX(), angleList.get(j).get(i).getY(), 35 / (2.5f*scale), dotPaint);
                    }
                }

                // draw lines between the three dots: 0 to 1 and 1 to 2
                if (numDots.get(j) == 3) {
                    //          graphics.drawLine
                    linePaint.setStrokeWidth(5 / scale);
                    canvas.drawLine(angleList.get(j).get(0).x, angleList.get(j).get(0).y, angleList.get(j).get(1).x, angleList.get(j).get(1).y, linePaint);
                    canvas.drawLine(angleList.get(j).get(1).x, angleList.get(j).get(1).y, angleList.get(j).get(2).x, angleList.get(j).get(2).y, linePaint);

                    float centerX = (angleList.get(j).get(0).x + angleList.get(j).get(1).x + angleList.get(j).get(2).x) / 3;
                    float centerY = (angleList.get(j).get(0).y + angleList.get(j).get(1).y + angleList.get(j).get(2).y) / 3;

                    if (angles.size() != 0 && (angles.size()-1) >= j) {
                        if (!degree) {
                            canvas.drawText((String.format("%.2f", angles.get(j)) + "°"), centerX, centerY, anglePaint);
                        } else {
                            double rad = (Math.PI / 180) * angles.get(j);
                            canvas.drawText((String.format("%.2f", rad) + " rad"), centerX, centerY, anglePaint);
                        }
                    }

                    if (scaleDist != 0 && setScaleDots.size() == 2 && showMeasure)
                    {
                        double lineDist = Math.sqrt(Math.pow((angleList.get(j).get(0).x - angleList.get(j).get(1).x), 2) + Math.pow((angleList.get(j).get(0).y - angleList.get(j).get(1).y), 2));
                        canvas.drawText((String.format("%.2f", lineDist * scaleFactor)), (angleList.get(j).get(0).x + angleList.get(j).get(1).x)/2, (angleList.get(j).get(0).y + angleList.get(j).get(1).y)/2, distPaint);
                        double lineDist2 = Math.sqrt(Math.pow((angleList.get(j).get(1).x - angleList.get(j).get(2).x), 2) + Math.pow((angleList.get(j).get(1).y - angleList.get(j).get(2).y), 2));
                        canvas.drawText((String.format("%.2f", lineDist2 * scaleFactor)), (angleList.get(j).get(1).x + angleList.get(j).get(2).x)/2, (angleList.get(j).get(1).y + angleList.get(j).get(2).y)/2, distPaint);
                    }
                }
            }

            if ((removeCurr && angleList.size() != 0) || (removeCurr && setScaleDots.size() != 0) || (removeCurr && lineList.size() != 0))
            {
                angleView.setText("Tap dot/line to remove");
            }
        }

        setScaleDots.forEach((dot) -> {
            canvas.drawCircle(dot.getX(), dot.getY(), 35/(2.5f*scale), scaledotPaint);
        });

        if (setScaleDots.size() == 2)
        {
            canvas.drawLine(setScaleDots.get(0).getX(), setScaleDots.get(0).getY(), setScaleDots.get(1).getX(), setScaleDots.get(1).getY(), scalelinePaint);

            if (scaleDist != 0 ) {
                double lineDist = Math.sqrt(Math.pow((setScaleDots.get(0).getX() - setScaleDots.get(1).getX()), 2) + Math.pow((setScaleDots.get(0).getY() - setScaleDots.get(1).getY()), 2));
                canvas.drawText((String.format("%.2f", lineDist * scaleFactor)), (setScaleDots.get(0).x + setScaleDots.get(1).x) / 2, (setScaleDots.get(0).y + setScaleDots.get(1).y) / 2, distPaint);
            }
        }

        if (lineList.size() != 0) {
            for (int j = 0; j < lineList.size(); ++j) {
                if (lineList.get(j).size() != 0) {
                    for (int i = 0; i < lineList.get(j).size(); ++i) {
                        canvas.drawCircle(lineList.get(j).get(i).getX(), lineList.get(j).get(i).getY(), 35/ (2.5f*scale), indiLinePaint);
                    }
                }

                // draw lines between the three dots: 0 to 1 and 1 to 2
                if (lineList.get(j).size() == 2) {
                    //          graphics.drawLine
                    linePaint.setStrokeWidth(5 / scale);
                    canvas.drawLine(lineList.get(j).get(0).x, lineList.get(j).get(0).y, lineList.get(j).get(1).x, lineList.get(j).get(1).y, indiLinePaint);

                    float centerX = (lineList.get(j).get(0).x + lineList.get(j).get(1).x) / 2;
                    float centerY = (lineList.get(j).get(0).y + lineList.get(j).get(1).y) / 2;

                    if (scaleDist != 0 && setScaleDots.size() == 2 && showMeasure)
                    {
                        double lineDist = Math.sqrt(Math.pow((lineList.get(j).get(0).x - lineList.get(j).get(1).x), 2) + Math.pow((lineList.get(j).get(0).y - lineList.get(j).get(1).y), 2));
                        canvas.drawText((String.format("%.2f", lineDist * scaleFactor)), (lineList.get(j).get(0).x + lineList.get(j).get(1).x)/2, (lineList.get(j).get(0).y + lineList.get(j).get(1).y)/2, distPaint);
                    }
                }
            }

            if ((removeCurr && angleList.size() != 0) || (removeCurr && setScaleDots.size() != 0) || (removeCurr && lineList.size() != 0))
            {
                angleView.setText("Tap dot/line to remove");
            }
        }

        // AI ------------------------------------------------------------------

        if (aiActive) {
            for (int j = 0; j < LineDetector.aiLinesList.size(); ++j) {
                if (LineDetector.aiLinesList.get(j).size() != 0) {
                    for (int i = 0; i < LineDetector.aiLinesList.get(j).size(); ++i) {
                        canvas.drawCircle((float)LineDetector.aiLinesList.get(j).get(i).x, (float)LineDetector.aiLinesList.get(j).get(i).y, 35/ (2.5f*scale), indiLinePaint);
                    }
                }

                // draw lines between the three dots: 0 to 1 and 1 to 2
                if (LineDetector.aiLinesList.get(j).size() == 2) {
                    //          graphics.drawLine
                    linePaint.setStrokeWidth(5 / scale);
                    canvas.drawLine((float)LineDetector.aiLinesList.get(j).get(0).x, (float)LineDetector.aiLinesList.get(j).get(0).y, (float)LineDetector.aiLinesList.get(j).get(1).x, (float)LineDetector.aiLinesList.get(j).get(1).y, indiLinePaint);

                    float centerX = ((float)LineDetector.aiLinesList.get(j).get(0).x +(float)LineDetector.aiLinesList.get(j).get(1).x) / 2;
                    float centerY = ((float)LineDetector.aiLinesList.get(j).get(0).y + (float)LineDetector.aiLinesList.get(j).get(1).y) / 2;

                    if (scaleDist != 0 && setScaleDots.size() == 2 && showMeasure)
                    {
                        double lineDist = Math.sqrt(Math.pow((LineDetector.aiLinesList.get(j).get(0).x - LineDetector.aiLinesList.get(j).get(1).x), 2) + Math.pow((LineDetector.aiLinesList.get(j).get(0).y - LineDetector.aiLinesList.get(j).get(1).y), 2));
                        canvas.drawText((String.format("%.2f", lineDist * scaleFactor)), ((float)LineDetector.aiLinesList.get(j).get(0).x + (float)LineDetector.aiLinesList.get(j).get(1).x)/2, ((float)LineDetector.aiLinesList.get(j).get(0).y + (float)LineDetector.aiLinesList.get(j).get(1).y)/2, distPaint);
                    }
                }
            }

            if ((removeCurr && angleList.size() != 0) || (removeCurr && setScaleDots.size() != 0) || (removeCurr && LineDetector.aiLinesList.size() != 0))
            {
                angleView.setText("Tap dot/line to remove");
            }
        }
 // --------------------------------------------------------------------

        anglePaint.setTextSize(40/scale);
        scalelinePaint.setStrokeWidth(5/scale);
        distPaint.setTextSize(30/scale);
        indiLinePaint.setStrokeWidth(5/scale);

        for (int i = 0; i < angles.size(); ++i)
        {
            Double d = calcAngle(i);
            angles.set(i, d);
        }

        if (setScaleDots.size() == 2)
        {
            DrawableDotImageView.showMeasure = true;
        } else {
            showMeasure = false;
        }
        /*
        if (width != 0) {
            DrawableDotImageView.bmp = DrawableDotImageView.getBitmapFromView(pointsView);
            System.out.println("Bitmap gotten");
        }
         */
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        count = event.getPointerCount();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int k = 0; k < setScaleDots.size(); ++k)
                {
                    if (setScaleDots.get(k).isInside((event.getX()), event.getY())) {
                        touchedDot = setScaleDots.get(k);

                        if (removeCurr)
                        {
                            setScaleDots.clear();
                            setScaleMode = false;
                            showMeasure = false;
                            justDeleted = true;
                            removeCurr = false;
                            if (!setScaleMode) {
                                angleView.setText("Tap to place dots for angles");
                            } else {
                                angleView.setText("Enter distance here→");
                            }
                        }

                        mode = NONE;
                        dragged = false;
                        Log.d("ImageView", "Dot touched");
                    } else {
                        viewTransformation(v, event);
                    }
                }

                if (setScaleDots.size() == 2) {
                    PointF p1 = new PointF(setScaleDots.get(0).x, setScaleDots.get(0).y);
                    PointF p2 = new PointF(setScaleDots.get(1).x, setScaleDots.get(1).y);
                    if (isPointOnLineSegment(p1, p2, new PointF((float) event.getX(), (float) event.getY()))) {
                        if (removeCurr) {
                            setScaleDots.clear();
                            touchedDot = null;
                            removeCurr = false;
                            justDeleted = true;
                            showMeasure = false;
                            setScaleMode = false;
                            mode = NONE;
                            dragged = false;
                            if (!setScaleMode) {
                                angleView.setText("Tap to place dots for angles");
                            } else {
                                angleView.setText("Enter distance here→");
                            }                        }
                    }
                }

                for (int i = 0; i < angleList.size(); ++i) { // checks if inside each dot
                    ArrayList<Dot> dots = angleList.get(i);
                    int finalI = i;
                    dots.forEach((dot) -> {
                        if (dot.isInside((event.getX()), event.getY())) {
                            touchedDot = dot;
                            Log.d("ImageView", "Dot touched");

                            if (removeCurr)
                            {
                                angleList.remove(finalI);
                                if (numDots.get(finalI) == 3) {
                                    angles.remove(finalI);
                                }
                                numDots.remove(finalI);
                                if (currAngle > 0) {
                                    --currAngle;
                                }
                                touchedDot = null;
                                removeCurr = false;
                                justDeleted = true;
                                if (!setScaleMode) {
                                    angleView.setText("Tap to place dots for angles");
                                } else {
                                    angleView.setText("Enter distance here→");
                                }                            }

                            mode = NONE;
                            dragged = false;
                        } else {
                            viewTransformation(v, event);
                        }
                    });
                }

                for (int j = 0; j < angleList.size(); ++j)
                {
                    PointF p2 = new PointF(0, 0);
                    PointF p0 = new PointF((float)angleList.get(j).get(0).getX(), (float)angleList.get(j).get(0).getY());
                    if (angleList.get(j).size() >= 2)
                    {
                        PointF p1 = new PointF((float)angleList.get(j).get(1).getX(), (float)angleList.get(j).get(1).getY());
                        if (angleList.get(j).size() == 3)
                        {
                            p2 = new PointF((float)angleList.get(j).get(2).getX(), (float)angleList.get(j).get(2).getY());
                        }
                        if (isPointOnLineSegment(p0, p1, new PointF((float) event.getX(), (float) event.getY())))
                        {
                            if (removeCurr)
                            {
                                angleList.remove(j);
                                if (numDots.get(j) == 3) {
                                    angles.remove(j);
                                }
                                numDots.remove(j);
                                if (currAngle > 0) {
                                    --currAngle;
                                }
                                touchedDot = null;
                                removeCurr = false;
                                justDeleted = true;
                                mode = NONE;
                                dragged = false;
                                if (!setScaleMode) {
                                    angleView.setText("Tap to place dots for angles");
                                } else {
                                    angleView.setText("Enter distance here→");
                                }                            }

                        } else if (angleList.get(j).size() == 3)
                        {
                            if (isPointOnLineSegment(p1, p2, new PointF((float) event.getX(), (float) event.getY())))
                            {
                                if (removeCurr)
                                {
                                    angleList.remove(j);
                                    if (numDots.get(j) == 3) {
                                        angles.remove(j);
                                    }
                                    numDots.remove(j);
                                    if (currAngle > 0) {
                                        --currAngle;
                                    }
                                    touchedDot = null;
                                    removeCurr = false;
                                    justDeleted = true;
                                    mode = NONE;
                                    dragged = false;
                                    if (!setScaleMode) {
                                        angleView.setText("Tap to place dots for angles");
                                    } else {
                                        angleView.setText("Enter distance here→");
                                    }                                }
                            }
                        }
                    }
                }

                for (int i = 0; i < lineList.size(); ++i) { // checks if inside each dot
                    ArrayList<Dot> dots = lineList.get(i);
                    int finalI = i;
                    dots.forEach((dot) -> {
                        if (dot.isInside((event.getX()), event.getY())) {
                            touchedDot = dot;
                            Log.d("ImageView", "Dot touched");

                            if (removeCurr)
                            {
                                lineList.remove(finalI);
                                numDotsLines.remove(finalI);

                                if (currLine > 0) {
                                    --currLine;
                                }
                                touchedDot = null;
                                removeCurr = false;
                                justDeleted = true;
                                mode = NONE;
                                dragged = false;
                                if (!setScaleMode) {
                                    angleView.setText("Tap to place dots for angles");
                                } else {
                                    angleView.setText("Enter distance here→");
                                }                            }

                        } else {
                            viewTransformation(v, event);
                        }
                    });
                }

                for (int j = 0; j < lineList.size(); ++j)
                {
                    PointF p0 = new PointF((float)lineList.get(j).get(0).getX(), (float)lineList.get(j).get(0).getY());
                    if (lineList.get(j).size() > 1)
                    {
                        PointF p1 = new PointF((float)lineList.get(j).get(1).getX(), (float)lineList.get(j).get(1).getY());
                        if (isPointOnLineSegment(p0, p1, new PointF((float) event.getX(), (float) event.getY())))
                        {
                            if (removeCurr)
                            {
                                lineList.remove(j);

                                if (currLine > 0) {
                                    --currLine;
                                }

                                touchedDot = null;
                                numDotsLines.remove(j);
                                removeCurr = false;
                                justDeleted = true;
                                mode = NONE;
                                dragged = false;
                                if (!setScaleMode) {
                                    angleView.setText("Tap to place dots for angles");
                                } else {
                                    angleView.setText("Enter distance here→");
                                }                            }
                        }
                    }
                }

                // AI -------------------------------------------------
                for (int i = 0; i < LineDetector.aiLinesList.size(); ++i) { // checks if inside each dot
                    ArrayList<Dot> dots = new ArrayList<Dot>();
                    dots.add(new Dot((float)LineDetector.aiLinesList.get(i).get(0).x, (float)LineDetector.aiLinesList.get(i).get(0).y, 35/ (2.5f*scale)));
                    dots.add(new Dot((float)LineDetector.aiLinesList.get(i).get(1).x, (float)LineDetector.aiLinesList.get(i).get(1).y, 35/ (2.5f*scale)));

                    int finalI = i;
                    dots.forEach((dot) -> {
                        if (dot.isInside((event.getX()), event.getY())) {
                            touchedDot = dot;
                            Log.d("ImageView", "Dot touched");

                            if (removeCurr)
                            {
                                LineDetector.aiLinesList.remove(finalI);
                                numAiDotsLines.remove(finalI);

                                touchedDot = null;
                                removeCurr = false;
                                justDeleted = true;
                                mode = NONE;
                                dragged = false;
                                if (!setScaleMode) {
                                    angleView.setText("Tap to place dots for angles");
                                } else {
                                    angleView.setText("Enter distance here→");
                                }                            }

                        } else {
                            viewTransformation(v, event);
                        }
                    });
                }

                for (int j = 0; j < LineDetector.aiLinesList.size(); ++j)
                {
                    PointF p0 = new PointF((float)LineDetector.aiLinesList.get(j).get(0).x, (float)LineDetector.aiLinesList.get(j).get(0).y);
                    if (LineDetector.aiLinesList.get(j).size() > 1)
                    {
                        PointF p1 = new PointF((float)LineDetector.aiLinesList.get(j).get(1).x, (float)LineDetector.aiLinesList.get(j).get(1).y);
                        if (isPointOnLineSegment(p0, p1, new PointF((float) event.getX(), (float) event.getY())))
                        {
                            if (removeCurr)
                            {
                                LineDetector.aiLinesList.remove(j);

                                if (currAILine > 0) {
                                    --currAILine;
                                }

                                touchedDot = null;
                                numAiDotsLines.remove(j);
                                removeCurr = false;
                                justDeleted = true;
                                mode = NONE;
                                dragged = false;
                                if (!setScaleMode) {
                                    angleView.setText("Tap to place dots for angles");
                                } else {
                                    angleView.setText("Enter distance here→");
                                }                            }
                        }
                    }
                }
                // -----------------------------------------------------
                invalidate();
                viewTransformation(v, event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchedDot != null) {
                    touchedDot.x = event.getX();
                    touchedDot.y = event.getY();
                    invalidate();
                    dragged = false;
                    mode = NONE;
                    Log.d("ImageView", "Dot moving X: " + touchedDot.x + " Y: " + touchedDot.y);
                }
                invalidate();
                viewTransformation(v, event);
                count = event.getPointerCount();
                break;

            case MotionEvent.ACTION_UP:
                count = event.getPointerCount();
                if (touchedDot != null) {
                    touchedDot = null;

                } else {

                    if (setScaleMode == true && setScaleDots.size() < 2 && !justDeleted) {
                        setScaleDots.add(new Dot(event.getX(), event.getY(), 35/ (2.5f*scale)));
                        invalidate();
                        if (justDeleted)
                        {
                            justDeleted = false;
                        }
                    }

                    else {
                        if (mode == DRAG && dragged == false && !justDeleted) {
                            ArrayList<Dot> temp;

                            if (line == false) {
                                if (numDots.get(currAngle) <= MAX_DOTS - 1) {
                                    if (numDots.get(currAngle) != 0) {
                                        temp = angleList.get(currAngle);
                                    } else {
                                        temp = new ArrayList<Dot>();
                                    }

                                    temp.add(new Dot(event.getX(), event.getY(), 35/ (2.5f*scale)));

                                    if (angleList.size() != 0) {
                                        angleList.set(currAngle, temp);
                                    } else {
                                        angleList.add(temp);
                                    }

                                    numDots.set(currAngle, (numDots.get(currAngle) + 1));

                                    if (numDots.get(currAngle) == 3) {
                                        angles.add(calcAngle(currAngle));
                                    }
                                    invalidate();
                                    Log.d("ImageView", "Dot created X: " + event.getX() + " Y: " + event.getY());
                                } else if (numDots.get(currAngle) == 3 && !justDeleted) {
                                    ++currAngle;
                                    temp = new ArrayList<Dot>();
                                    temp.add(new Dot(event.getX(), event.getY(), 35/ (2.5f*scale)));
                                    angleList.add(temp);

                                    numDots.set(currAngle, (numDots.get(currAngle) + 1));
                                    invalidate();
                                    Log.d("ImageView", "Dot created X: " + event.getX() + " Y: " + event.getY());

                                    invalidate();
                                }
                            } else {
                                // the same for lineList
                                if (numDotsLines.get(currLine) <= 1) {
                                    if (numDotsLines.get(currLine) != 0) {
                                        temp = lineList.get(currLine);
                                    } else {
                                        temp = new ArrayList<Dot>();
                                    }

                                    temp.add(new Dot(event.getX(), event.getY(), 35/ (2.5f*scale)));

                                    if (lineList.size() != 0) {
                                        lineList.set(currLine, temp);
                                    } else {
                                        lineList.add(temp);
                                    }

                                    numDotsLines.set(currLine, (numDotsLines.get(currLine) + 1));


                                    invalidate();
                                    Log.d("ImageView", "Dot created X: " + event.getX() + " Y: " + event.getY());
                                } else if (lineList.get(currLine).size() == 2 && !justDeleted) {
                                    ++currLine;
                                    temp = new ArrayList<Dot>();
                                    temp.add(new Dot(event.getX(), event.getY(), 35/ (2.5f*scale)));
                                    lineList.add(temp);

                                    numDotsLines.set(currLine, (numDotsLines.get(currLine) + 1));

                                    invalidate();
                                    Log.d("ImageView", "Dot created X: " + event.getX() + " Y: " + event.getY());

                                    invalidate();
                                }
                            }
                            if (justDeleted)
                            {
                                justDeleted = false;
                            }
                        }
                        if (justDeleted)
                        {
                            justDeleted = false;
                        }
                    }
                }
                viewTransformation(v, event);
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                viewTransformation(v, event);
                invalidate();
                break;

            default:
                viewTransformation(v, event);
                invalidate();
                break;
        }
        return true;
    }
/*
    public static Bitmap getBitmapFromView(View view)
    {
        Bitmap bitmap = null;
        return bitmap;
    }

 */

    public static class Dot {
        private float x;
        private float y;
        private float radius;

        public Dot(float x, float y, float radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getRadius() {
            return radius;
        }

        //https://www.geeksforgeeks.org/find-if-a-point-lies-inside-or-on-circle/
        public boolean isInside(float x, float y) {
            return (getX() - x) * (getX() - x) + (getY() - y) * (getY() - y) <= radius * radius;
        }

    }

    private void viewTransformation(View view, MotionEvent event) {
        dragged = false;
        if (event.getPointerCount() == 2)
        {
            System.out.println("2 touches fingers");
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xCoOrdinate = view.getX() - event.getRawX();
                yCoOrdinate = view.getY() - event.getRawY();

                start.set(event.getX(), event.getY());
                isOutSide = false;
                if (event.getPointerCount() == 1) {
                    mode = DRAG;
                }
                if (event.getPointerCount() == 2) {
                    mode = ZOOM;
                }
                lastEvent = null;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);

                if (event.getPointerCount() == 2) {
                    mode = ZOOM;
                }
                if (oldDist > 10f) {
                    midPoint(mid, event);
                    if (event.getPointerCount() == 2) {
                        mode = ZOOM;
                    }
                }

                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                // TODO: when pivot is set, photo moves. why?
                if (count == 2) {
                    // narrowed culprit for view shifting down to these two damn lines
                    view.setPivotX((lastEvent[0] + lastEvent[1]) / 2);
                    view.setPivotY((lastEvent[2] + lastEvent[3]) / 2);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mode == DRAG) {
                    float x = event.getX();
                    float y = event.getY();
                }
                if (event.getPointerCount() == 1) {
                    mode = DRAG;
                }
                if (event.getPointerCount() == 2) {
                    mode = ZOOM;
                }
            case MotionEvent.ACTION_OUTSIDE:
                isOutSide = true;
                mode = NONE;
                lastEvent = null;
                if (event.getPointerCount() == 1) {
                    mode = DRAG;
                }
                if (event.getPointerCount() == 2) {
                    mode = ZOOM;
                }
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                if (event.getPointerCount() == 1) {
                    mode = DRAG;
                }
                if (event.getPointerCount() == 2) {
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isOutSide) {
                    if (mode == DRAG && event.getPointerCount() == 1) {
                        if (count == 1) {
                            dragged = true;
                            view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                        }
                    }
                    if (mode == ZOOM && event.getPointerCount() == 2) {
                        dragged = false;
                        float newDist1 = spacing(event);
                        if (newDist1 > 10f) {
                            scale = newDist1 / oldDist * view.getScaleX();
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                            newRot = rotation(event);
                            getRotation = view.getRotation();
                            view.setRotation((float) (view.getRotation() + (newRot - d)));
                        }
                        if (lastEvent != null) {
                        }
                    }
                }
        }
    }

    public boolean isPointOnLine(PointF lineStaPt, PointF lineEndPt, PointF point) {
        final float EPSILON = 0.001f;
        if (Math.abs(lineStaPt.x - lineEndPt.x) < EPSILON) {
            // We've a vertical line, thus check only the x-value of the point.
            return (Math.abs(point.x - lineStaPt.x) < EPSILON);
        } else {
            float m = (lineEndPt.y - lineStaPt.y) / (lineEndPt.x - lineStaPt.x);
            float b = lineStaPt.y - m * lineStaPt.x;
            return (Math.abs(point.y - (m * point.x + b)) < EPSILON);
        }
    }

    public boolean isPointOnLineSegment(PointF staPt, PointF endPt, PointF point) {
        double mindistance = GFG.minDistance(new GFG.pair(staPt.x, staPt.y), new GFG.pair(endPt.x, endPt.y), new GFG.pair (point.x, point.y));
        if (mindistance <= 25/scale)
        {
            return true;
        } else {
            return false;
        }
    }

    public boolean in_circle(double center_x, double center_y, double radius, double x, double y) {
        double square_dist = Math.pow((center_x - x), 2) + Math.pow((center_y - y), 2);
        return square_dist <= Math.pow(radius, 2);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (int) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}

// Java implementation of the approach
class GFG
{

    static class pair
    {
        double F, S;
        public pair(double F, double S)
        {
            this.F = F;
            this.S = S;
        }
        public pair() {
        }
    }

    // Function to return the minimum distance
// between a line segment AB and a point E
    static double minDistance(pair A, pair B, pair E) {

        // vector AB
        pair AB = new pair();
        AB.F = B.F - A.F;
        AB.S = B.S - A.S;

        // vector BP
        pair BE = new pair();
        BE.F = E.F - B.F;
        BE.S = E.S - B.S;

        // vector AP
        pair AE = new pair();
        AE.F = E.F - A.F;
        AE.S = E.S - A.S;

        // Variables to store dot product
        double AB_BE, AB_AE;

        // Calculating the dot product
        AB_BE = (AB.F * BE.F + AB.S * BE.S);
        AB_AE = (AB.F * AE.F + AB.S * AE.S);

        // Minimum distance from
        // point E to the line segment
        double reqAns = 0;

        // Case 1
        if (AB_BE > 0) {

            // Finding the magnitude
            double y = E.S - B.S;
            double x = E.F - B.F;
            reqAns = Math.sqrt(x * x + y * y);
        }

        // Case 2
        else if (AB_AE < 0) {
            double y = E.S - A.S;
            double x = E.F - A.F;
            reqAns = Math.sqrt(x * x + y * y);
        }

        // Case 3
        else {

            // Finding the perpendicular distance
            double x1 = AB.F;
            double y1 = AB.S;
            double x2 = AE.F;
            double y2 = AE.S;
            double mod = Math.sqrt(x1 * x1 + y1 * y1);
            reqAns = Math.abs(x1 * y2 - y1 * x2) / mod;
        }
        return reqAns;
    }
}


// TODO: bound points in angeview
// TODO: create arraylist with all lines to display distances easily

// TODO: fix zooming: make it over user's fingerprint average and not the center of
// TODO: the pointsview
