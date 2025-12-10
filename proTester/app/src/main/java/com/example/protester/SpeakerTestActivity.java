package com.example.protester;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeakerTestActivity extends TestBaseActivity {
    private SoundPool soundPool;
    private int soundId;
    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    private Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvSpeakerStatus;
    private TextView tvTestInfo;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 2;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_speaker);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvSpeakerStatus = findViewById(R.id.tv_speaker_status);
        tvTestInfo = findViewById(R.id.tv_test_info);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_speaker));
        tvSpeakerStatus.setText("喇叭未启动");
        tvSpeakerStatus.setVisibility(View.VISIBLE);
        tvTestInfo.setText("请仔细听喇叭是否有声音输出");
        tvTestInfo.setVisibility(View.VISIBLE);
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
        updateStatus("正在初始化音频系统...", R.color.status_testing);

        try {
            // 初始化SoundPool
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

            soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();

            // 创建测试音调
            soundId = createTestTone();

            if (soundId != -1) {
                updateStatus("正在播放测试音...", R.color.status_testing);
                tvSpeakerStatus.setText("喇叭正在播放测试音");
                playTestTone();
            } else {
                testFailed("无法生成测试音");
            }

        } catch (Exception e) {
            testFailed("喇叭测试异常: " + e.getMessage());
        }
    }

    private int createTestTone() {
        try {
            // 创建一个简单的440Hz正弦波测试音
            int sampleRate = 44100;
            int duration = 2000; // 2秒
            int samples = sampleRate * duration / 1000;
            short[] buffer = new short[samples];

            double frequency = 440.0; // A4音符
            for (int i = 0; i < samples; i++) {
                double angle = 2.0 * Math.PI * frequency * i / sampleRate;
                buffer[i] = (short) (Short.MAX_VALUE * Math.sin(angle));
            }

            // 创建临时文件
            java.io.File tempFile = new java.io.File(getExternalFilesDir(null), "test_tone.wav");
            tempFile.deleteOnExit();

            // 写入WAV文件
            try (java.io.DataOutputStream dos = new java.io.DataOutputStream(
                    new java.io.FileOutputStream(tempFile))) {
                // WAV文件头
                dos.writeBytes("RIFF");
                dos.writeInt(36 + samples * 2);
                dos.writeBytes("WAVE");
                dos.writeBytes("fmt ");
                dos.writeInt(16);
                dos.writeShort(1);
                dos.writeShort(1);
                dos.writeInt(sampleRate);
                dos.writeInt(sampleRate * 2);
                dos.writeShort(2);
                dos.writeShort(16);
                dos.writeBytes("data");
                dos.writeInt(samples * 2);

                // 音频数据
                for (short sample : buffer) {
                    dos.writeShort(sample);
                }
            }

            // 加载音频文件
            return soundPool.load(tempFile.getAbsolutePath(), 1);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void playTestTone() {
        isPlaying.set(true);

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0 && sampleId == soundId) {
                // 音频加载完成，开始播放
                soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);

                // 播放结束后停止
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopTest();
                        updateStatus("播放完成，请确认声音输出", R.color.status_idle);
                        tvSpeakerStatus.setText("播放完成");
                    }
                }, 2000);
            } else {
                testFailed("音频加载失败");
            }
        });

        // 5秒后自动停止（如果没有自动停止的话）
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlaying.get()) {
                    stopTest();
                    updateStatus("播放超时，请确认声音输出", R.color.status_fail);
                    tvSpeakerStatus.setText("播放超时");
                }
            }
        }, 5000);
    }

    private void stopTest() {
        isPlaying.set(false);
        if (soundPool != null) {
            soundPool.stop(soundId);
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    protected void testPassed() {
        stopTest();
        currentTestResult.setDetails("用户确认喇叭正常工作");
        super.testPassed();
    }

    @Override
    protected void testFailed(String errorMessage) {
        stopTest();
        super.testFailed(errorMessage);
    }

    @Override
    protected void testSkipped() {
        stopTest();
        super.testSkipped();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTest();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}