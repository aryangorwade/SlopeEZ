package com.slopeez;

import static com.slopeez.FreeformAngleMeasureActivity.aiVals;
import static com.slopeez.FreeformAngleMeasureActivity.pointsView;

import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint;
import org.opencv.core.CvType;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;

public class LineDetector {
    public static ArrayList<ArrayList<Point>> aiLinesList = new ArrayList<ArrayList<Point>>();

    public static void temp(Bitmap bmp) {
        Mat src = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, src);

        //Converting the image to Gray
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY);
        //Detecting the edges
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 60, 60*3, 3, false);
        // Changing the color of the canny
        Mat cannyColor = new Mat();
        Imgproc.cvtColor(edges, cannyColor, Imgproc.COLOR_GRAY2BGR);
        //Detecting the hough lines from (canny)
        Mat lines = new Mat();
        Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 150);
        for (int i = 0; i < lines.rows(); i++) {
            double[] data = lines.get(i, 0);
            double rho = data[0];
            double theta = data[1];
            double a = Math.cos(theta);
            double b = Math.sin(theta);
            double x0 = a*rho;
            double y0 = b*rho;
            //Drawing lines on the image
            Point pt1 = new Point();
            Point pt2 = new Point();
            pt1.x = Math.round(x0 + 1000*(-b));
            pt1.y = Math.round(y0 + 1000*(a));
            pt2.x = Math.round(x0 - 1000*(-b));
            pt2.y = Math.round(y0 - 1000 *(a));

            ArrayList<Point> currLine = new ArrayList<Point>();
            currLine.add(pt1);
            currLine.add(pt2);
            aiLinesList.add(currLine);
        }
    }

    public static void detectEdges(Bitmap bmp)
    {
        Mat img = new Mat();
        Mat gray = new Mat();
        Utils.bitmapToMat(bmp, img);
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 50, 150, 3, false);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI/180, aiVals[0], aiVals[1], aiVals[2]);

          //   Imgproc.HoughLinesP(edges, lines, 1, Math.PI/180, 100, 30, 10);

        float viewHeight = pointsView.getHeight();
        float viewWidth = pointsView.getWidth();
        float height = img.rows();
        float width = img.cols();
        float factorY = viewHeight/height;
        float factorX = viewWidth/width;
        // Store line coordinates
        for (int i = 0; i < lines.rows(); i++) {
            double[] data = lines.get(i, 0);
            double x1 = data[0], y1 = data[1], x2 = data[2], y2 = data[3];
            ArrayList<Point> dots = new ArrayList<Point>();
            dots.add(new Point(x1*factorX, ((y1)*factorY)));
            dots.add(new Point(x2*factorX, ((y2)*factorY)));
            aiLinesList.add(dots);

            System.out.println(x1*factorX + " "  + (y1+50)*factorY);
            System.out.println(x2*factorX + " "  + (y2+50)*factorY);
            // Draw lines on the image
            Imgproc.line(img, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 0, 255), 2);
        }
        System.out.println(aiLinesList);
    }

    /*
    public static void detectEdges(Bitmap bmp) {
        Mat src = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, src);

        //
        Mat dst = new Mat(), cdst = new Mat(), cdstP;
        // Edge detection
        Imgproc.Canny(src, dst, 50, 200, 3, false);
        // Copy edges to the images that will display the results in BGR
        Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR);
        cdstP = cdst.clone();
        // Standard Hough Line Transform
        Mat lines = new Mat(); // will hold the results of the detection
        Imgproc.HoughLines(dst, lines, 1, Math.PI/180, 150); // runs the actual detection
        // Draw the lines
        for (int x = 0; x < lines.rows(); x++) {
            double rho = lines.get(x, 0)[0],
                    theta = lines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a*rho, y0 = b*rho;
            Point pt1 = new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)));
            Point pt2 = new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
            ArrayList<Point> currLine = new ArrayList<Point>();
            currLine.add(pt1);
            currLine.add(pt2);
            System.out.println(currLine);
            linesList.add(currLine);

            Imgproc.line(cdst, pt1, pt2, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        }
    }
     */

    public static void reset()
    {
        aiLinesList.clear();
    }
}