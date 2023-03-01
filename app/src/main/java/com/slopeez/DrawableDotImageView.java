package com.slopeez;

import static com.slopeez.FreeformAngleMeasureActivity.angleView;
import static com.slopeez.FreeformAngleMeasureActivity.calcAngle;
import static com.slopeez.FreeformAngleMeasureActivity.removeCurr;
import static com.slopeez.FreeformAngleMeasureActivity.scaleDist;
import static com.slopeez.FreeformAngleMeasureActivity.thread;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
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
    public float scale = 1;
    public static int x1 = 0;
    public static int y1 = 0;
    public static double scaleFactor = 0;
    public static boolean degree;

    // TODO: toggle for deg/rad and for line/angle

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
        anglePaint.setTextSize(70/scale);
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
                setScaleDots.forEach((dot) -> {
                    if (dot.isInside((event.getX()), event.getY())) {
                        touchedDot = dot;
                        Log.d("ImageView", "Dot touched");
                    } else {
                        viewTransformation(v, event);
                    }
                });

                for (int i = 0; i < angleList.size(); ++i) {
                    ArrayList<Dot> dots = angleList.get(i);
                    int finalI = i;
                    dots.forEach((dot) -> {
                        if (dot.isInside((event.getX()), event.getY())) {
                            touchedDot = dot;
                            Log.d("ImageView", "Dot touched");

                            if (removeCurr)
                            {
                                angleList.remove(finalI);
                                angles.remove(finalI);
                                numDots.remove(finalI);
                                --currAngle;
                                touchedDot = null;
                                removeCurr = false;
                            }

                        } else {
                            viewTransformation(v, event);
                        }
                    });
                }
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchedDot != null) {
                    touchedDot.x = event.getX();
                    touchedDot.y = event.getY();
                    invalidate();
                    Log.d("ImageView", "Dot moving X: " + touchedDot.x + " Y: " + touchedDot.y);
                }
                else {
                    viewTransformation(v, event);
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                if (viewTransformation(v, event) == false) {
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

// TODO: create arraylist with all lines to display distances easily
                            }
                        } else {
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
                            } else if (numDots.get(currAngle) == 3) {
                                ++currAngle;
                                temp = new ArrayList<Dot>();
                                temp.add(new Dot(event.getX(), event.getY(), 35));
                                angleList.add(temp);

                                numDots.set(currAngle, (numDots.get(currAngle) + 1));
                                invalidate();
                                Log.d("ImageView", "Dot created X: " + event.getX() + " Y: " + event.getY());

                                invalidate();
                            }
                        }
                    }
                    viewTransformation(v, event);
                    invalidate();
                }
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
    private boolean viewTransformation(View view, MotionEvent event) {
        boolean didItTransform = false;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xCoOrdinate = view.getX() - event.getRawX();
                yCoOrdinate = view.getY() - event.getRawY();

                start.set(event.getX(), event.getY());
                isOutSide = false;
                mode = DRAG;
                lastEvent = null;
                return true;

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
                return true;

            case MotionEvent.ACTION_UP:
                isZoomAndRotate = false;
                if (mode == DRAG) {
                    float x = event.getX();
                    float y = event.getY();
                    didItTransform = true;
                }
            case MotionEvent.ACTION_OUTSIDE:
                isOutSide = true;
                mode = NONE;
                lastEvent = null;
                didItTransform = false;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                return false;
            case MotionEvent.ACTION_MOVE:
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false;
                        view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
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
                return true;
        }
        return didItTransform;
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

// TODO: bound points in angeview
// TODO: add functionality for mutliple angle dots