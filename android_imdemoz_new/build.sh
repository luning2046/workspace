#!/bin/sh
#clean
rm -rf imbuild/build/
gradle clean
mkdir -p imbuild/libs/
mkdir -p demo/libs/armeabi/
mkdir -p demo/libs/x86
#ndk
ndk-build -C imlib/src/main/
cp -r imlib/src/main/libs/ demo/libs/

ndk-build -C imkit/src/voip/
cp -r imkit/src/voip/libs/ demo/libs/


#lib
gradle :imlib:assemble
cp imlib/build/intermediates/bundles/release/classes.jar imbuild/libs/imlib.jar
jar -umf imbuild/manifest.mf imbuild/libs/imlib.jar

#kit
gradle :imkit:assemble
cp imkit/build/intermediates/bundles/release/classes.jar imbuild/libs/imkit.jar
jar -umf imbuild/manifest.mf imbuild/libs/imkit.jar

#build
gradle :imbuild:assemble

#demo
gradle :demo:assemble

#obtain lib
mkdir -p imbuild/build/outputs/imlib/libs/armeabi
mkdir -p imbuild/build/outputs/imlib/libs/x86
cp imbuild/libs/imlib.jar imbuild/build/outputs/imlib/libs
cp -r imlib/src/main/libs/ imbuild/build/outputs/imlib/libs/
cp imlib/src/main/AndroidManifest.xml imbuild/build/outputs/imlib/

#obtain kit
mkdir -p imbuild/build/outputs/imkit/libs/armeabi
mkdir -p imbuild/build/outputs/imkit/libs/x86
cp imbuild/build/libs/imbuild.jar imbuild/build/outputs/imkit/libs/imkit.jar
cp imbuild/android-support-v4.jar imbuild/build/outputs/imkit/libs
cp -r demo/libs/ imbuild/build/outputs/imkit/libs/
cp imkit/src/main/AndroidManifest.xml imbuild/build/outputs/imkit/
cp -r imkit/src/main/assets imbuild/build/outputs/imkit
cp -r imkit/src/voip/assets imbuild/build/outputs/imkit
cp -r imkit/src/main/res imbuild/build/outputs/imkit
cp -r imkit/src/voip/res imbuild/build/outputs/imkit