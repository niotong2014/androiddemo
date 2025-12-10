package com.example.protester;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WiFiTestActivity extends TestBaseActivity {
    private TextView tvWiFiStatus;
    private TextView WiFiInfo;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 4;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_wifi);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvWiFiStatus = findViewById(R.id.tv_wifi_status);
        WiFiInfo = findViewById(R.id.tv_wifi_info);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_wifi));
        tvWiFiStatus.setText("WiFi未检查");
        tvWiFiStatus.setVisibility(View.VISIBLE);
        WiFiInfo.setText("点击开始测试WiFi连接");
        WiFiInfo.setVisibility(View.VISIBLE);
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
        updateStatus("正在检查WiFi...", R.color.status_testing);
        tvWiFiStatus.setText("检查中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                checkWiFiStatus();
            }
        }).start();
    }

    private void checkWiFiStatus() {
        ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            testFailed("无法获取网络管理器");
            return;
        }

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null &&
                            activeNetworkInfo.isConnected() &&
                            activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);

                    String ssid = "未知网络";
                    int rssi = -100;

                    if (wifiManager != null) {
                        ssid = wifiManager.getConnectionInfo().getSSID() != null ?
                              wifiManager.getConnectionInfo().getSSID() : "未知网络";
                        rssi = wifiManager.getConnectionInfo().getRssi();
                    }

                    String signalStrength = getSignalStrength(rssi);

                    tvWiFiStatus.setText("已连接");
                    tvWiFiStatus.setTextColor(getResources().getColor(R.color.status_pass));

                    WiFiInfo.setText(String.format(
                        "SSID: %s\n信号强度: %d (%s)\n状态: 已连接",
                        ssid, rssi, signalStrength
                    ));

                    updateStatus("WiFi连接正常", R.color.status_pass);

                    // 自动通过测试
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            testPassed();
                        }
                    }).start();
                } else {
                    tvWiFiStatus.setText("未连接");
                    tvWiFiStatus.setTextColor(getResources().getColor(R.color.status_fail));

                    WiFiInfo.setText("WiFi未连接或不可用\n请检查WiFi设置");

                    updateStatus("WiFi连接失败", R.color.status_fail);
                    testFailed("WiFi未连接");
                }
            }
        });
    }

    private String getSignalStrength(int rssi) {
        if (rssi > -50) return "优秀";
        if (rssi > -60) return "良好";
        if (rssi > -70) return "一般";
        if (rssi > -80) return "较差";
        return "极差";
    }

    @Override
    protected void testPassed() {
        super.testPassed();
    }

    @Override
    protected void testFailed(String errorMessage) {
        super.testFailed(errorMessage);
    }
}