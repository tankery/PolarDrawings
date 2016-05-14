package me.tankery.demo.polardrawings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final float[][] SPEECH_WAVE_CONFIGS = {
            {1.0f, 0.5f, 0},    // Base wave, full amplitude, half cycle, no speed.
            {0.2f, 4.5f, 0.2f}, // bottom wave, small amplitude, 4.5 cycle, slow speed.
            {0.3f, 3.5f, 0.3f}, // middle wave, middle amplitude, 3.5 cycle, middle speed.
            {0.5f, 3.0f, 0.5f}, // top wave, full amplitude, 3.0 cycle, fast speed.
    };

    private static final int[][] SPEECH_WAVE_VISUAL = {
            {0, 0},                                         // base wave, no visual configs.
            {R.color.colorPrimaryDark, R.dimen.view_speech_wave_stroke},   // bottom wave, transparent, 2 stroke width
            {R.color.colorPrimaryDark, R.dimen.view_speech_wave_stroke},   // middle wave, transparent, 2 stroke width.
            {R.color.colorAccent,             R.dimen.view_speech_wave_stroke},   // top wave, opaque, 2 stroke width.
    };
    private static final float SMOOTH_ALPHA = 0.5f;

    DynamicSineWaveView viewPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPanel = (DynamicSineWaveView) findViewById(R.id.view_panel);

        viewPanel.clearWave();
        for (int i = 0; i < SPEECH_WAVE_CONFIGS.length; i++) {
            float[] config = SPEECH_WAVE_CONFIGS[i];
            int[] visual = SPEECH_WAVE_VISUAL[i];
            int color = visual[0] > 0 ? getResources().getColor(visual[0]) : 0;
            float stroke = visual[1] > 0 ? getResources().getDimension(visual[1]) : 0;
            viewPanel.addWave(config[0], config[1], config[2], color, stroke);
        }
        viewPanel.startAnimation();
    }

    @Override
    protected void onDestroy() {
        viewPanel.stopAnimation();
        super.onDestroy();
    }
}
