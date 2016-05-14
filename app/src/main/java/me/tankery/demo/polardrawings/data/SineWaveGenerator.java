package me.tankery.demo.polardrawings.data;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tankery on 1/18/16.
 *
 * Generate data points.
 */
public class SineWaveGenerator {

    // Each cycle contains at least 10 points
    private static final int POINTS_FOR_CYCLE = 10;

    public List<PointF> generateSineWave(float amplitude, float cycle, float offset, int pointCount) {
        int count = pointCount;
        if (count <= 0)
            count = (int) (POINTS_FOR_CYCLE * cycle);
        double T = 1. / cycle;
        double f = 1f / T;

        List<PointF> points = new ArrayList<>(count);
        if (count < 2)
            return points;

        double dx = 1. / (count - 1);
        for (double x = 0; x <= 1; x+= dx) {
            double y = amplitude * Math.sin(2 * Math.PI * f * (x + offset));
            points.add(new PointF((float) x, (float) y));
        }

        return points;
    }

    public List<PointF> convertPolarToCartesian(List<PointF> polarPoints) {
        final double kBaseRadius = 0.2;
        List<PointF> points = new ArrayList<>();
        for (PointF polar : polarPoints) {
            double angle = Math.PI + polar.x * Math.PI;   // half circle match x range
            double radius = kBaseRadius + polar.y / 50;

            PointF converted = new PolarPointF(angle, radius).toCartesian();
            converted.x += 0.5f; // normalize
            points.add(converted);
        }

        // Add next half circle
        for (PointF polar : polarPoints) {
            double angle = polar.x * Math.PI;   // next half circle
            double radius = kBaseRadius;

            PointF converted = new PolarPointF(angle, radius).toCartesian();
            converted.x += 0.5f; // normalize
            points.add(converted);
        }

        return points;
    }

}
