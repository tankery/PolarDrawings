package me.tankery.demo.polardrawings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.SystemClock;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import me.tankery.demo.polardrawings.data.PathGenerator;
import me.tankery.demo.polardrawings.data.SineWaveGenerator;

/**
 * Created by tankery on 8/27/15.
 *
 * A View can display the animated sine wave, and the wave can be dynamic changed by input.
 */
public class DynamicSineWaveView extends View {

    // Each cycle contains at least 10 points
    private static final int POINTS_FOR_CYCLE = 10;

    public static class Wave {
        public float amplitude; // wave amplitude, relative to view, range from 0 ~ 0.5
        public float cycle;     // contains how many cycles in scene
        public float speed;     // space per second, relative to view. X means the wave move X times of width per second

        public Wave(float a, float c, float s) {
            this.amplitude = a;
            this.cycle = c;
            this.speed = s;
        }

        public Wave(Wave other) {
            this.amplitude = other.amplitude;
            this.cycle = other.cycle;
            this.speed = other.speed;
        }
    }

    private final List<Paint> wavePaints = new ArrayList<>();
    private final Matrix wavePathScale = new Matrix();
    private List<Path> currentPaths = new ArrayList<>();
    private Path drawingPath = new Path();

    private int viewWidth = 0;
    private int viewHeight = 0;

    private float baseWaveAmplitudeFactor = 1f;

    private static class TransferData {

        public final List<Wave> waveConfigs = new ArrayList<>();

        /**
         * The computation result, a set of wave path can draw.
         */
        public final BlockingQueue<List<Path>> transferPathsQueue = new LinkedBlockingQueue<>(1);

        /**
         * animate ticker will set this to true to request further data change.
         * computer set this to false before compute, after computation, if this still false,
         * stop and wait for next request, or continue to compute next data change.
         *
         * After data change, the computer post a invalidate to redraw.
         */
        public boolean requestFutureChange = false;
        public final Object requestCondition = new Object();

        public long startAnimateTime = 0;

    }

    private TransferData transferData = new TransferData();

    private Runnable animateTicker = new Runnable() {
        public void run() {
            requestUpdateFrame();
            ViewCompat.postOnAnimation(DynamicSineWaveView.this, this);
        }
    };

    public DynamicSineWaveView(Context context) {
        this(context, null);
    }

