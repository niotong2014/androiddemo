package com.example.protester;

import android.content.Context;
import android.content.res.AssetManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TestConfigManager {
    private static final String CONFIG_FILE = "test_config.json";
    private Context context;

    public TestConfigManager(Context context) {
        this.context = context;
    }

    public List<TestItem> loadTestItems() {
        List<TestItem> testItems = new ArrayList<>();

        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(CONFIG_FILE);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray itemsArray = jsonObject.getJSONArray("test_items");

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject itemObject = itemsArray.getJSONObject(i);
                String id = itemObject.getString("id");
                String name = itemObject.getString("name");
                String description = itemObject.getString("description");
                boolean enabled = itemObject.optBoolean("enabled", true);
                int order = itemObject.optInt("order", i);

                Class activityClass = getTestActivityClass(id);
                if (activityClass != null) {
                    TestItem testItem = new TestItem(id, name, description, enabled, order, activityClass);
                    testItems.add(testItem);
                }
            }

            // 按order排序
            Collections.sort(testItems, new Comparator<TestItem>() {
                @Override
                public int compare(TestItem item1, TestItem item2) {
                    return Integer.compare(item1.getOrder(), item2.getOrder());
                }
            });

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            // 如果配置文件加载失败，使用默认配置
            testItems = getDefaultTestItems();
        }

        return testItems;
    }

    private Class getTestActivityClass(String testId) {
        try {
            switch (testId) {
                case "mic":
                    return MicTestActivity.class;
                case "speaker":
                    return SpeakerTestActivity.class;
                case "display":
                    return DisplayTestActivity.class;
                case "wifi":
                    return WiFiTestActivity.class;
                case "bluetooth":
                    return BluetoothTestActivity.class;
                case "gpio":
                    return GpioTestActivity.class;
                case "sdcard":
                    return SdCardTestActivity.class;
                case "hdmi":
                    return HdmiTestActivity.class;
                case "usb":
                    return UsbTestActivity.class;
                case "ethernet":
                    return EthernetTestActivity.class;
                case "uart":
                    return UartTestActivity.class;
                case "adc":
                    return AdcTestActivity.class;
                default:
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<TestItem> getDefaultTestItems() {
        List<TestItem> defaultItems = new ArrayList<>();

        // 添加默认的测试项
        defaultItems.add(new TestItem("mic", "MIC测试", "请对着MIC说话，检查录音是否正常", true, 1, MicTestActivity.class));
        defaultItems.add(new TestItem("speaker", "喇叭测试", "检查喇叭是否能正常播放声音", true, 2, SpeakerTestActivity.class));
        defaultItems.add(new TestItem("display", "显示屏测试", "检查显示屏显示是否正常", true, 3, DisplayTestActivity.class));
        defaultItems.add(new TestItem("wifi", "WiFi测试", "检查WiFi连接是否正常", true, 4, WiFiTestActivity.class));
        defaultItems.add(new TestItem("bluetooth", "蓝牙测试", "检查蓝牙功能是否正常", true, 5, BluetoothTestActivity.class));
        defaultItems.add(new TestItem("gpio", "GPIO测试", "检查GPIO引脚功能是否正常", true, 6, GpioTestActivity.class));
        defaultItems.add(new TestItem("sdcard", "SD卡测试", "检查SD卡读写是否正常", true, 7, SdCardTestActivity.class));
        defaultItems.add(new TestItem("hdmi", "HDMI测试", "检查HDMI输出是否正常", true, 8, HdmiTestActivity.class));
        defaultItems.add(new TestItem("usb", "USB测试", "检查USB功能是否正常", true, 9, UsbTestActivity.class));
        defaultItems.add(new TestItem("ethernet", "以太网测试", "检查以太网连接是否正常", true, 10, EthernetTestActivity.class));
        defaultItems.add(new TestItem("uart", "串口测试", "检查串口通信是否正常", true, 11, UartTestActivity.class));
        defaultItems.add(new TestItem("adc", "ADC按键测试", "检查ADC按键是否正常", true, 12, AdcTestActivity.class));

        return defaultItems;
    }

    public boolean isAutoTestEnabled() {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(CONFIG_FILE);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optBoolean("auto_test_enabled", true);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return true; // 默认启用自动测试
        }
    }

    public void updateTestItemStatus(String testId, boolean enabled) {
        // TODO: 实现更新测试项状态的功能
        // 这里可以更新配置文件或数据库
    }
}