package com.slopeez;

import static com.slopeez.FreeformAngleMeasureActivity.thread;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DrawableDotImageView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {

    public static final ArrayList<Dot> dots = new ArrayList<>();
    public static Paint dotPaint;
    public static Paint linePaint;
    public static Dot touchedDot;
    private final int MAX_DOTS = 3;
    public static int numDots = 0;
    public static int paintColor = 0;
    public float scale = 1;

    // --------------------------------------- DELETE IF FAILS
    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;
    private boolean isZoomAndRotate;
    private boolean isOutSide;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    float oldDist = 1f;
    private float xCoOrdinate, yCoOrdinate;
    // -------------------------------------------

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
        linePaint = new Paint();
        linePaint.setStrokeWidth(5/scale);
        linePaint.setColor(Color.WHITE);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dotPaint != null && linePaint != null) {
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
        }

        dots.forEach((dot) -> {
            canvas.drawCircle(dot.getX(), dot.getY(), dot.getRadius()/(2*scale), dotPaint);
            Log.d("ImageView", "Drawing X: " + dot.x + " Y: " + dot.y);
        });

        // draw lines between the three dots: 0 to 1 and 1 to 2
        if (numDots == 3)
        {
            //          graphics.drawLine
            linePaint.setStrokeWidth(5/scale);
            canvas.drawLine(dots.get(0).x, dots.get(0).y, dots.get(1).x, dots.get(1).y, linePaint);
            canvas.drawLine(dots.get(1).x, dots.get(1).y, dots.get(2).x, dots.get(2).y, linePaint);

        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO: problem: imageview covering status bar
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dots.forEach((dot) -> {
                    if (dot.isInside((event.getX()), event.getY())) {
                        touchedDot = dot;
                        Log.d("ImageView", "Dot touched");
                    }
                    else {
                        viewTransformation(v, event);
                    }
                });
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
                break;
            case MotionEvent.ACTION_UP:
                if (touchedDot != null) {
                    touchedDot = null;
                } else {
                    dotPaint.setColor(Color.WHITE);

                    if (numDots <= MAX_DOTS-1) {
                        dots.add(new Dot(event.getX(), event.getY(), 35));
                        ++numDots;
                        invalidate();
                        Log.d("ImageView", "Dot created X: " + event.getX() + " Y: " + event.getY());
                        if (numDots == MAX_DOTS)
                        {
                            thread.start();
                        }
                    }
                    viewTransformation(v, event);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                viewTransformation(v, event);
                break;
            default:
                viewTransformation(v, event);
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
                isZoomAndRotate = false;
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
                        isZoomAndRotate = false;
                        view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                    }
                    if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist1 = spacing(event);
                        if (newDist1 > 10f) {
                            scale = newDist1 / oldDist * view.getScaleX();
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event);
                            view.setRotation((float) (view.getRotation() + (newRot - d)));
                        }
                    }
                }
                break;
        }
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