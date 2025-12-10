package com.example.protester;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.Date;

public abstract class TestBaseActivity extends AppCompatActivity {
    protected TestConfigManager configManager;
    protected TestResultManager resultManager;
    protected TestResult currentTestResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());

        configManager = new TestConfigManager(this);
        resultManager = new TestResultManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(getTestName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentTestResult = new TestResult(getTestId(), getTestName());

        initializeViews();
        setupClickListeners();
    }

    protected abstract int getLayoutResource();
    protected abstract int getTestId();
    protected abstract String getTestName();
    protected abstract void initializeViews();
    protected abstract void setupClickListeners();

    protected void updateStatus(String status) {
        TextView statusTextView = findViewById(R.id.tv_status);
        if (statusTextView != null) {
            statusTextView.setText(status);
        }
    }

    protected void updateStatus(String status, int colorResId) {
        TextView statusTextView = findViewById(R.id.tv_status);
        if (statusTextView != null) {
            statusTextView.setText(status);
            statusTextView.setTextColor(getResources().getColor(colorResId));
        }
    }

    protected void showButton(Button button, boolean show) {
        if (button != null) {
            button.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void startTest() {
        updateStatus("测试中...", R.color.status_testing);
        showButton(findViewById(R.id.btn_start_test), false);
        showButton(findViewById(R.id.btn_pass), true);
        showButton(findViewById(R.id.btn_fail), true);
        showButton(findViewById(R.id.btn_skip), true);

        // 在子类中实现具体的测试逻辑
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        performTest();
                    }
                });
            }
        }).start();
    }

    protected abstract void performTest();

    protected void testPassed() {
        currentTestResult.setPassed(true);
        currentTestResult.setEndTime(new Date());
        resultManager.addTestResult(currentTestResult);

        updateStatus("通过", R.color.status_pass);
        showToast("测试通过");

        finish();
    }

    protected void testFailed(String errorMessage) {
        currentTestResult.setPassed(false);
        currentTestResult.setErrorMessage(errorMessage);
        currentTestResult.setEndTime(new Date());
        resultManager.addTestResult(currentTestResult);

        updateStatus("失败", R.color.status_fail);
        showToast("测试失败: " + errorMessage);

        finish();
    }

    protected void testSkipped() {
        currentTestResult.setPassed(false);
        currentTestResult.setEndTime(new Date());
        currentTestResult.setDetails("用户跳过测试");
        resultManager.addTestResult(currentTestResult);

        updateStatus("跳过", R.color.status_skip);
        showToast("测试已跳过");

        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    protected String formatDuration(long duration) {
        return String.format("%.2f秒", duration / 1000.0);
    }
}