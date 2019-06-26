package de.hwse.houghlines;

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Tracing {

    ImagePlus imp;
    int yStart;
    int yStop;
    int xCenter;
    int pointListCapacity;

    public void setup(String arg, ImagePlus imp) {
        this.imp = imp;
    }

    public void run(ByteProcessor bp, List<Point> leftStartPoint, List<Point> rightStartPoint) {

        //Vorverarbeitung
        //ByteProcessor bp = preprocessing(ip);

        //Parameters
        yStart = bp.getHeight() - 2;
        yStop = (int)(bp.getHeight() * Parameters.horizonRatio);
        xCenter = bp.getWidth() / 2;
        pointListCapacity = (yStart-yStop) / Parameters.stepSize;

        //Right Lane
        List<Point> rPoints = rightStartPoint == null ? startR(bp) : rightStartPoint;
        trace(bp,rPoints);

        //Left Lane
        List<Point> lPoints = leftStartPoint == null ? startL(bp): leftStartPoint;
        trace(bp,lPoints);

        //Make Overlay
        Overlay overlay = new Overlay();
        ImageRoi edges = new ImageRoi(0,0,bp);
        edges.setOpacity(Parameters.edgeOverlayOpacity);
        overlay.add(edges);
        overlay.add(makeRoi(rPoints));
        overlay.add(makeRoi(lPoints));

        overlay.setStrokeColor(Color.RED);
        imp.setOverlay(overlay);
        imp.show();
        //imp.setImage(bp.getBufferedImage());

        System.out.print("right");
        for (Point p: rPoints) {
            System.out.print("," + p.x + "/" + p.y);
        }
        System.out.println();
        System.out.print("left");
        for (Point p: lPoints) {
            System.out.print("," + p.x + "/" + p.y);
        }
        System.out.println();
    }


    private List<Point> startR(ByteProcessor bp){
        List<Point> points = new ArrayList<Point>(pointListCapacity);

        for(int x = xCenter; x < bp.getWidth(); x++){
            if (bp.get(x, yStart) == 255){
                points.add(new Point(x+(Parameters.stepSize/2), yStart+Parameters.stepSize));
                points.add(new Point(x, yStart));
                break;
            }
        }

        if (points.size() == 0){
            points.add(new Point(bp.getWidth()-1, yStart+Parameters.stepSize));
            points.add(new Point(bp.getWidth()-1, yStart));
        }

        return points;
    }

    private List<Point> startL(ByteProcessor bp){
        List<Point> points = new ArrayList<Point>(pointListCapacity);

        for(int x = xCenter; x >= 0; x--){
            if (bp.get(x, yStart) == 255){
                points.add(new Point(x-(Parameters.stepSize/2), yStart+Parameters.stepSize));
                points.add(new Point(x, yStart));
                break;
            }
        }

        if (points.size() == 0){
            points.add(new Point(0, yStart+Parameters.stepSize));
            points.add(new Point(0, yStart));
        }

        return points;
    }

    private void traceR(ByteProcessor bp, List<Point> points){
        final int additionalSearchArea = Parameters.stepSize/2;

        for (int y = yStart-Parameters.stepSize; y >= yStop; y-=Parameters.stepSize){
            Point currentPoint = points.get(points.size()-1);
            Point lastPoint = points.get(points.size()-2);
            int xSlopeOffset = lastPoint.x - currentPoint.x;
            int searchStart = (currentPoint.x - (Parameters.stepSize + additionalSearchArea)) - xSlopeOffset;
            int searchStop = (currentPoint.x + (Parameters.stepSize + additionalSearchArea)) - xSlopeOffset;

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

    private void trace(ByteProcessor bp, List<Point> points){
        List<Point> pointbuffer = new ArrayList<Point>(pointListCapacity);
        boolean gap = false;

        for (int y = yStart-Parameters.stepSize; y >= yStop; y-=Parameters.stepSize){
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

    private Roi makeRoi(List<Point> points){
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
