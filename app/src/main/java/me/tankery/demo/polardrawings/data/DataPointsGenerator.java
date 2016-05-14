package me.tankery.demo.polardrawings.data;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tankery on 1/18/16.
 *
 * Generate data points.
 */
public class DataPointsGenerator {

    private float width = 0;
    private float height = 0;
    private float halfWidth = 0;
    private float halfHeight = 0;

    public void resize(int w, int h) {
        width = w;
        height = h;
        halfWidth = width / 2;
        halfHeight = height / 2;
    }

    public List<PointF> generatePoints() {
        final float pointsCount = 7;
        final float pointStep = width / (pointsCount - 1);
        List<PointF> points = new ArrayList<>();
        for (int i = 0; i < pointsCount; i++) {
            float x = -halfWidth + i * pointStep;
            float normalizedX = x / halfWidth;
            points.add(new PointF(x, (float) (200 * Math.sin(4 * normalizedX))));
        }
        return points;
    }

    public List<PointF> generatePointsFromPolar() {
        List<PointF> wave = generateWave(0, Math.PI);
        List<PointF> wave2 = generateWave2(0, Math.PI);
        List<PointF> circle = generateCircle(Math.PI, 2 * Math.PI);
        List<PointF> points = new ArrayList<>();
        points.addAll(wave);
        points.addAll(circle);
        points.addAll(wave2);
        return points;
    }

    private List<PointF> generateWave(double start, double end) {
        final int pointsCount = 50;
        final double range = (end - start);
        final double pointStep = range / (pointsCount - 1);
        List<PointF> points = new ArrayList<>();
        for (int i = 0; i < pointsCount; i++) {
            double angle = start + i * pointStep;
            double waveRadius = getSineWaveY(1, 5, 0, angle / range);

            double baseRadius = getSineWaveY(1, 0.5, 0, angle / range);

            double radius = (width / 4) + (width / 50) * waveRadius * baseRadius;

            points.add(new PolarPointF(angle, radius).toCartesian());
        }
        return points;
    }

    private List<PointF> generateWave2(double start, double end) {
        final int pointsCount = 50;
        final double range = (end - start);
        final double pointStep = range / (pointsCount - 1);
        List<PointF> points = new ArrayList<>();
        for (int i = 0; i < pointsCount; i++) {
            double angle = start + i * pointStep;
            double waveRadius = getSineWaveY(1, 5, Math.PI, angle / range);

            double baseRadius = getSineWaveY(1, 0.5, 0, angle / range);

            double radius = (width / 4) + (width / 70) * waveRadius * baseRadius;

            points.add(new PolarPointF(angle, radius).toCartesian());
        }
        return points;
    }

    private List<PointF> generateCircle(double start, double end) {
        final int pointsCount = 50;
        final double pointStep = (end - start) / (pointsCount - 1);
        List<PointF> points = new ArrayList<>();
        for (int i = 0; i < pointsCount; i++) {
            double angle = start + i * pointStep;
            double radius = (width / 4);

            points.add(new PolarPointF(angle, radius).toCartesian());
        }
        return points;
    }

    private double getSineWaveY(double amplitude, double cycle, double offset,
                                          double x) {
        double T = 1. / cycle;
        double f = 1f / T;
        return amplitude * Math.sin(2 * Math.PI * f * (x + offset));
    }

}
