package com.example.protester;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdcTestActivity extends TestBaseActivity {
    private TextView tvAdcStatus;
    private TextView tvAdcValue;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 12;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_adc);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvAdcStatus = findViewById(R.id.tv_adc_status);
        tvAdcValue = findViewById(R.id.tv_adc_value);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_adc));
        tvAdcStatus.setText("ADC未测试");
        tvAdcStatus.setVisibility(View.VISIBLE);
        tvAdcValue.setVisibility(View.VISIBLE);
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
        updateStatus("正在检测ADC按键...", R.color.status_testing);
        tvAdcStatus.setText("检测中...");

        // 模拟ADC按键检测
        simulateAdcDetection();
    }

    private void simulateAdcDetection() {
        Runnable adcCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // 模拟ADC值检测
                int simulatedAdcValue = (int) (Math.random() * 4096); // 0-4095

                runOnUiThread(() -> {
                    tvAdcValue.setText(String.format("ADC值: %d", simulatedAdcValue));

                    // 模拟按键按下检测
                    if (simulatedAdcValue > 3000) {
                        tvAdcStatus.setText("检测到按键按下");
                        tvAdcStatus.setTextColor(getResources().getColor(R.color.status_pass));
                        updateStatus("ADC按键检测正常", R.color.status_pass);
                        testPassed();
                        handler.removeCallbacks(this);
                    } else {
                        tvAdcStatus.setText("等待按键按下");
                        // 继续检测
                        handler.postDelayed(this, 500);
                    }
                });
            }
        };

        handler.postDelayed(adcCheckRunnable, 1000);
    }

    @Override
    protected void testPassed() {
        handler.removeCallbacksAndMessages(null);
        super.testPassed();
    }

    @Override
    protected void testFailed(String errorMessage) {
        handler.removeCallbacksAndMessages(null);
        super.testFailed(errorMessage);
    }

    @Override
    protected void testSkipped() {
        handler.removeCallbacksAndMessages(null);
        super.testSkipped();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}