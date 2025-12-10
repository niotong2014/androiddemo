LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# 模块标签
LOCAL_MODULE_TAGS := optional

# 源文件路径
LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

# 资源文件路径
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/src/main/res

# 包名
LOCAL_PACKAGE_NAME := ProTester

# 证书类型（使用平台证书）
LOCAL_CERTIFICATE := platform

# 禁用ProGuard混淆
LOCAL_PROGUARD_ENABLED := disabled

# 特权模块（系统应用）
LOCAL_PRIVILEGED_MODULE := true

# 启用调试
LOCAL_DEBUG_MODULE := true

# 添加aidl文件支持
LOCAL_AIDL_INCLUDES := $(LOCAL_PATH)/src/main/java

# 添加静态库支持
LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-v4 \
    android-support-v13

# 添加动态库
LOCAL_JNI_SHARED_LIBRARIES :=

# 添加编译参数
LOCAL_CFLAGS += -DANDROID

# 添加链接参数
LOCAL_LDFLAGS += -Wl,--exclude-libs,libz.so

# 添加系统属性
LOCAL_PROPRIETARY_MODULE := false

# 添加测试支持
LOCAL_INSTRUMENTATION_FOR := ProTester

# 包含构建包的规则
include $(BUILD_PACKAGE)