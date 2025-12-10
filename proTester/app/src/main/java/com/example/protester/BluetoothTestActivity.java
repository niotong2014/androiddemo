package com.example.protester;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BluetoothTestActivity extends TestBaseActivity {
    private TextView tvBluetoothStatus;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 5;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_bluetooth);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvBluetoothStatus = findViewById(R.id.tv_bluetooth_status);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_bluetooth));
        tvBluetoothStatus.setText("蓝牙未检查");
        tvBluetoothStatus.setVisibility(View.VISIBLE);
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
        updateStatus("正在检查蓝牙...", R.color.status_testing);
        tvBluetoothStatus.setText("检查中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                checkBluetoothStatus();
            }
        }).start();
    }

    private void checkBluetoothStatus() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            testFailed("设备不支持蓝牙");
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bluetoothAdapter.isEnabled()) {
                    tvBluetoothStatus.setText("蓝牙已开启");
                    tvBluetoothStatus.setTextColor(getResources().getColor(R.color.status_pass));
                    updateStatus("蓝牙功能正常", R.color.status_pass);

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
                    tvBluetoothStatus.setText("蓝牙未开启");
                    tvBluetoothStatus.setTextColor(getResources().getColor(R.color.status_fail));
                    updateStatus("蓝牙未开启", R.color.status_fail);
                    testFailed("蓝牙未开启");
                }
            }
        });
    }
}