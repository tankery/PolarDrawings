package me.tankery.demo.polardrawings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

import me.tankery.demo.polardrawings.data.DataPointsGenerator;

/**
 * Created by tankery on 1/17/16.
 *
 * custom drawing panel view.
 */
public class PanelView extends View {

    public static final boolean DRAW_POINTS = false;

    public PanelView(Context context) {
        super(context);
        initPaint();
        initPath();
    }

    public PanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        initPath();
    }

    public PanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        initPath();
    }

    private final Paint strokePaint = new Paint();
    private final Paint pointPaint = new Paint();
    private final Paint coordinatePaint = new Paint();

    private final PathGenerator pathGenerator = new PathGenerator();
    private final DataPointsGenerator pointsGenerator = new DataPointsGenerator();
    private List<PointF> drawingPoints;
    private Path drawingPath = new Path();

    private float width = getWidth();
    private float height = getHeight();
    private float halfWidth = width / 2;
    private float halfHeight = height / 2;

    private void initPaint() {
        strokePaint.setAntiAlias(true);
        strokePaint.setDither(true);
        strokePaint.setColor(Color.BLUE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(6);

        coordinatePaint.set(strokePaint);
        coordinatePaint.setStrokeWidth(2);
        coordinatePaint.setARGB(0xaa, 0, 0x77, 0);

        pointPaint.set(strokePaint);
        pointPaint.setColor(Color.RED);
        pointPaint.setStrokeWidth(3);
    }

    private void initPath() {
        drawingPoints = pointsGenerator.generatePointsFromPolar();
        drawingPath = pathGenerator.generatePath(drawingPoints);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        pointsGenerator.resize(w, h);
        width = w;
        height = h;
        halfWidth = width / 2;
        halfHeight = height / 2;
        initPath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(width / 2, height / 2);
        canvas.drawLine(-halfWidth, 0, halfWidth, 0, coordinatePaint);
        canvas.drawLine(0, -halfHeight, 0, halfHeight, coordinatePaint);


        canvas.drawPath(drawingPath, strokePaint);

        if (DRAW_POINTS) {
            for (PointF point : drawingPoints) {
                canvas.drawCircle(point.x, point.y, 10, pointPaint);
            }
        }
    }
}
