package com.example.protester;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UsbTestActivity extends TestBaseActivity {
    private TextView tvUsbStatus;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 9;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_usb);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvUsbStatus = findViewById(R.id.tv_usb_status);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_usb));
        tvUsbStatus.setText("USB未测试");
        tvUsbStatus.setVisibility(View.VISIBLE);
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
        updateStatus("正在检测USB设备...", R.color.status_testing);
        tvUsbStatus.setText("检测中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                checkUsbDevices();
            }
        }).start();
    }

    private void checkUsbDevices() {
        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
        if (usbManager == null) {
            testFailed("无法获取USB管理器");
            return;
        }

        java.util.HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        int deviceCount = deviceList.size();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (deviceCount > 0) {
                    tvUsbStatus.setText(String.format("检测到 %d 个USB设备", deviceCount));
                    tvUsbStatus.setTextColor(getResources().getColor(R.color.status_pass));
                    updateStatus("USB设备检测成功", R.color.status_pass);
                    testPassed();
                } else {
                    tvUsbStatus.setText("未检测到USB设备");
                    tvUsbStatus.setTextColor(getResources().getColor(R.color.status_fail));
                    updateStatus("请连接USB设备", R.color.status_testing);
                    // 等待用户连接USB设备
                    new Thread(() -> {
                        try {
                            Thread.sleep(10000); // 等待10秒
                            UsbManager finalUsbManager = (UsbManager) getSystemService(USB_SERVICE);
                            if (finalUsbManager != null) {
                                java.util.HashMap<String, UsbDevice> finalDeviceList = finalUsbManager.getDeviceList();
                                if (finalDeviceList.size() > 0) {
                                    runOnUiThread(() -> {
                                        tvUsbStatus.setText(String.format("检测到 %d 个USB设备", finalDeviceList.size()));
                                        tvUsbStatus.setTextColor(getResources().getColor(R.color.status_pass));
                                        updateStatus("USB设备检测成功", R.color.status_pass);
                                        testPassed();
                                    });
                                } else {
                                    testFailed("未检测到USB设备");
                                }
                            }
                        } catch (InterruptedException e) {
                            testFailed("USB检测异常");
                        }
                    }).start();
                }
            }
        });
    }
}