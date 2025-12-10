package com.example.protester;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;

public class AutoTestActivity extends AppCompatActivity {
    private TestConfigManager configManager;
    private TestResultManager resultManager;
    private List<TestItem> testItems;
    private int currentIndex = 0;
    private boolean isAutoTesting = false;

    private TextView tvProgress;
    private TextView tvStatus;
    private TextView tvCurrentTest;
    private Button btnStartStop;
    private Button btnBack;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable autoTestRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_test);

        configManager = new TestConfigManager(this);
        resultManager = new TestResultManager(this);

        Toolbar toolbar = findViewById(R.id.tv_title);
        toolbar.setText(R.string.title_auto_test);

        tvProgress = findViewById(R.id.tv_progress);
        tvStatus = findViewById(R.id.tv_status);
        tvCurrentTest = findViewById(R.id.tv_current_test);
        btnStartStop = findViewById(R.id.btn_start_stop);
        btnBack = findViewById(R.id.btn_back);

        testItems = configManager.loadTestItems();

        setupClickListeners();
        updateProgress();
    }

    private void setupClickListeners() {
        btnStartStop.setOnClickListener(v -> {
            if (isAutoTesting) {
                stopAutoTest();
            } else {
                startAutoTest();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (isAutoTesting) {
                stopAutoTest();
            }
            finish();
        });
    }

    private void startAutoTest() {
        if (testItems.isEmpty()) {
            showToast("没有可用的测试项");
            return;
        }

        isAutoTesting = true;
        currentIndex = 0;
        resultManager.clearResults();

        btnStartStop.setText("停止测试");
        updateProgress();

        showToast("自动测试开始");

        // 开始执行第一个测试
        executeNextTest();
    }

    private void stopAutoTest() {
        isAutoTesting = false;
        btnStartStop.setText("开始测试");

        if (autoTestRunnable != null) {
            handler.removeCallbacks(autoTestRunnable);
        }

        showToast("自动测试已停止");
        updateProgress();
    }

    private void executeNextTest() {
        if (!isAutoTesting || currentIndex >= testItems.size()) {
            completeAutoTest();
            return;
        }

        TestItem currentTest = testItems.get(currentIndex);
        tvCurrentTest.setText("正在测试: " + currentTest.getName());
        tvStatus.setText("测试进行中...");

        // 模拟测试过程
        autoTestRunnable = new Runnable() {
            @Override
            public void run() {
                performAutoTest(currentTest);
            }
        };

        // 延迟执行测试，给用户一点时间看到进度
        handler.postDelayed(autoTestRunnable, 1000);
    }

    private void performAutoTest(TestItem testItem) {
        // 这里应该根据不同的测试项执行相应的测试
        // 为了演示，我们使用随机结果

        boolean passed = Math.random() > 0.2; // 80% 的概率通过

        TestResult result = new TestResult(testItem.getId(), testItem.getName());
        result.setPassed(passed);

        if (passed) {
            result.setDetails("测试通过");
            result.setEndTime(new Date());
            resultManager.addTestResult(result);
        } else {
            result.setErrorMessage("自动测试失败");
            result.setDetails("测试过程中发现问题");
            result.setEndTime(new Date());
            resultManager.addTestResult(result);
        }

        currentIndex++;
        updateProgress();

        // 继续下一个测试或结束测试
        if (isAutoTesting) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    executeNextTest();
                }
            }, 2000); // 每个测试间隔2秒
        }
    }

    private void completeAutoTest() {
        isAutoTesting = false;
        btnStartStop.setText("开始测试");

        String summary = resultManager.getSummary();
        tvCurrentTest.setText("自动测试完成");
        tvStatus.setText(summary);

        showToast("自动测试完成: " + summary);

        // 可以选择性地跳转到结果页面
        // Intent intent = new Intent(this, TestResultsActivity.class);
        // startActivity(intent);
        // finish();
    }

    private void updateProgress() {
        int total = testItems.size();
        int completed = currentIndex;

        if (isAutoTesting && currentIndex < total) {
            tvProgress.setText(String.format("进度: %d/%d (%.1f%%)",
                completed, total, (double) completed / total * 100));
        } else if (!isAutoTesting) {
            tvProgress.setText(String.format("准备测试: %d 个项目", total));
        } else {
            tvProgress.setText(String.format("完成: %d/%d (%.1f%%)",
                completed, total, 100.0));
        }
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoTestRunnable != null) {
            handler.removeCallbacks(autoTestRunnable);
        }
    }
}