    public DynamicSineWaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicSineWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            addWave(1.0f, 0.5f, 0, 0, 0);
            addWave(0.5f, 2.5f, 0, Color.BLUE, 2);
            addWave(0.3f, 2f, 0, Color.RED, 2);
            setBaseWaveAmplitudeScale(1);
            createComputationThread().tick();
            return;
        }

        createComputationThread().start();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    /**
     * Add new wave to the view.
     *
     * The first added wave will become the 'base wave', which ignored the color & stroke, and
     * other wave will multiple with the 'base wave'.
     *
     * @param amplitude wave amplitude, relative to view, range from 0 ~ 0.5
     * @param cycle contains how many cycles in scene
     * @param speed space per second, relative to view. X means the wave move X times of width per second
     * @param color the wave color, ignored when add the 'base wave'
     * @param stroke wave stroke width, in pixel, ignored when add the 'base wave'
     * @return wave count (exclude the base wave).
     */
    public int addWave(float amplitude, float cycle, float speed, int color, float stroke) {
        synchronized (transferData.waveConfigs) {
            transferData.waveConfigs.add(new Wave(amplitude, cycle, speed));
        }

        if (transferData.waveConfigs.size() > 1) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStrokeWidth(stroke);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            wavePaints.add(paint);
        }

        return wavePaints.size();
    }

    public void clearWave() {
        synchronized (transferData.waveConfigs) {
            transferData.waveConfigs.clear();
        }
        wavePaints.clear();
    }

    /**
     * Start to animate the sine waves.
     */
    public void startAnimation() {
        transferData.startAnimateTime = SystemClock.uptimeMillis();
        removeCallbacks(animateTicker);
        ViewCompat.postOnAnimation(this, animateTicker);
    }

    /**
     * Stop the sine waves.
     */
    public void stopAnimation() {
        removeCallbacks(animateTicker);
    }

    /**
     * Scale sine waves.
     * @param scale set the scale for sine waves.
     */
    public void setBaseWaveAmplitudeScale(float scale) {
        baseWaveAmplitudeFactor = scale;
    }

    public float getBaseWaveAmplitudeFactor() {
        return baseWaveAmplitudeFactor;
    }

    /**
     * Update just one frame.
     */
    public void requestUpdateFrame() {
        synchronized (transferData.requestCondition) {
            transferData.requestFutureChange = true;
            transferData.requestCondition.notify();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!transferData.transferPathsQueue.isEmpty()) {
            currentPaths = transferData.transferPathsQueue.poll();

            if (currentPaths.size() != wavePaints.size()) {
                throw new RuntimeException("Generated paths size " + currentPaths.size() +
                        " not match the paints size " + wavePaints.size());
            }
        }

        if (currentPaths.isEmpty())
            return;

        float minSide = viewWidth < viewHeight ? viewWidth : viewHeight;
        wavePathScale.setScale(minSide, minSide * baseWaveAmplitudeFactor);
        wavePathScale.postTranslate(0, viewHeight / 2);
        for (int i = 0; i < currentPaths.size(); i++) {
            Path path = currentPaths.get(i);
            Paint paint = wavePaints.get(i);

            drawingPath.set(path);
            drawingPath.transform(wavePathScale);
            canvas.drawPath(drawingPath, paint);
        }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        viewWidth = getWidth();
        viewHeight = getHeight();
    }

    private ComputationThread createComputationThread() {
        return new ComputationThread(transferData, this);
    }

    private static class ComputationThread extends Thread {

        private final PathGenerator pathGenerator = new PathGenerator();
        private final SineWaveGenerator waveGenerator = new SineWaveGenerator();

        private TransferData transferData = new TransferData();
        private WeakReference<View> hostRef;

        public ComputationThread(TransferData transferData, View host) {
            this.transferData = transferData;
            this.hostRef = new WeakReference<>(host);
        }

        @Override
        public void run() {
            while (isAlive()) {
                synchronized (transferData.requestCondition) {
                    try {
                        if (!transferData.requestFutureChange)
                            transferData.requestCondition.wait();
                        if (!transferData.requestFutureChange)
                            continue;
                        transferData.requestFutureChange = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // If tick has new data offered, post a invalidate notify.
                View host  = hostRef.get();
                if (tick() && host != null) {
                    host.postInvalidate();
                }
            }
        }

        public boolean tick() {
            List<Wave> waveList;
            List<Path> newPaths;

            synchronized (transferData.waveConfigs) {
                if (transferData.waveConfigs.size() < 2)
                    return false;

                waveList = new ArrayList<>(transferData.waveConfigs.size());
                newPaths = new ArrayList<>(transferData.waveConfigs.size());

                for (Wave o : transferData.waveConfigs) {
                    waveList.add(new Wave(o));
                }
            }

            long currentTime = SystemClock.uptimeMillis();
            float t = (currentTime - transferData.startAnimateTime) / 1000f;

            float maxCycle = 0;
            float maxAmplitude = 0;
            for (int i = 0; i < waveList.size(); i++) {
                Wave w = waveList.get(i);
                if (w.cycle > maxCycle)
                    maxCycle = w.cycle;
                if (w.amplitude > maxAmplitude && i > 0)
                    maxAmplitude = w.amplitude;
            }
            int pointCount = (int) (POINTS_FOR_CYCLE * maxCycle);

            Wave baseWave = waveList.get(0);
            waveList.remove(0);

            float normal = baseWave.amplitude / maxAmplitude;
            List<PointF> baseWavePoints = waveGenerator.generateSineWave(
                    baseWave.amplitude, baseWave.cycle, -baseWave.speed * t, pointCount);

            for (Wave w : waveList) {
                float space = - w.speed * t;

                List<PointF> wavePoints = waveGenerator.generateSineWave(
                        w.amplitude, w.cycle, space, pointCount);
                if (wavePoints.size() != baseWavePoints.size()) {
                    throw new RuntimeException("base wave point size " + baseWavePoints.size() +
                            " not match the sub wave point size " + wavePoints.size());
                }

                // multiple up
                for (int i = 0; i < wavePoints.size(); i++) {
                    PointF p = wavePoints.get(i);
                    PointF base = baseWavePoints.get(i);
                    p.set(p.x, p.y * base.y * normal);
                }

                List<PointF> converted = waveGenerator.convertPolarToCartesian(wavePoints);
                Path path = pathGenerator.generatePath(converted);
                newPaths.add(path);
            }

            // offer the new wave paths & post invalidate to redraw.
            transferData.transferPathsQueue.offer(newPaths);
            return true;
        }

    }

}
