$(info ====================================)
$(info current cpu arch: $(TARGET_ARCH))
$(info MYDEBUGFLAG : $(MYDEBUGFLAG))
$(info ====================================)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)     
LOCAL_MODULE    := librcsdk
ifeq ($(TARGET_ARCH),x86)
LOCAL_SRC_FILES := librcsdk_x86.a
else
LOCAL_SRC_FILES := librcsdk.a
endif
include $(PREBUILT_STATIC_LIBRARY) 

include $(CLEAR_VARS)     
LOCAL_MODULE    := libCommunication
ifeq ($(TARGET_ARCH),x86)
LOCAL_SRC_FILES := libCommunication_x86.a
else
LOCAL_SRC_FILES := libCommunication.a
endif
include $(PREBUILT_STATIC_LIBRARY) 

include $(CLEAR_VARS)     
LOCAL_MODULE    := libprotobuf-lite
ifeq ($(TARGET_ARCH),x86)
LOCAL_SRC_FILES := libprotobuf-lite_x86.a
else
LOCAL_SRC_FILES := libprotobuf-lite.a
endif
include $(PREBUILT_STATIC_LIBRARY) 

include $(CLEAR_VARS)
LOCAL_STATIC_LIBRARIES := librcsdk libCommunication libprotobuf-lite 
LOCAL_MODULE    := RongIMLib
LOCAL_SRC_FILES := RongIMLib.cpp 					     
LOCAL_LDLIBS := -llog  -lz
include $(BUILD_SHARED_LIBRARY)


