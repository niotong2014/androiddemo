#!/bin/bash

# 进入Android 10源码目录
cd /path/to/android-10.0.0_r1

# 设置环境变量
source build/envsetup.sh
lunch aosp_arm-eng

# 使用Soong编译系统
echo "使用Soong编译ProTester应用..."
m ProTester

# 检查编译结果
if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo "APK位置: out/target/product/*/system/app/ProTester/ProTester.apk"
else
    echo "编译失败！"
    exit 1
fi