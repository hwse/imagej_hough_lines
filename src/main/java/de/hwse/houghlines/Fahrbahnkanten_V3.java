package de.hwse.houghlines;

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
import java.util.*;

public class Fahrbahnkanten_V3 implements PlugInFilter {

    ImagePlus imp;
    int yStart;
    int yStop;
    int xCenter;
    int pointListCapacity;

    final int stepSize = 6;
    final float horizonRatio = 0.52f;
    final int binaryThreshold = 132;
    final float edgeOverlayOpacity = 0.5f;


    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    public void run(ImageProcessor ip) {

        //Vorverarbeitung
        ByteProcessor bp = preprocessing(ip);

        //Parameters
        yStart = bp.getHeight() - 2;
        yStop = (int)(bp.getHeight() * horizonRatio);
        xCenter = bp.getWidth() / 2;
        pointListCapacity = (yStart-yStop) / stepSize;

        //Right Lane
        ArrayList<Point> rPoints = startR(bp);
        traceR(bp,rPoints);

        //Left Lane
        ArrayList<Point> lPoints = startL(bp);
        traceL(bp,lPoints);

        //Make Overlay
        Overlay overlay = new Overlay();
        ImageRoi edges = new ImageRoi(0,0,bp);
        edges.setOpacity(edgeOverlayOpacity);
        overlay.add(edges);
        overlay.add(makeRoi(rPoints));
        overlay.add(makeRoi(lPoints));

        overlay.setStrokeColor(Color.RED);
        imp.setOverlay(overlay);
        imp.show();
        //imp.setImage(bp.getBufferedImage());
    }

    private ByteProcessor preprocessing(ImageProcessor ip){
        ByteProcessor bp = ip.convertToByteProcessor();
        bp.threshold(binaryThreshold);
        bp.dilate();
        //bp.erode();
        bp.findEdges();
        //bp.invert();
        //bp.skeletonize();
        return bp;
    }

    private ArrayList<Point> startR(ByteProcessor bp){
        ArrayList<Point> points = new ArrayList<Point>(pointListCapacity);

        for(int x = xCenter; x < bp.getWidth(); x++){
            if (bp.get(x, yStart) == 255){
                points.add(new Point(x+(stepSize/2), yStart+stepSize));
                points.add(new Point(x, yStart));
                break;
            }
        }

        if (points.size() == 0){
            points.add(new Point(bp.getWidth()-1, yStart+stepSize));
            points.add(new Point(bp.getWidth()-1, yStart));
        }

        return points;
    }

    private ArrayList<Point> startL(ByteProcessor bp){
        ArrayList<Point> points = new ArrayList<Point>(pointListCapacity);

        for(int x = xCenter; x >= 0; x--){
            if (bp.get(x, yStart) == 255){
                points.add(new Point(x-(stepSize/2), yStart+stepSize));
                points.add(new Point(x, yStart));
                break;
            }
        }

        if (points.size() == 0){
            points.add(new Point(0, yStart+stepSize));
            points.add(new Point(0, yStart));
        }

        return points;
    }

    private void traceR(ByteProcessor bp, ArrayList<Point> points){
        final int additionalSearchArea = stepSize/2;

        for (int y = yStart-stepSize; y >= yStop; y-=stepSize){
            Point currentPoint = points.get(points.size()-1);
            Point lastPoint = points.get(points.size()-2);
            int xSlopeOffset = lastPoint.x - currentPoint.x;
            int searchStart = (currentPoint.x - (stepSize + additionalSearchArea)) - xSlopeOffset;
            int searchStop = (currentPoint.x + (stepSize + additionalSearchArea)) - xSlopeOffset;

            //Try find next Point
            Point nextPoint = null;
            for (int x = searchStart; x <= searchStop; x++){
                if (bp.getPixel(x,y) == 255){
                    nextPoint = new Point(x,y);
                    break;
                }
            }

            if(nextPoint == null){
                nextPoint = new Point(currentPoint.x - xSlopeOffset, y);
            }

            points.add(nextPoint);
        }
    }

    private void traceL(ByteProcessor bp, ArrayList<Point> points){
        ArrayList<Point> pointbuffer = new ArrayList<Point>(pointListCapacity);
        boolean gap = false;

        for (int y = yStart-stepSize; y >= yStop; y-=stepSize){
            Point currentPoint = points.get(points.size() - 1);
            Point lastPoint = points.get(points.size() - 2);
            int dx = lastPoint.x - currentPoint.x;
            int dy = currentPoint.y - y;
            float slope = (float)dx / (float)(lastPoint.y - currentPoint.y);
            int xSlopeOffset = (int)(dy*slope);
            int expectedX = currentPoint.x - xSlopeOffset;
            int searchRange = dy*2;

            Point nextPoint = null;
            for (int x = 0; x <= searchRange; x++){
                if (bp.getPixel(expectedX + x, y) == 255){
                    nextPoint = new Point(expectedX + x, y);
                    break;
                }
                if (bp.getPixel(expectedX - x, y) == 255){
                    nextPoint = new Point(expectedX - x, y);
                    break;
                }
            }

            if (gap){
                if(nextPoint == null){
                    pointbuffer.add(new Point(expectedX, y));
                }
                else{
                    pointbuffer.clear();
                    gap = false;
                    points.add(nextPoint);
                }
            }
            else{
                if(nextPoint == null){
                    pointbuffer.add(new Point(expectedX, y));
                    gap = true;
                }
                else{
                    points.add(nextPoint);
                }
            }
        }
        if (!pointbuffer.isEmpty()){
            points.add(pointbuffer.get(pointbuffer.size()-1));
        }
    }

    private Roi makeRoi(ArrayList<Point> points){
        int[] xs = new int[points.size()];
        int[] ys = new int[points.size()];
        for (int i = 0; i < points.size(); i++){
            Point p = points.get(i);
            xs[i] = p.x;
            ys[i] = p.y;
        }
        return new PolygonRoi(xs,ys,points.size(),Roi.POLYLINE);
    }
}
