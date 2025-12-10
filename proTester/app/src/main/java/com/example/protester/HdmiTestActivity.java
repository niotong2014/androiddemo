package com.example.protester;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HdmiTestActivity extends TestBaseActivity {
    private TextView tvHdmiStatus;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 8;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_hdmi);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvHdmiStatus = findViewById(R.id.tv_hdmi_status);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_hdmi));
        tvHdmiStatus.setText("HDMI未测试");
        tvHdmiStatus.setVisibility(View.VISIBLE);
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
        updateStatus("正在测试HDMI...", R.color.status_testing);
        tvHdmiStatus.setText("测试中...");

        // 模拟HDMI测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    runOnUiThread(() -> {
                        tvHdmiStatus.setText("请连接HDMI显示器");
                        updateStatus("等待HDMI连接", R.color.status_testing);

                        // 继续等待连接
                        new Thread(() -> {
                            try {
                                Thread.sleep(5000);
                                runOnUiThread(() -> {
                                    tvHdmiStatus.setText("HDMI测试完成");
                                    tvHdmiStatus.setTextColor(getResources().getColor(R.color.status_pass));
                                    updateStatus("HDMI测试通过", R.color.status_pass);
                                    testPassed();
                                });
                            } catch (InterruptedException e) {
                                testFailed("HDMI测试异常");
                            }
                        }).start();
                    });
                } catch (InterruptedException e) {
                    testFailed("HDMI测试异常");
                }
            }
        }).start();
    }
}