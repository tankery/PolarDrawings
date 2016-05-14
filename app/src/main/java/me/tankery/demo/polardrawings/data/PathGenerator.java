package me.tankery.demo.polardrawings.data;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.List;

/**
 * Generate path from point set.
 *
 * This generator use a cubic Bezier curve to connecting dots, so we can
 * draw a smooth curve with a few dots.
 *
 * Created by tankery on 1/18/16.
 */
public class PathGenerator {

    public Path generatePath(List<PointF> points) {
        Path path = new Path();

        if (points.isEmpty())
            return path;

        PointF prevDelta = new PointF(0, 0);
        for (int i = 0; i < points.size(); i++) {
            PointF prev = i - 1 < 0 ? null : points.get(i - 1);
            PointF next = i + 1 >= points.size() ? null : points.get(i + 1);
            PointF current = points.get(i);

            prevDelta = movePath(path, prev, current, next, prevDelta);
        }

        return path;
    }

    // Move path to current point, return current point dx, dy;
    private PointF movePath(Path path, PointF prev, PointF current, PointF next, PointF prevDelta) {
        float dx = 0;
        float dy = 0;
        if (prev == null) {
            if (next != null) {
                dx = ((next.x - current.x) / 3);
                dy = ((next.y - current.y) / 3);
            }
            path.moveTo(current.x, current.y);
        } else {
            if (next == null) {
                dx = ((current.x - prev.x) / 3);
                dy = ((current.y - prev.y) / 3);
            } else {
                dx = ((next.x - prev.x) / 6);
                dy = ((next.y - prev.y) / 6);
            }
            path.cubicTo(prev.x + prevDelta.x, prev.y + prevDelta.y,
                    current.x - dx, current.y - dy,
                    current.x, current.y);
        }

        return new PointF(dx, dy);
    }
}
