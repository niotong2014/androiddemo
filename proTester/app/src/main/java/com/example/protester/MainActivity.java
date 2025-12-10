package com.example.protester;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TestConfigManager configManager;
    private TestResultManager resultManager;
    private LinearLayout llTestItems;
    private List<TestItem> testItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configManager = new TestConfigManager(this);
        resultManager = new TestResultManager(this);

        Toolbar toolbar = findViewById(R.id.tv_title);
        toolbar.setText(R.string.title_main);

        llTestItems = findViewById(R.id.ll_test_items);

        Button btnAutoTest = findViewById(R.id.btn_auto_test);
        Button btnTestResults = findViewById(R.id.btn_test_results);

        loadTestItems();
        setupClickListeners();
    }

    private void loadTestItems() {
        testItems = configManager.loadTestItems();
        llTestItems.removeAllViews();

        for (TestItem testItem : testItems) {
            if (testItem.isEnabled()) {
                createTestItemView(testItem);
            }
        }
    }

    private void createTestItemView(TestItem testItem) {
        View testItemView = getLayoutInflater().inflate(R.layout.item_test, llTestItems, false);

        TextView tvTestName = testItemView.findViewById(R.id.tv_test_name);
        TextView tvTestDesc = testItemView.findViewById(R.id.tv_test_desc);
        TextView tvStatus = testItemView.findViewById(R.id.tv_status);
        Button btnStartTest = testItemView.findViewById(R.id.btn_start_test);

        tvTestName.setText(testItem.getName());
        tvTestDesc.setText(testItem.getDescription());
        tvStatus.setText(R.string.status_idle);
        tvStatus.setTextColor(getResources().getColor(R.color.status_idle));
        btnStartTest.setText(R.string.btn_start_test);

        btnStartTest.setOnClickListener(v -> {
            Intent intent = new Intent(this, testItem.getActivityClass());
            startActivity(intent);
        });

        llTestItems.addView(testItemView);
    }

    private void setupClickListeners() {
        Button btnAutoTest = findViewById(R.id.btn_auto_test);
        Button btnTestResults = findViewById(R.id.btn_test_results);

        btnAutoTest.setOnClickListener(v -> {
            Intent intent = new Intent(this, AutoTestActivity.class);
            startActivity(intent);
        });

        btnTestResults.setOnClickListener(v -> {
            Intent intent = new Intent(this, TestResultsActivity.class);
            startActivity(intent);
        });
    }

    private void loadTestResults() {
        // 重新加载测试结果并更新UI
        loadTestItems();

        // TODO: 从文件加载测试结果并更新状态
        // 这里应该从之前保存的测试结果文件中读取数据
        // 并更新每个测试项的状态显示
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTestResults();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            loadTestResults();
        }
    }
}