#!/bin/bash

# ProTester Android 10 源码编译脚本
# 此脚本用于将ProTester应用编译到Android 10源码中

echo "========================================="
echo "ProTester Android 10 源码编译脚本"
echo "========================================="

# 检查参数
if [ $# -eq 0 ]; then
    echo "使用方法: $0 <android_source_path> [target_device]"
    echo "示例: $0 /path/to/android-10.0.0_r1 aosp_arm-eng"
    exit 1
fi

ANDROID_SOURCE_PATH=$1
TARGET_DEVICE=$2

# 检查Android源码路径是否存在
if [ ! -d "$ANDROID_SOURCE_PATH" ]; then
    echo "错误: Android源码路径不存在: $ANDROID_SOURCE_PATH"
    exit 1
fi

echo "Android源码路径: $ANDROID_SOURCE_PATH"

# 进入Android源码目录
cd "$ANDROID_SOURCE_PATH" || exit 1

# 检查是否是Android源码目录
if [ ! -f "build/envsetup.sh" ]; then
    echo "错误: 指定的路径不是Android源码目录"
    exit 1
fi

# 设置构建环境
echo "设置构建环境..."
source build/envsetup.sh

# 设置默认target设备
if [ -z "$TARGET_DEVICE" ]; then
    echo "请选择目标设备:"
    echo "1. aosp_arm-eng"
    echo "2. aosp_x86-eng"
    echo "3. aosp_arm64-eng"
    echo "4. aosp_x86_64-eng"
    read -p "请输入选项 (1-4): " choice
    case $choice in
        1) TARGET_DEVICE="aosp_arm-eng" ;;
        2) TARGET_DEVICE="aosp_x86-eng" ;;
        3) TARGET_DEVICE="aosp_arm64-eng" ;;
        4) TARGET_DEVICE="aosp_x86_64-eng" ;;
        *) echo "无效选项，使用默认的 aosp_arm-eng"
           TARGET_DEVICE="aosp_arm-eng" ;;
    esac
fi

echo "选择的目标设备: $TARGET_DEVICE"

# 选择构建方式
echo "选择构建方式:"
echo "1. 单独编译ProTester应用"
echo "2. 完整系统编译"
echo "3. 使用Soong编译系统"
read -p "请输入选项 (1-3): " build_choice

case $build_choice in
    1)
        echo "单独编译ProTester应用..."
        lunch "$TARGET_DEVICE"
        if [ $? -ne 0 ]; then
            echo "错误: lunch命令执行失败"
            exit 1
        fi
        mm packages/apps/ProTester
        ;;
    2)
        echo "完整系统编译..."
        lunch "$TARGET_DEVICE"
        if [ $? -ne 0 ]; then
            echo "错误: lunch命令执行失败"
            exit 1
        fi
        make -j$(nproc)
        ;;
    3)
        echo "使用Soong编译系统..."
        lunch "$TARGET_DEVICE"
        if [ $? -ne 0 ]; then
            echo "错误: lunch命令执行失败"
            exit 1
        fi
        m ProTester
        ;;
    *)
        echo "无效选项，使用默认的单独编译"
        lunch "$TARGET_DEVICE"
        mm packages/apps/ProTester
        ;;
esac

# 检查编译结果
if [ $? -eq 0 ]; then
    echo "========================================="
    echo "编译成功！"
    echo "========================================="
    echo "APK位置:"
    find "$ANDROID_SOURCE_PATH/out/target/product" -name "ProTester.apk" 2>/dev/null
    echo ""
    echo "系统镜像位置:"
    find "$ANDROID_SOURCE_PATH/out/target/product" -name "system.img" 2>/dev/null
    echo ""
    echo "刷机命令:"
    echo "fastboot flash system $ANDROID_SOURCE_PATH/out/target/product/*/system.img"
    echo "fastboot reboot"
else
    echo "========================================="
    echo "编译失败！"
    echo "========================================="
    echo "请检查以下内容:"
    echo "1. 确保Android源码路径正确"
    echo "2. 确保所有依赖库都已编译"
    echo "3. 检查ProTester应用目录结构"
    echo "4. 检查Android.mk或Android.bp文件"
    exit 1
fi