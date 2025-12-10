# ProTester Android 10 源码集成指南

## 概述

本文档详细说明了如何将ProTester应用集成到Android 10源码中，使其成为系统应用的一部分。

## 项目结构

```
ProTester/
├── app/                              # 原始Android项目
│   ├── src/main/
│   │   ├── java/com/example/protester/    # Java源码
│   │   ├── res/                           # 资源文件
│   │   └── assets/                        # 配置文件
│   └── build.gradle                       # 构建配置
├── packages/apps/ProTester/           # Android 10源码中的系统应用目录
│   ├── Android.mk                        # Make构建文件
│   ├── Android.bp                        # Soong构建文件
│   ├── ProTester-permissions.xml        # 权限配置文件
│   └── [原始项目文件]                     # 复制自app目录
├── build_protester.sh                   # 编译脚本
└── INTEGRATION_GUIDE.md                # 本指南
```

## 集成步骤

### 步骤1：准备Android 10源码环境

1. 确保您已经下载并配置了Android 10源码
2. 确保构建环境正确配置（Java、环境变量等）
3. 验证源码可以正常编译

### 步骤2：复制项目文件到源码中

```bash
# 进入Android 10源码目录
cd /path/to/android-10.0.0_r1

# 创建系统应用目录
mkdir -p packages/apps/ProTester

# 复制原始项目文件
cp -r /path/to/proTester/app/* packages/apps/ProTester/

# 复制构建文件（如果需要）
# cp /path/to/proTester/packages/apps/ProTester/* packages/apps/ProTester/
```

### 步骤3：设置构建配置

#### 选项A：使用Android.mk（传统Make系统）

创建 `packages/apps/ProTester/Android.mk` 文件：

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

#### 选项B：使用Android.bp（Soong构建系统）

创建 `packages/apps/ProTester/Android.bp` 文件：

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

### 步骤4：配置权限

创建 `packages/apps/ProTester/ProTester-permissions.xml` 文件：

```xml
<?xml version="1.0" encoding="utf-8"?>
<permissions>
    <privapp-permissions package="com.example.protester">
        <!-- 硬件测试权限 -->
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

### 步骤5：修改AndroidManifest.xml

更新 `app/src/main/AndroidManifest.xml` 文件，添加系统应用相关的配置：

```xml
<application
    android:allowBackup="true"
    android:debuggable="true"
    android:allowTest="true"
    android:allowClearUserData="true"
    android:allowTaskReparenting="true"
    android:configChanges="keyboard|keyboardHidden|orientation|screenSize|smallestScreenSize|screenLayout|uiMode"
    android:hardwareAccelerated="true"
    android:killAfterRestore="false"
    android:restoreAnyVersion="true"
    android:stateNotNeeded="true"
    android:vmSafeMode="true"
    android:extractNativeLibs="true"
    android:largeHeap="true">

    <!-- 所有Activity声明 -->
</application>
```

### 步骤6：集成到系统产品配置

#### 方法A：通过device配置文件

在设备配置文件中添加ProTester：

```bash
# 创建或修改 device/*/device.mk 文件
echo "PRODUCT_PACKAGES += ProTester" >> device/*/device.mk
```

#### 方法B：通过product.mk

创建或修改 `device/generic/generic_no_phone/protester.mk`：

```makefile
PRODUCT_PACKAGES += \
    ProTester

PRODUCT_PROPERTY_OVERRIDES += \
    ro.protester.enabled=true
```

### 步骤7：编译应用

#### 使用编译脚本

```bash
# 赋予执行权限
chmod +x build_protester.sh

# 运行编译脚本
./build_protester.sh /path/to/android-10.0.0_r1 aosp_arm-eng
```

#### 手动编译

```bash
# 进入Android源码目录
cd /path/to/android-10.0.0_r1

# 设置环境
source build/envsetup.sh

# 选择目标设备
lunch aosp_arm-eng

# 单独编译ProTester
mm packages/apps/ProTester

# 或完整系统编译
make -j4
```

### 步骤8：刷机和测试

```bash
# 进入fastboot模式
adb reboot bootloader

# 刷机（替换为实际的镜像路径）
fastboot flash system out/target/product/aosp_arm/system.img

# 重启设备
fastboot reboot
```

## 构建选项

### 编译方式

1. **单独编译**: 只编译ProTester应用
   ```bash
   mm packages/apps/ProTester
   ```

2. **完整系统编译**: 编译整个系统
   ```bash
   make -j$(nproc)
   ```

3. **Soong编译**: 使用新的构建系统
   ```bash
   m ProTester
   ```

### 目标设备

- `aosp_arm-eng`: ARM架构模拟器
- `aosp_x86-eng`: x86架构模拟器
- `aosp_arm64-eng`: ARM 64位架构
- `aosp_x86_64-eng`: x86 64位架构

## 故障排除

### 常见问题

1. **编译失败**
   - 检查Android源码路径是否正确
   - 确保所有依赖库都已编译
   - 检查项目目录结构

2. **权限错误**
   - 确保ProTester-permissions.xml文件正确配置
   - 检查manifest中的权限声明

3. **资源文件错误**
   - 确保资源文件路径正确
   - 检查资源文件格式

### 调试技巧

1. **查看详细日志**
   ```bash
   make -j1 V=1
   ```

2. **清理后重新编译**
   ```bash
   make clean
   mm packages/apps/ProTester
   ```

3. **检查APK文件**
   ```bash
   find out -name "ProTester.apk"
   ```

## 高级配置

### 自定义权限

如果需要额外的自定义权限，可以在ProTester-permissions.xml中添加：

```xml
<permission name="com.example.protester.permission.CUSTOM_PERMISSION"
            label="Custom Permission"
            description="Custom permission description"
            protectionLevel="signature" />
```

### 系统属性

在产品配置中添加系统属性：

```makefile
PRODUCT_PROPERTY_OVERRIDES += \
    ro.protester.enabled=true \
    ro.protester.debug=false \
    ro.protester.autostart=false
```

### 条件编译

使用Android.mk的条件编译：

```makefile
ifeq ($(TARGET_DEVICE), specific_device)
    LOCAL_CFLAGS += -DSPECIFIC_DEVICE_CONFIG
endif
```

## 部署测试

### 验证应用安装

```bash
# 检查应用是否安装
adb shell pm list packages | grep com.example.protester

# 检查应用权限
adb shell dumpsys package com.example.protester

# 检查日志
adb logcat | grep ProTester
```

### 功能测试

1. 启动ProTester应用
2. 测试各个硬件功能模块
3. 验证测试结果保存功能
4. 检查自动测试流程

## 注意事项

1. **版本兼容性**: 确保代码与Android 10的API兼容
2. **权限管理**: 谨慎使用系统权限，避免安全风险
3. **性能优化**: 系统应用需要考虑性能优化
4. **安全考虑**: 确保测试操作的安全性
5. **硬件适配**: 根据具体硬件调整测试逻辑

## 技术支持

如果在集成过程中遇到问题，请检查：

1. Android 10源码是否完整
2. 构建环境是否正确
3. 权限配置是否正确
4. 资源文件是否完整
5. 代码是否与Android 10兼容

---

*本文档最后更新时间：2024年*