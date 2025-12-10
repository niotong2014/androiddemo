package com.example.protester;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EthernetTestActivity extends TestBaseActivity {
    private TextView tvEthernetStatus;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 10;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_ethernet);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvEthernetStatus = findViewById(R.id.tv_ethernet_status);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_ethernet));
        tvEthernetStatus.setText("以太网未测试");
        tvEthernetStatus.setVisibility(View.VISIBLE);
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
        updateStatus("正在检查以太网连接...", R.color.status_testing);
        tvEthernetStatus.setText("检查中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                checkEthernetConnection();
            }
        }).start();
    }

    private void checkEthernetConnection() {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            testFailed("无法获取网络管理器");
            return;
        }

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    String typeName = activeNetworkInfo.getTypeName();
                    if (typeName != null && typeName.toLowerCase().contains("ethernet")) {
                        tvEthernetStatus.setText("以太网已连接");
                        tvEthernetStatus.setTextColor(getResources().getColor(R.color.status_pass));
                        updateStatus("以太网连接正常", R.color.status_pass);
                        testPassed();
                    } else if (typeName != null && typeName.toLowerCase().contains("wifi")) {
                        tvEthernetStatus.setText("当前WiFi连接");
                        tvEthernetStatus.setTextColor(getResources().getColor(R.color.status_fail));
                        updateStatus("请连接以太网", R.color.status_fail);
                        testFailed("当前WiFi连接，请连接以太网");
                    } else {
                        tvEthernetStatus.setText("网络连接异常");
                        tvEthernetStatus.setTextColor(getResources().getColor(R.color.status_fail));
                        updateStatus("请连接以太网", R.color.status_fail);
                        testFailed("网络连接异常");
                    }
                } else {
                    tvEthernetStatus.setText("未连接网络");
                    tvEthernetStatus.setTextColor(getResources().getColor(R.color.status_fail));
                    updateStatus("请连接以太网", R.color.status_fail);
                    testFailed("未连接网络");
                }
            }
        });
    }
}