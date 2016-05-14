package me.tankery.demo.polardrawings.data;

import android.graphics.PointF;

/**
 * Created by tankery on 1/18/16.
 *
 * A point in polar coordinate.
 */
public class PolarPointF {

    public double angle;
    public double radius;

    public PolarPointF() {
        this(0, 0);
    }

    public PolarPointF(double angle, double radius) {
        this.angle = angle;
        this.radius = radius;
    }

    public PointF toCartesian() {// polar to Cartesian
        float x = (float) ( Math.cos(angle) * radius );
        float y = (float) ( Math.sin(angle) * radius );
        return new PointF(x, y);
    }

    public static PolarPointF fromCartesian(PointF point) {
        // Cartesian to polar.
        float x = point.x;
        float y = point.y;
        double radius = Math.sqrt( x * x + y * y );
        double angleInRadians = Math.acos( x / radius );
        return new PolarPointF(angleInRadians, radius);
    }

}
