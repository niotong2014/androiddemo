package com.example.protester;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicTestActivity extends TestBaseActivity {
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord audioRecord;
    private AtomicBoolean isRecording = new AtomicBoolean(false);
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable volumeCheckRunnable;
    private TextView tvVolumeLevel;
    private TextView tvMicStatus;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 1;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_mic);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvVolumeLevel = findViewById(R.id.tv_volume_level);
        tvMicStatus = findViewById(R.id.tv_mic_status);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_mic));
        tvVolumeLevel.setText("音量级别: 0");
        tvVolumeLevel.setVisibility(View.VISIBLE);
        tvMicStatus.setText("MIC未启动");
        tvMicStatus.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setupClickListeners() {
        Button btnStartTest = findViewById(R.id.btn_start_test);
        Button btnPass = findViewById(R.id.btn_pass);
        Button btnFail = findViewById(R.id.btn_fail);
        Button btnSkip = findViewById(R.id.btn_skip);

        btnStartTest.setOnClickListener(v -> startTest());
        btnPass.setOnClickListener(v -> testPassed());
        btnFail.setOnClickListener(v -> testFailed("手动标记为失败"));
        btnSkip.setOnClickListener(v -> testSkipped());
    }

    @Override
    protected void performTest() {
        updateStatus("正在初始化MIC...", R.color.status_testing);

        try {
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                testFailed("MIC初始化失败");
                return;
            }

            startRecording();

        } catch (SecurityException e) {
            testFailed("缺少MIC权限: " + e.getMessage());
        } catch (Exception e) {
            testFailed("MIC测试异常: " + e.getMessage());
        }
    }

    private void startRecording() {
        isRecording.set(true);
        audioRecord.startRecording();

        updateStatus("正在检测音量...", R.color.status_testing);
        tvMicStatus.setText("MIC正在录音");

        volumeCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRecording.get()) {
                    return;
                }

                short[] audioBuffer = new short[BUFFER_SIZE];
                int bytesRead = audioRecord.read(audioBuffer, 0, BUFFER_SIZE);

                if (bytesRead > 0) {
                    double volume = calculateVolume(audioBuffer, bytesRead);
                    updateVolumeDisplay(volume);

                    // 检测到足够音量则认为测试通过
                    if (volume > 1000) {
                        stopRecording();
                        testPassed();
                        return;
                    }
                }

                // 持续检测
                handler.postDelayed(this, 500);
            }
        };

        handler.postDelayed(volumeCheckRunnable, 1000);
    }

    private void stopRecording() {
        isRecording.set(false);
        if (volumeCheckRunnable != null) {
            handler.removeCallbacks(volumeCheckRunnable);
        }
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    private double calculateVolume(short[] audioBuffer, int length) {
        long sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Math.abs(audioBuffer[i]);
        }
        return (double) sum / length;
    }

    private void updateVolumeDisplay(double volume) {
        String volumeText = String.format("音量级别: %.0f", volume);
        tvVolumeLevel.setText(volumeText);
    }

    @Override
    protected void testPassed() {
        stopRecording();
        super.testPassed();
    }

    @Override
    protected void testFailed(String errorMessage) {
        stopRecording();
        super.testFailed(errorMessage);
    }

    @Override
    protected void testSkipped() {
        stopRecording();
        super.testSkipped();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecording();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}