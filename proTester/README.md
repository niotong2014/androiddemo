# Android设备出厂测试APK

这是一个专门为Android设备出厂测试设计的APK，可以测试设备的各项硬件功能。

## 功能特性

### 支持的测试项目
1. **MIC测试** - 检查麦克风录音功能
2. **喇叭测试** - 检查音频播放功能
3. **显示屏测试** - 检查屏幕显示效果
4. **WiFi测试** - 检查无线网络连接
5. **蓝牙测试** - 检查蓝牙功能
6. **GPIO测试** - 检查GPIO引脚功能
7. **SD卡测试** - 检查SD卡读写功能
8. **HDMI测试** - 检查HDMI输出
9. **USB测试** - 检查USB设备连接
10. **以太网测试** - 检查有线网络连接
11. **串口测试** - 检查串口通信功能
12. **ADC按键测试** - 检查ADC按键功能

### 主要功能
- **主界面**: 显示所有测试项目，可单独点击进入测试
- **自动测试**: 可配置自动执行所有测试项目
- **测试结果**: 实时显示每个测试项目的状态
- **结果保存**: 支持JSON和XML格式保存测试结果
- **配置文件**: 通过JSON配置文件控制测试项和顺序

## 项目结构

```
proTester/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/protester/
│   │   │   │   ├── MainActivity.java              # 主界面
│   │   │   │   ├── AutoTestActivity.java           # 自动测试界面
│   │   │   │   ├── TestResultsActivity.java        # 测试结果界面
│   │   │   │   ├── TestBaseActivity.java          # 测试基类
│   │   │   │   ├── TestConfigManager.java         # 配置管理器
│   │   │   │   ├── TestResultManager.java         # 结果管理器
│   │   │   │   ├── TestItem.java                  # 测试项数据类
│   │   │   │   ├── TestResult.java                # 测试结果数据类
│   │   │   │   ├── MicTestActivity.java           # MIC测试
│   │   │   │   ├── SpeakerTestActivity.java        # 喇叭测试
│   │   │   │   ├── DisplayTestActivity.java       # 显示屏测试
│   │   │   │   ├── WiFiTestActivity.java          # WiFi测试
│   │   │   │   ├── BluetoothTestActivity.java      # 蓝牙测试
│   │   │   │   ├── GpioTestActivity.java          # GPIO测试
│   │   │   │   ├── SdCardTestActivity.java        # SD卡测试
│   │   │   │   ├── HdmiTestActivity.java          # HDMI测试
│   │   │   │   ├── UsbTestActivity.java           # USB测试
│   │   │   │   ├── EthernetTestActivity.java      # 以太网测试
│   │   │   │   ├── UartTestActivity.java          # 串口测试
│   │   │   │   └── AdcTestActivity.java           # ADC按键测试
│   │   │   ├── res/
│   │   │   │   ├── layout/                        # 布局文件
│   │   │   │   ├── values/                        # 资源文件
│   │   │   │   └── assets/                         # 配置文件
│   │   │   └── AndroidManifest.xml                # 清单文件
│   │   └── build.gradle                            # 模块构建文件
├── build.gradle                                    # 项目构建文件
└── README.md                                       # 项目说明文档
```

## 配置文件

测试配置文件位于 `app/src/main/assets/test_config.json`，包含以下配置项：

```json
{
  "auto_test_enabled": true,
  "test_items": [
    {
      "id": "mic",
      "name": "MIC测试",
      "description": "请对着MIC说话，检查录音是否正常",
      "enabled": true,
      "order": 1
    }
    // ... 其他测试项
  ]
}
```

### 配置项说明
- `auto_test_enabled`: 是否启用自动测试
- `test_items`: 测试项列表
  - `id`: 测试项唯一标识
  - `name`: 测试项显示名称
  - `description`: 测试项描述
  - `enabled`: 是否启用该测试项
  - `order`: 测试执行顺序

## 测试结果格式

### JSON格式
```json
{
  "test_results": [
    {
      "test_id": "mic",
      "test_name": "MIC测试",
      "passed": true,
      "start_time": 1642675200000,
      "end_time": 1642675230000,
      "duration": 3000,
      "error_message": null,
      "details": "测试通过"
    }
  ],
  "total_tests": 12,
  "passed_tests": 10,
  "failed_tests": 2,
  "test_date": 1642675200000
}
```

### XML格式
```xml
<test_results>
  <summary>
    <total_tests>12</total_tests>
    <passed_tests>10</passed_tests>
    <failed_tests>2</failed_tests>
    <test_date>2024-01-20 10:00:00</test_date>
  </summary>
  <test>
    <id>mic</id>
    <name>MIC测试</name>
    <passed>true</passed>
    <start_time>2024-01-20 10:00:00</start_time>
    <end_time>2024-01-20 10:00:03</end_time>
    <duration>3000ms</duration>
    <details>测试通过</details>
  </test>
</test_results>
```

## 使用方法

1. **单个测试**:
   - 在主界面点击要测试的项目
   - 按照提示进行测试
   - 点击"通过"、"失败"或"跳过"按钮完成测试

2. **自动测试**:
   - 点击主界面的"自动测试"按钮
   - 系统会按照配置顺序自动执行所有测试项
   - 可随时停止自动测试

3. **查看结果**:
   - 点击主界面的"查看结果"按钮
   - 可以查看所有测试项目的详细结果
   - 支持导出JSON和XML格式的测试报告

## 权限要求

APK需要以下权限：
- `RECORD_AUDIO`: MIC测试
- `INTERNET`: 网络相关测试
- `ACCESS_NETWORK_STATE`: 网络状态检测
- `BLUETOOTH`: 蓝牙测试
- `READ_EXTERNAL_STORAGE/ WRITE_EXTERNAL_STORAGE`: SD卡测试
- `MODIFY_AUDIO_SETTINGS`: 音频设置修改
- `SYSTEM_ALERT_WINDOW`: 显示悬浮窗口

## 编译和部署

1. 使用Android Studio打开项目
2. 等待Gradle同步完成
3. 选择合适的构建目标
4. 执行Build → Build Bundle(s) / APK(s) → Build APK(s)
5. 在`app/build/outputs/apk/debug/`目录下找到生成的APK文件

## 扩展开发

### 添加新的测试项目
1. 创建新的Activity继承`TestBaseActivity`
2. 实现`performTest()`方法
3. 在`TestConfigManager`中添加测试项配置
4. 在配置文件中添加对应的测试项

### 自定义测试结果保存
修改`TestResultManager`类中的`saveResultsAsJson()`和`saveResultsAsXml()`方法。

### 自定义配置项
在`test_config.json`中添加新的配置项，并在`TestConfigManager`中解析相应的配置。

## 注意事项

1. 某些测试可能需要特定的硬件支持
2. 建议在测试前确保设备已连接所有相关的硬件外设
3. 测试结果文件保存在设备存储的`ProTester`目录下
4. 部分测试可能需要用户手动确认测试结果

## 技术支持

如需技术支持或有任何问题，请通过以下方式联系：
- 检查项目代码中的注释和文档
- 参考Android官方开发文档
- 查看相关硬件厂商的开发指南