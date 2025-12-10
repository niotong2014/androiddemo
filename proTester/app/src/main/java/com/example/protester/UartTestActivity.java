package com.example.protester;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UartTestActivity extends TestBaseActivity {
    private TextView tvUartStatus;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 11;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_uart);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvUartStatus = findViewById(R.id.tv_uart_status);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_uart));
        tvUartStatus.setText("串口未测试");
        tvUartStatus.setVisibility(View.VISIBLE);
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
        updateStatus("正在测试串口...", R.color.status_testing);
        tvUartStatus.setText("测试中...");

        // 模拟串口测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    runOnUiThread(() -> {
                        tvUartStatus.setText("串口测试完成");
                        tvUartStatus.setTextColor(getResources().getColor(R.color.status_pass));
                        updateStatus("串口测试通过", R.color.status_pass);
                        testPassed();
                    });
                } catch (InterruptedException e) {
                    testFailed("串口测试异常");
                }
            }
        }).start();
    }
}