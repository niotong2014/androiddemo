package com.example.protester;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestResultsActivity extends AppCompatActivity {
    private TestResultManager resultManager;
    private TextView tvSummary;
    private TextView tvResults;
    private Button btnSaveJson;
    private Button btnSaveXml;
    private Button btnClearResults;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);

        resultManager = new TestResultManager(this);

        Toolbar toolbar = findViewById(R.id.tv_title);
        toolbar.setText(R.string.title_test_results);

        tvSummary = findViewById(R.id.tv_summary);
        tvResults = findViewById(R.id.tv_results);
        btnSaveJson = findViewById(R.id.btn_save_json);
        btnSaveXml = findViewById(R.id.btn_save_xml);
        btnClearResults = findViewById(R.id.btn_clear_results);
        btnBack = findViewById(R.id.btn_back);

        setupClickListeners();
        displayResults();
    }

    private void setupClickListeners() {
        btnSaveJson.setOnClickListener(v -> {
            if (resultManager.saveResultsAsJson()) {
                showToast("JSON结果保存成功");
            } else {
                showToast("JSON结果保存失败");
            }
        });

        btnSaveXml.setOnClickListener(v -> {
            if (resultManager.saveResultsAsXml()) {
                showToast("XML结果保存成功");
            } else {
                showToast("XML结果保存失败");
            }
        });

        btnClearResults.setOnClickListener(v -> {
            resultManager.clearResults();
            displayResults();
            showToast("测试结果已清空");
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void displayResults() {
        // 显示汇总信息
        String summary = resultManager.getSummary();
        tvSummary.setText(summary);

        // 显示详细结果
        StringBuilder resultsText = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (TestResult result : resultManager.getTestResults()) {
            resultsText.append("测试项目: ").append(result.getTestName()).append("\n");
            resultsText.append("结果: ").append(result.isPassed() ? "通过" : "失败").append("\n");
            resultsText.append("开始时间: ").append(dateFormat.format(result.getStartTime())).append("\n");
            resultsText.append("结束时间: ").append(dateFormat.format(result.getEndTime())).append("\n");
            resultsText.append("耗时: ").append(formatDuration(result.getDuration())).append("\n");

            if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) {
                resultsText.append("错误信息: ").append(result.getErrorMessage()).append("\n");
            }

            if (result.getDetails() != null && !result.getDetails().isEmpty()) {
                resultsText.append("详细信息: ").append(result.getDetails()).append("\n");
            }

            resultsText.append("---\n");
        }

        if (resultsText.length() == 0) {
            tvResults.setText("暂无测试结果");
        } else {
            tvResults.setText(resultsText.toString());
        }
    }

    private String formatDuration(long duration) {
        return String.format("%.2f秒", duration / 1000.0);
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}