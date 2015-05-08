#!/bin/sh

ndk-build -C demo/src/main/
cp demo/src/main/libs/armeabi/libimdemo.so demo/libs/armeabi/

#mkdir -p  imkit/src/main/libs/x86
#mkdir -p  imkit/src/main/libs/armeabi
ndk-build -C imlib/src/main/


cp imlib/src/main/libs/armeabi/libRongIMLib.so demo/libs/armeabi/
cp imlib/src/main/libs/armeabi/libRongIMLib.so imlib/libs/armeabi/

#mkdir -p  imkit/src/voip/libs/x86
mkdir -p  imkit/libs/armeabi
mkdir -p  imkit/src/voip/libs/armeabi
ndk-build -C imkit/src/voip/
cp imkit/src/voip/libs/armeabi/libRongIMVoip.so demo/libs/armeabi/
