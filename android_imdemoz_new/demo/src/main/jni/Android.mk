LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := imdemo
LOCAL_SRC_FILES := imdemo.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
