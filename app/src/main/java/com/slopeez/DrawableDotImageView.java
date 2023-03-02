package com.slopeez;

import static com.slopeez.FreeformAngleMeasureActivity.angleListSize;
import static com.slopeez.FreeformAngleMeasureActivity.angleView;
import static com.slopeez.FreeformAngleMeasureActivity.calcAngle;
import static com.slopeez.FreeformAngleMeasureActivity.removeCurr;
import java.io.*;
import static com.slopeez.FreeformAngleMeasureActivity.scaleDist;
import static com.slopeez.FreeformAngleMeasureActivity.thread;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DrawableDotImageView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {

    public static ArrayList<Dot> setScaleDots = new ArrayList<>();
    public static ArrayList<Double> angles = new ArrayList<>();
    public static Paint dotPaint;
    public static Paint linePaint;
    public static Dot touchedDot;
    public static Paint anglePaint;
    public static Paint scaledotPaint;
    public static int numSetScaleDots = 0;
    public static Paint scalelinePaint;
    private final int MAX_DOTS = 3;
    public static int paintColor = 0;
    public static float scale = 1;
    public static int x1 = 0;
    public static int y1 = 0;
    public static double scaleFactor = 0;
    public static boolean degree;
    public static boolean dragged;


    // --------------------------------------- DELETE IF FAILS
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
    float oldDist = 1f;
    private float xCoOrdinate, yCoOrdinate;
    public static boolean setScaleMode = false;
    // -------------------------------------------
    public static ArrayList<Integer> numDots = new ArrayList<>();
    public static int currAngle = 0;
    public static final ArrayList<ArrayList<Dot>> angleList = new ArrayList<ArrayList<Dot>>();
    // -----------------------------------------

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
        dotPaint.setAlpha(100);
        scaledotPaint = new Paint();
        scaledotPaint.setColor(Color.GREEN);
        scaledotPaint.setAlpha(100);
        linePaint = new Paint();
        linePaint.setStrokeWidth(5/scale);
        linePaint.setColor(Color.WHITE);
        scalelinePaint = new Paint();
        scalelinePaint.setStrokeWidth(5/scale);
        scalelinePaint.setColor(Color.GREEN);
        anglePaint = new Paint();
        anglePaint.setColor(Color.RED);
        anglePaint.setTextSize(50/scale);
        anglePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        invalidate();

        for (int i = 0; i < 100; ++i)
        {
            numDots.add(0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dotPaint != null && linePaint != null) {
            if (DrawableDotImageView.paintColor == 0) {
                dotPaint.setColor(Color.RED);
                linePaint.setColor(Color.RED);
                anglePaint.setColor(Color.RED);
            }

            if (DrawableDotImageView.paintColor == 1) {
                dotPaint.setColor(Color.GRAY);
                linePaint.setColor(Color.GRAY);
                anglePaint.setColor(Color.GRAY);
            }

            if (DrawableDotImageView.paintColor == 2) {
                dotPaint.setColor(Color.BLACK);
                linePaint.setColor(Color.BLACK);
                anglePaint.setColor(Color.BLACK);
            }

            if (DrawableDotImageView.paintColor == 3) {
                dotPaint.setColor(Color.WHITE);
                linePaint.setColor(Color.WHITE);
                anglePaint.setColor(Color.WHITE);
            }
        }

        if (angleList.size() != 0) {
            for (int j = 0; j < angleList.size(); ++j) {
                if (numDots.get(j) != 0) {
                    for (int i = 0; i < angleList.get(j).size(); ++i) {
                        canvas.drawCircle(angleList.get(j).get(i).getX(), angleList.get(j).get(i).getY(), angleList.get(j).get(i).getRadius() / (2 * scale), dotPaint);
                        Log.d("ImageView", "Drawing X: " + angleList.get(j).get(i).x + " Y: " + angleList.get(j).get(i).y);
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
                }
            }
        }

        setScaleDots.forEach((dot) -> {
            canvas.drawCircle(dot.getX(), dot.getY(), dot.getRadius()/(2*scale), scaledotPaint);
            System.out.println("Drawing scale dot : " + dot.x + " Y: " + dot.y);
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                viewTransformation(v, event);

                setScaleDots.forEach((dot) -> {
                    if (dot.isInside((event.getX()), event.getY())) {
                        touchedDot = dot;
                        mode = NONE;
                        dragged = false;
                        Log.d("ImageView", "Dot touched");
                    } else {
                        viewTransformation(v, event);
                    }
                });

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
                            }

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
                            }
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
                                }
                            }
                        }
                    }
                }
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                viewTransformation(v, event);
                if (touchedDot != null) {
                    touchedDot.x = event.getX();
                    touchedDot.y = event.getY();
                    invalidate();
                    dragged = false;
                    mode = NONE;
                    Log.d("ImageView", "Dot moving X: " + touchedDot.x + " Y: " + touchedDot.y);
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                    if (touchedDot != null) {
                        touchedDot = null;

                    } else {

                        if (setScaleMode == true && numSetScaleDots < 2) {
                            setScaleDots.add(new Dot(event.getX(), event.getY(), 35));
                            ++numSetScaleDots;
                            invalidate();
                            if (numSetScaleDots == 2) {
                                setScaleMode = true;
                                FreeformAngleMeasureActivity.angleView.setText("");
                                FreeformAngleMeasureActivity.angleView.setHint("What is this distance? ");

                            }
                        } else {
                            if (mode == DRAG && dragged == false && !justDeleted) {
                                ArrayList<Dot> temp;

                                if (numDots.get(currAngle) <= MAX_DOTS - 1) {
                                    if (numDots.get(currAngle) != 0) {
                                        temp = angleList.get(currAngle);
                                    } else {
                                        temp = new ArrayList<Dot>();
                                    }

                                    temp.add(new Dot(event.getX(), event.getY(), 35));

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
                                    temp.add(new Dot(event.getX(), event.getY(), 35));
                                    angleList.add(temp);

                                    numDots.set(currAngle, (numDots.get(currAngle) + 1));
                                    invalidate();
                                    Log.d("ImageView", "Dot created X: " + event.getX() + " Y: " + event.getY());

                                    invalidate();
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

    // ------------------------------------------------------- ADDED CODE -------------------------
    private void viewTransformation(View view, MotionEvent event) {
        dragged = false;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xCoOrdinate = view.getX() - event.getRawX();
                yCoOrdinate = view.getY() - event.getRawY();

                start.set(event.getX(), event.getY());
                isOutSide = false;
                mode = DRAG;
                lastEvent = null;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    midPoint(mid, event);
                    mode = ZOOM;
                }

                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;

            case MotionEvent.ACTION_UP:
                if (mode == DRAG) {
                    float x = event.getX();
                    float y = event.getY();
                }
            case MotionEvent.ACTION_OUTSIDE:
                isOutSide = true;
                mode = NONE;
                lastEvent = null;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isOutSide) {
                    if (mode == DRAG) {
                        view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                        dragged = true;
                    }
                    if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist1 = spacing(event);
                        if (newDist1 > 10f) {
                            scale = newDist1 / oldDist * view.getScaleX();
                            // Can't zoom to scale less than default
                            if (scale < 1.0f)
                            {
                                scale = 1.0f;
                            }
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event);
                            getRotation = view.getRotation();
                            view.setRotation((float) (view.getRotation() + (newRot - d)));
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
        if (mindistance <= 25)
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

// TODO: pressing: whole pointsView moves when a point is moved. why?
// TODO: fix zooming: make it over user's fingerprint average and not the center of
// TODO: the pointsview


