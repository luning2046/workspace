#include <jni.h>
#include <dlfcn.h>

#undef printf
#include <android/log.h>
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG,"--HYJ--",__VA_ARGS__)

int VoidSet()
{
	printf("===VoidSet");

	void *  filehandle = dlopen("libRongIMLib.so", RTLD_LAZY );
	if (filehandle == 0)
	{
		printf("===fail");
		return -1;
	}

	int (*FuncA)() = 0;
	FuncA = (int(*)())dlsym(filehandle, "SetSaveLogToFileFlagForAndroid");
	if (FuncA == 0)
	{
		printf("===null");
		dlclose(filehandle);
		return -2;
	}

	int nSet = FuncA();
	dlclose(filehandle);

	printf("===ok");

	return 0;
}

jint JNI_OnLoad(JavaVM* vm, void *reserved)
{
	int _voidSet = VoidSet();
	return JNI_VERSION_1_4;
}
