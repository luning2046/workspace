APP_STL := gnustl_static
APP_CFLAGS += -fexceptions
APP_CFLAGS += -frtti
APP_CFLAGS += -D ANDROID
APP_ABI := armeabi x86
ifeq ($(MYDEBUGFLAG),Debug)
APP_CFLAGS += -D DEBUG
$(info ============debug mode========================)
endif

#APP_CFLAGS += -I/usr/include
#APP_CFLAGS += -I$(ANDROID_NDK_HOME)/platforms/$(TARGET_PLATFORM)/arch-arm/usr/include
#APP_CFLAGS += /Users/test/android-ndk-r9d/platforms/android-14/arch-arm/usr/include
