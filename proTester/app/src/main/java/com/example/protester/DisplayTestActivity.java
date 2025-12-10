package com.example.protester;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.atomic.AtomicInteger;

public class DisplayTestActivity extends TestBaseActivity {
    private Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvDisplayInfo;
    private TextView tvCurrentTest;
    private ViewGroup displayTestContainer;
    private AtomicInteger currentTestStep = new AtomicInteger(0);

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_test_base;
    }

    @Override
    protected int getTestId() {
        return 3;
    }

    @Override
    protected String getTestName() {
        return getString(R.string.test_display);
    }

    @Override
    protected void initializeViews() {
        TextView tvTestDescription = findViewById(R.id.tv_test_description);
        tvDisplayInfo = findViewById(R.id.tv_display_info);
        tvCurrentTest = findViewById(R.id.tv_current_test);
        displayTestContainer = findViewById(R.id.ll_test_content);
        Button btnStartTest = findViewById(R.id.btn_start_test);

        tvTestDescription.setText(getString(R.string.desc_display));
        tvDisplayInfo.setText("测试未开始");
        tvDisplayInfo.setVisibility(View.VISIBLE);
        tvCurrentTest.setVisibility(View.VISIBLE);
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
        updateStatus("正在执行显示屏测试...", R.color.status_testing);
        tvDisplayInfo.setText("测试进行中");

        // 执行多步骤显示测试
        runDisplayTests();
    }

    private void runDisplayTests() {
        currentTestStep.set(0);

        // 步骤1: 基本颜色测试
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvCurrentTest.setText("步骤 1/5: 基本颜色测试");
                testBasicColors();
            }
        }, 500);

        // 步骤2: 文本显示测试
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvCurrentTest.setText("步骤 2/5: 文本显示测试");
                testTextDisplay();
            }
        }, 3000);

        // 步骤3: 动画测试
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvCurrentTest.setText("步骤 3/5: 动画测试");
                testAnimation();
            }
        }, 6000);

        // 步骤4: 触摸响应测试
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvCurrentTest.setText("步骤 4/5: 触摸响应测试");
                testTouchResponse();
            }
        }, 9000);

        // 步骤5: 屏幕分辨率测试
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvCurrentTest.setText("步骤 5/5: 屏幕分辨率测试");
                testScreenResolution();
            }
        }, 12000);

        // 测试完成
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentTestStep.get() == 5) {
                    testPassed();
                } else {
                    testFailed("显示屏测试未完成");
                }
            }
        }, 15000);
    }

    private void testBasicColors() {
        displayTestContainer.removeAllViews();

        // 创建颜色测试区域
        View colorTestView = new View(this);
        colorTestView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            200
        ));
        colorTestView.setBackgroundColor(Color.RED);

        TextView colorDesc = new TextView(this);
        colorDesc.setText("请确认屏幕显示为红色");
        colorDesc.setTextSize(16);
        colorDesc.setPadding(16, 8, 16, 8);

        displayTestContainer.addView(colorTestView);
        displayTestContainer.addView(colorDesc);

        // 3秒后切换到下一个颜色
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                colorTestView.setBackgroundColor(Color.GREEN);
                colorDesc.setText("请确认屏幕显示为绿色");
            }
        }, 1500);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                colorTestView.setBackgroundColor(Color.BLUE);
                colorDesc.setText("请确认屏幕显示为蓝色");
                currentTestStep.set(1);
            }
        }, 3000);
    }

    private void testTextDisplay() {
        displayTestContainer.removeAllViews();

        TextView textView1 = new TextView(this);
        textView1.setText("大号文本测试");
        textView1.setTextSize(24);
        textView1.setTextColor(Color.BLACK);
        textView1.setPadding(16, 8, 16, 8);

        TextView textView2 = new TextView(this);
        textView2.setText("小号文本测试 Small Text");
        textView2.setTextSize(12);
        textView2.setTextColor(Color.GRAY);
        textView2.setPadding(16, 8, 16, 8);

        TextView textView3 = new TextView(this);
        textView3.setText("中文English混合文本 Mixed Text");
        textView3.setTextSize(16);
        textView3.setTextColor(Color.BLACK);
        textView3.setPadding(16, 8, 16, 8);

        displayTestContainer.addView(textView1);
        displayTestContainer.addView(textView2);
        displayTestContainer.addView(textView3);

        currentTestStep.set(2);
    }

    private void testAnimation() {
        displayTestContainer.removeAllViews();

        View animatedView = new View(this);
        animatedView.setLayoutParams(new ViewGroup.LayoutParams(
            100, 100
        ));
        animatedView.setBackgroundColor(Color.MAGENTA);

        TextView animDesc = new TextView(this);
        animDesc.setText("请观察方块的颜色变化");
        animDesc.setTextSize(16);
        animDesc.setPadding(16, 8, 16, 8);

        displayTestContainer.addView(animatedView);
        displayTestContainer.addView(animDesc);

        // 简单的颜色动画
        for (int i = 0; i < 10; i++) {
            final int step = i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int color = Color.HSVToColor(new float[]{
                        (step * 36) % 360, 1.0f, 1.0f
                    });
                    animatedView.setBackgroundColor(color);
                }
            }, i * 200);
        }

        currentTestStep.set(3);
    }

    private void testTouchResponse() {
        displayTestContainer.removeAllViews();

        TextView touchDesc = new TextView(this);
        touchDesc.setText("点击任意位置测试触摸响应");
        touchDesc.setTextSize(16);
        touchDesc.setPadding(16, 8, 16, 8);

        TextView touchStatus = new TextView(this);
        touchStatus.setText("未检测到触摸");
        touchStatus.setTextSize(14);
        touchStatus.setPadding(16, 8, 16, 8);
        touchStatus.setTag("touchStatus");

        View touchArea = new View(this);
        touchArea.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            200
        ));
        touchArea.setBackgroundColor(Color.LTGRAY);

        touchArea.setOnClickListener(v -> {
            TextView status = (TextView) displayTestContainer.findViewWithTag("touchStatus");
            if (status != null) {
                status.setText("触摸检测成功！");
                status.setTextColor(Color.GREEN);
            }
        });

        displayTestContainer.addView(touchDesc);
        displayTestContainer.addView(touchArea);
        displayTestContainer.addView(touchStatus);

        currentTestStep.set(4);
    }

    private void testScreenResolution() {
        displayTestContainer.removeAllViews();

        TextView resolutionInfo = new TextView(this);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float density = getResources().getDisplayMetrics().density;
        int dpi = (int)(density * 160);

        String resolutionText = String.format(
            "屏幕信息:\n" +
            "分辨率: %d x %d\n" +
            "密度: %.2f\n" +
            "DPI: %d\n" +
            "请确认显示正常",
            screenWidth, screenHeight, density, dpi
        );

        resolutionInfo.setText(resolutionText);
        resolutionInfo.setTextSize(16);
        resolutionInfo.setPadding(16, 8, 16, 8);
        resolutionInfo.setTextColor(Color.BLACK);

        // 创建一个覆盖整个屏幕的测试区域
        View fullTestArea = new View(this);
        fullTestArea.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        fullTestArea.setBackgroundColor(Color.YELLOW);

        displayTestContainer.addView(fullTestArea);
        displayTestContainer.addView(resolutionInfo);

        // 3秒后恢复颜色
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fullTestArea.setBackgroundColor(Color.TRANSPARENT);
            }
        }, 2000);

        currentTestStep.set(5);
        tvCurrentTest.setText("显示屏测试完成");
    }

    @Override
    protected void testPassed() {
        handler.removeCallbacksAndMessages(null);
        super.testPassed();
    }

    @Override
    protected void testFailed(String errorMessage) {
        handler.removeCallbacksAndMessages(null);
        super.testFailed(errorMessage);
    }

    @Override
    protected void testSkipped() {
        handler.removeCallbacksAndMessages(null);
        super.testSkipped();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}