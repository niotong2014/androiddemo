package com.example.protester;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GpioTestActivity extends TestBaseActivity {
    private TextView tvGpioStatus;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 6;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_gpio);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvGpioStatus = findViewById(R.id.tv_gpio_status);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_gpio));
        tvGpioStatus.setText("GPIO未测试");
        tvGpioStatus.setVisibility(View.VISIBLE);
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
        updateStatus("正在测试GPIO...", R.color.status_testing);
        tvGpioStatus.setText("测试中...");

        // 模拟GPIO测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    runOnUiThread(() -> {
                        tvGpioStatus.setText("GPIO测试完成");
                        tvGpioStatus.setTextColor(getResources().getColor(R.color.status_pass));
                        updateStatus("GPIO测试通过", R.color.status_pass);
                        testPassed();
                    });
                } catch (InterruptedException e) {
                    testFailed("GPIO测试异常");
                }
            }
        }).start();
    }
}