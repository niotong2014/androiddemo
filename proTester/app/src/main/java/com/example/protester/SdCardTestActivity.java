package com.example.protester;

import android.os.Environment;
import android.os.StatFs;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import androidx.appcompat.app.AppCompatActivity;

public class SdCardTestActivity extends TestBaseActivity {
    private TextView tvSdCardStatus;
    private TextView sdCardInfo;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 7;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_sdcard);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvSdCardStatus = findViewById(R.id.tv_sdcard_status);
        sdCardInfo = findViewById(R.id.tv_sdcard_info);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_sdcard));
        tvSdCardStatus.setText("SD卡未检查");
        tvSdCardStatus.setVisibility(View.VISIBLE);
        sdCardInfo.setText("点击开始测试SD卡");
        sdCardInfo.setVisibility(View.VISIBLE);
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
        updateStatus("正在检查SD卡...", R.color.status_testing);
        tvSdCardStatus.setText("检查中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                checkSdCard();
            }
        }).start();
    }

    private void checkSdCard() {
        File sdcard = Environment.getExternalStorageDirectory();
        File externalFilesDir = getExternalFilesDir(null);

        if (sdcard == null || !sdcard.exists()) {
            testFailed("SD卡不存在");
            return;
        }

        // 检查读写权限
        if (!sdcard.canRead() || !sdcard.canWrite()) {
            testFailed("SD卡权限不足");
            return;
        }

        // 获取SD卡信息
        StatFs statFs = new StatFs(sdcard.getPath());
        long totalBytes = statFs.getTotalBytes();
        long freeBytes = statFs.getFreeBytes();
        long usedBytes = totalBytes - freeBytes;

        DecimalFormat df = new DecimalFormat("#.##");
        String totalSize = formatSize(totalBytes);
        String freeSize = formatSize(freeBytes);
        String usedSize = formatSize(usedBytes);

        // 执行读写测试
        boolean readWriteTest = performReadWriteTest();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (readWriteTest) {
                    tvSdCardStatus.setText("测试通过");
                    tvSdCardStatus.setTextColor(getResources().getColor(R.color.status_pass));

                    sdCardInfo.setText(String.format(
                        "总容量: %s\n可用空间: %s\n已使用: %s\n状态: 正常",
                        totalSize, freeSize, usedSize
                    ));

                    updateStatus("SD卡测试通过", R.color.status_pass);
                    testPassed();
                } else {
                    tvSdCardStatus.setText("测试失败");
                    tvSdCardStatus.setTextColor(getResources().getColor(R.color.status_fail));
                    sdCardInfo.setText("SD卡读写测试失败");
                    updateStatus("SD卡测试失败", R.color.status_fail);
                    testFailed("SD卡读写测试失败");
                }
            }
        });
    }

    private boolean performReadWriteTest() {
        File testFile = new File(getExternalFilesDir(null), "test_write.txt");

        try {
            // 写入测试
            String testData = "SD卡读写测试数据 " + System.currentTimeMillis();
            FileWriter writer = new FileWriter(testFile);
            writer.write(testData);
            writer.close();

            // 读取测试
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(testFile));
            String readData = reader.readLine();
            reader.close();

            // 删除测试文件
            testFile.delete();

            return testData.equals(readData);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}