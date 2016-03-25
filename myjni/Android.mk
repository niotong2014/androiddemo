LOCAL_PATH := $(call my-dir)

#$(shell rm $(LOCAL_PATH)/gen $(LOCAL_PATH)/bin)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := JniTestApp
LOCAL_CERTIFICATE := platform

LOCAL_JNI_SHARED_LIBRARIES := libJniTest

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)
include $(CLEAR_VARS)
LOCAL_PATH := $(call my-dir)
include $(call all-makefiles-under, $(LOCAL_PATH)/)
