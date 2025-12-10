# ProTester - Android 10 系统应用集成

## 概述

ProTester是一个为Android设备出厂测试设计的系统应用，集成到Android 10源码中后，可以全面测试设备的硬件功能。

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
packages/apps/ProTester/
├── Android.mk                        # Make构建文件
├── Android.bp                        # Soong构建文件
├── ProTester-permissions.xml         # 权限配置文件
├── README.md                         # 本文档
├── build_protester.sh                # 编译脚本
├── INTEGRATION_GUIDE.md              # 集成指南
├── app/                              # 原始Android项目
│   ├── src/main/
│   │   ├── java/com/example/protester/    # Java源码
│   │   ├── res/                           # 资源文件
│   │   └── assets/                        # 配置文件
│   └── build.gradle                       # 模块构建文件
└── build.gradle                       # 项目构建文件
```

## 构建配置

### Android.mk（Make系统）

```makefile
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/src/main/res

LOCAL_PACKAGE_NAME := ProTester

LOCAL_CERTIFICATE := platform

LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)
```

### Android.bp（Soong系统）

```bp
android_app {
    name: "ProTester",
    srcs: ["src/main/java/**/*.java"],
    resource_dirs: ["src/main/res"],
    platform_apis: true,
    certificate: "platform",
    privileged: true,
    debuggable: true,
    system: true,
    compile_sdk_version: "android-29",
    min_sdk_version: "29",
}
```

### 权限配置

```xml
<permissions>
    <privapp-permissions package="com.example.protester">
        <permission name="android.permission.RECORD_AUDIO" />
        <permission name="android.permission.MODIFY_AUDIO_SETTINGS" />
        <permission name="android.permission.INTERNET" />
        <permission name="android.permission.ACCESS_NETWORK_STATE" />
        <permission name="android.permission.ACCESS_WIFI_STATE" />
        <permission name="android.permission.CHANGE_WIFI_STATE" />
        <permission name="android.permission.BLUETOOTH" />
        <permission name="android.permission.BLUETOOTH_ADMIN" />
        <permission name="android.permission.ACCESS_FINE_LOCATION" />
        <permission name="android.permission.READ_EXTERNAL_STORAGE" />
        <permission name="android.permission.WRITE_EXTERNAL_STORAGE" />
        <permission name="android.permission.MANAGE_EXTERNAL_STORAGE" />
        <permission name="android.permission.SYSTEM_ALERT_WINDOW" />
    </privapp-permissions>
</permissions>
```

## 构建和编译

### 使用编译脚本

```bash
# 赋予执行权限
chmod +x build_protester.sh

# 运行编译脚本
./build_protester.sh /path/to/android-10.0.0_r1 aosp_arm-eng
```

### 手动编译

```bash
# 进入Android源码目录
cd /path/to/android-10.0.0_r1

# 设置环境
source build/envsetup.sh

# 选择目标设备
lunch aosp_arm-eng

# 编译ProTester
mm packages/apps/ProTester
```

### 构建选项

- **单独编译**: `mm packages/apps/ProTester`
- **完整系统编译**: `make -j$(nproc)`
- **Soong编译**: `m ProTester`

## 配置文件

测试配置文件位于 `app/src/main/assets/test_config.json`：

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
  ]
}
```

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

## 权限要求

系统应用需要以下权限：
- `RECORD_AUDIO`: MIC测试
- `INTERNET`: 网络相关测试
- `ACCESS_NETWORK_STATE`: 网络状态检测
- `BLUETOOTH`: 蓝牙测试
- `READ_EXTERNAL_STORAGE/ WRITE_EXTERNAL_STORAGE`: SD卡测试
- `MODIFY_AUDIO_SETTINGS`: 音频设置修改
- `SYSTEM_ALERT_WINDOW`: 显示悬浮窗口
- `INSTALL_PACKAGES`: 系统级安装权限
- `DEBUG`: 调试权限

## 测试模块说明

### 测试基类

所有测试活动继承自`TestBaseActivity`，提供：
- 统一的测试生命周期管理
- 测试状态实时更新
- 测试结果保存功能
- 异常处理机制

### 单元测试实现

每个测试模块都实现了：
- 硬件检测逻辑
- 状态反馈机制
- 错误处理
- 结果记录

### 自动化测试

自动测试功能支持：
- 配置驱动的测试顺序
- 批量执行测试项目
- 实时进度显示
- 中断和恢复功能

## 系统集成

### 应用签名

使用平台证书签名，获得系统应用权限：
```makefile
LOCAL_CERTIFICATE := platform
```

### 系统属性

可以通过系统属性控制应用行为：
```makefile
PRODUCT_PROPERTY_OVERRIDES += \
    ro.protester.enabled=true \
    ro.protester.debug=false
```

### 设备配置

在设备配置文件中添加：
```makefile
PRODUCT_PACKAGES += ProTester
```

## 故障排除

### 常见问题

1. **编译失败**
   - 检查Android.mk/Android.bp配置
   - 确保资源文件路径正确
   - 验证依赖库可用性

2. **权限问题**
   - 检查ProTester-permissions.xml配置
   - 验证manifest权限声明
   - 确认系统应用配置正确

3. **运行时错误**
   - 检查硬件API兼容性
   - 验证权限授予状态
   - 查看日志cat输出

### 调试技巧

```bash
# 查看详细编译日志
make -j1 V=1

# 检查应用安装状态
adb shell pm list packages | grep com.example.protester

# 查看应用日志
adb logcat | grep ProTester

# 导出测试结果
adb pull /storage/emulated/0/ProTester/
```

## 扩展开发

### 添加新测试项目

1. 创建新的Activity继承`TestBaseActivity`
2. 实现`performTest()`方法
3. 在AndroidManifest.xml中注册Activity
4. 在配置文件中添加测试项

### 自定义测试结果

修改`TestResultManager`类中的保存方法：
```java
public boolean saveResultsAsJson() {
    // 自定义JSON生成逻辑
}
```

### 自定义配置项

在`test_config.json`中添加新的配置项，并在`TestConfigManager`中解析。

## 注意事项

1. **硬件兼容性**: 某些测试可能需要特定硬件支持
2. **权限控制**: 谨慎使用系统权限，避免安全风险
3. **性能优化**: 系统应用需要考虑系统资源占用
4. **错误处理**: 确保测试异常不会影响系统稳定性
5. **测试环境**: 建议在测试前确保设备连接所有相关硬件

## 版本信息

- **Android版本**: Android 10 (API 29)
- **构建系统**: Make/Soong
- **应用类型**: 系统应用
- **目标设备**: 支持Android 10的设备

---

*ProTester - Android 10 系统应用集成*
*最后更新: 2024年*