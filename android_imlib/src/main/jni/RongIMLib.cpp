#include <string.h>
#include <jni.h>
#include <stdio.h>

#include "RCloudBiz.h"
#include "RmtpWork.h"

#ifdef DEBUG

#undef printf
#include <android/log.h>
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG,"--HYJ--",__VA_ARGS__)

#endif

#ifdef __cplusplus
extern "C" {
#endif

extern JavaVM* g_jvm;

char g_szImlibVersion[16] = { "0.0" };
char g_szImkitVersion[16] = { "0.0" };
char g_szVoipVersion[16]  = { "0.0" };

static jobject g_objMessage = 0;
static jobject g_objDiscussionInfo = 0;
static jobject g_objUserInfo = 0;

jint JNI_OnLoad(JavaVM* vm, void *reserved) {
	printf("----JNI_OnLoad().");
	JNIEnv *env;
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		return -1;
	}

	jclass cls = env->FindClass("io/rong/imlib/NativeObject$Message");
	if (cls)
	{
		printf("==== Message found ====");
		g_objMessage = env->NewGlobalRef(cls);
		env->DeleteLocalRef(cls);
	}
	else
	{
		printf("==== Message not found ====");
	}


	cls = env->FindClass("io/rong/imlib/NativeObject$DiscussionInfo");
	if (env->ExceptionCheck())
				env->ExceptionClear();
	if (cls)
	{
		printf("==== DiscussionInfo found ====");
		g_objDiscussionInfo = env->NewGlobalRef(cls);
		env->DeleteLocalRef(cls);
	}
	else
	{
		printf("==== DiscussionInfo not found ====");
	}

	try {
		cls = env->FindClass("io/rong/imlib/version/Version");
		if (env->ExceptionCheck())
			env->ExceptionClear();
		if (cls)
		{
			jfieldID fid = env->GetStaticFieldID(cls, "VERSION" ,"Ljava/lang/String;");
			if (env->ExceptionCheck())
				env->ExceptionClear();
			jstring str1 = (jstring)env->GetStaticObjectField(cls, fid);
			jboolean b_ret;
			const char *s1 = env->GetStringUTFChars(str1, &b_ret);
			strncpy(g_szImlibVersion, s1, 15);
			g_szImlibVersion[15] = 0;
		}
		cls = env->FindClass("io/rong/imkit/version/Version");
		if (env->ExceptionCheck())
			env->ExceptionClear();
		if (cls)
		{
			jfieldID fid = env->GetStaticFieldID(cls, "VERSION" ,"Ljava/lang/String;");
			if (env->ExceptionCheck())
				env->ExceptionClear();
			jstring str1 = (jstring)env->GetStaticObjectField(cls, fid);
			jboolean b_ret;
			const char *s1 = env->GetStringUTFChars(str1, &b_ret);
			strncpy(g_szImkitVersion, s1, 15);
			g_szImkitVersion[15] = 0;
		}
		cls = env->FindClass("io/rong/voip/version/Version");
		if (env->ExceptionCheck())
			env->ExceptionClear();
		if (cls)
		{
			jfieldID fid = env->GetStaticFieldID(cls, "VERSION" ,"Ljava/lang/String;");
			if (env->ExceptionCheck())
				env->ExceptionClear();
			jstring str1 = (jstring)env->GetStaticObjectField(cls, fid);
			jboolean b_ret;
			const char *s1 = env->GetStringUTFChars(str1, &b_ret);
			strncpy(g_szVoipVersion, s1, 15);
			g_szVoipVersion[15] = 0;
		}
	} catch (...) {
	}
	/*
	cls = env->FindClass("io/rong/imlib/NativeObject$UserInfo");
	if (cls)
	{
		printf("==== UserInfo found ====");
		g_objUserInfo = env->NewGlobalRef(cls);
		env->DeleteLocalRef(cls);
	}
	else
	{
		printf("==== UserInfo not found ====");
	}
	*/

	return JNI_VERSION_1_4;
}

class CJavaEnv {
public:
	operator JNIEnv*() {
		return env;
	}
	CJavaEnv() :
			env(0), m_bAlreadyAttach(false) {

		printf("CJavaEnv()");

		if (g_jvm == 0)
			printf("====== Not Call setJNIEnv =======\n");

		if (g_jvm) {
			if (g_jvm->GetEnv((void **) &env, JNI_VERSION_1_4) == JNI_OK)
				printf("---jni--- current thread already attach to javaVM \n"), m_bAlreadyAttach = true;
			else if (g_jvm->AttachCurrentThread(&env, NULL) != JNI_OK)
				printf("AttachCurrentThread() failed");
		}
	}
	~CJavaEnv() {

		printf("~CJavaEnv()");
		if (m_bAlreadyAttach == false)
		{
			if (g_jvm == 0)
				printf("====== Not Call setJNIEnv =======\n");

			if (g_jvm) {
				int check = g_jvm->GetEnv((void **) &env, JNI_VERSION_1_4);
				printf("---jni--- check %d \n", check);
				if (check != JNI_EDETACHED) {
					if (g_jvm->DetachCurrentThread() != JNI_OK)
						printf("DetachCurrentThread() failed");
				}

			}
		}
	}
private:
	JNIEnv *env;
	bool m_bAlreadyAttach;
};
class CAutoJString
{
public:
	CAutoJString(JNIEnv *env,jstring& jstr)
	{
		m_cstr = "";
		if (jstr)
		{
			jboolean b_ret;
			m_cstr = env->GetStringUTFChars(jstr, &b_ret);
			m_jstr = &jstr;
			m_env = env;
		}
	}
	operator const char*()
	{
		return m_cstr;
	}
	~CAutoJString()
	{
		if (m_env)
			m_env->ReleaseStringUTFChars(*m_jstr,m_cstr);
	}
private:
	const char* m_cstr;
	jstring* m_jstr;
	JNIEnv *m_env;

};

char* GetClassName(JNIEnv*& env, jclass cls)
{
	jobject obj = env->AllocObject(cls);

	// First get the class object
	jmethodID mid = env->GetMethodID(cls, "getClass", "()Ljava/lang/Class;");
	jobject clsObj = env->CallObjectMethod(obj, mid);

	// Now get the class object's class descriptor
	cls = env->GetObjectClass(clsObj);

	// Find the getName() method on the class object
	mid = env->GetMethodID(cls, "getName", "()Ljava/lang/String;");

	// Call the getName() to get a jstring object back
	jstring strObj = (jstring)env->CallObjectMethod(clsObj, mid);

	// Now get the c string from the java jstring object
	const char* str = env->GetStringUTFChars(strObj, NULL);

	// Release the memory pinned char array
	env->ReleaseStringUTFChars(strObj, str);
	env->DeleteLocalRef(clsObj);
	env->DeleteLocalRef(obj);

	return strdup(str);
}

void SetObjectValue_Int(JNIEnv*& env, jobject& obj, jclass& cls, const char* pszSetMethod, int nVal)
{
	jmethodID methodId = env->GetMethodID(cls, pszSetMethod, "(I)V");
	if (methodId != NULL)
	{
		env->CallVoidMethod(obj, methodId, (jint)nVal);
	}
	else
		printf("method: %s not found", pszSetMethod);
}

void SetObjectValue_Bool(JNIEnv*& env, jobject& obj, jclass& cls, const char* pszSetMethod, bool nVal)
{
	jmethodID methodId = env->GetMethodID(cls, pszSetMethod, "(Z)V");
	if (methodId != NULL)
	{
		env->CallVoidMethod(obj, methodId, (jboolean)nVal);
	}
	else
		printf("method: %s not found", pszSetMethod);
}

void SetObjectValue_Long(JNIEnv*& env, jobject& obj, jclass& cls, const char* pszSetMethod, long lVal)
{
	jmethodID methodId = env->GetMethodID(cls, pszSetMethod, "(J)V");
	if (methodId != NULL)
	{
		env->CallVoidMethod(obj, methodId, (jlong)lVal);
	}
	else
		printf("method: %s not found", pszSetMethod);
}

void SetObjectValue_LongLong(JNIEnv*& env, jobject& obj, jclass& cls, const char* pszSetMethod, long long lVal)
{
	jmethodID methodId = env->GetMethodID(cls, pszSetMethod, "(J)V");
	if (methodId != NULL)
	{
		env->CallVoidMethod(obj, methodId, (jlong)lVal);
	}
	else
		printf("method: %s not found", pszSetMethod);
}

void SetObjectValue_String(JNIEnv*& env, jobject& obj, jclass& cls, const char* pszSetMethod, const char* pszVal)
{
	jmethodID methodId = env->GetMethodID(cls, pszSetMethod, "(Ljava/lang/String;)V");
	if (methodId != NULL)
	{
		jstring jVal = env->NewStringUTF(pszVal);
		env->CallVoidMethod(obj, methodId, jVal);
		env->DeleteLocalRef(jVal);
	}
	else
		printf("method: %s not found", pszSetMethod);
}

void SetObjectValue_ByteArray(JNIEnv*& env, jobject& obj, jclass& cls, const char* pszSetMethod, const unsigned char* message, long nl)
{
	jmethodID methodId = env->GetMethodID(cls, pszSetMethod, "([B)V");
	if (methodId != NULL)
	{
		jbyte *by = (jbyte*) message;
		jbyteArray jMessage = env->NewByteArray(nl);
		env->SetByteArrayRegion(jMessage, 0, nl, by);
		env->CallVoidMethod(obj, methodId, jMessage);
		env->DeleteLocalRef(jMessage);
	}
	else
		printf("method: %s not found", pszSetMethod);
}


static jobject g_objSelfDiscussionsListener = 0;

class SelfDiscussionsListenerWrap: public SelfDiscussionsListener {
public:
	SelfDiscussionsListenerWrap(void* pObj) : m_pObj(pObj) {}
	virtual void OnReceive(DiscussionInfo* pArray,int nCount)
	{
		printf("receive discussionInfo: %d", nCount);
		CJavaEnv oEvn;
		JNIEnv* env = oEvn;
		jclass cls = env->GetObjectClass((jobject)m_pObj);
		if (cls != NULL)
		{
			jmethodID nMethodId = env->GetMethodID(cls, "onReceived", "([Lio/rong/imlib/NativeObject$DiscussionInfo;)V");
			if (nMethodId != NULL)
			{
				jclass clsDiscussionInfo = (jclass)g_objDiscussionInfo;
				if(clsDiscussionInfo != NULL)
				{
					jmethodID constuctor_id = env->GetMethodID(clsDiscussionInfo, "<init>", "()V");
					if(constuctor_id != NULL)
					{
						jobjectArray jo_array = env->NewObjectArray(nCount, (jclass)g_objDiscussionInfo, 0);
						jobject obj;
						for(int j = 0 ; j < nCount ;j++) {
							if(constuctor_id != NULL)
								obj = env->NewObject(clsDiscussionInfo, constuctor_id);
							else
								obj = env->AllocObject(clsDiscussionInfo);
							if(obj != NULL)
							{
								DiscussionInfo *p = &pArray[j];
								SetObjectValue_String(env, obj, clsDiscussionInfo, "setDiscussionId", p->m_Id);
								SetObjectValue_String(env, obj, clsDiscussionInfo, "setDiscussionName", p->m_Name);
								SetObjectValue_String(env, obj, clsDiscussionInfo, "setAdminId", p->m_AdminId);
								SetObjectValue_String(env, obj, clsDiscussionInfo, "setUserIds", p->m_UserIds);//\n分隔
								env->SetObjectArrayElement(jo_array, j, obj);
								env->DeleteLocalRef(obj);
							}
							else
								printf("NewObject fail");
						}

						env->CallVoidMethod((jobject)m_pObj, nMethodId, jo_array);
						env->DeleteLocalRef(jo_array);
						printf("call method success");

					}
					else
						printf("constuctor not found");

				}
				else
					printf("class discussionInfo not found");
			}
			else
				printf("onReceived not found");

			env->DeleteLocalRef(cls);
		}
		else
			printf("GetObjectClass fail");
	}

	void OnError(int status) {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "OnError", "(I)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId, (int) status);
			}
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}
protected:
	void* m_pObj;
};


static jobject g_objDiscussionInfoListener = 0;

class DiscussionInfoListenerWrap: public DiscussionInfoListener {
public:
	DiscussionInfoListenerWrap(void* pObj) : m_pObj(pObj) {}
	virtual void OnReceive(DiscussionInfo* pDiscussionInfo)
	{
		printf("receive discussionInfo: ");
		CJavaEnv oEvn;
		JNIEnv* env = oEvn;
		jclass cls = env->GetObjectClass((jobject)m_pObj);
		if (cls != NULL)
		{
			jmethodID nMethodId = env->GetMethodID(cls, "onReceived", "(Lio/rong/imlib/NativeObject$DiscussionInfo;)V");
			if (nMethodId != NULL)
			{
				jclass clsDiscussionInfo = (jclass)g_objDiscussionInfo;
				if(clsDiscussionInfo != NULL)
				{
					jmethodID constuctor_id = env->GetMethodID(clsDiscussionInfo, "<init>", "()V");
					if(constuctor_id != NULL)
					{
						jobject obj = env->NewObject(clsDiscussionInfo, constuctor_id);
						if(obj != NULL)
						{
							SetObjectValue_String(env, obj, clsDiscussionInfo, "setDiscussionId", pDiscussionInfo->m_Id);
							SetObjectValue_String(env, obj, clsDiscussionInfo, "setDiscussionName", pDiscussionInfo->m_Name);
							SetObjectValue_String(env, obj, clsDiscussionInfo, "setAdminId", pDiscussionInfo->m_AdminId);
							SetObjectValue_String(env, obj, clsDiscussionInfo, "setUserIds", pDiscussionInfo->m_UserIds);//\n分隔
							//发版本暂时不上传新代码
							SetObjectValue_Int(env, obj, clsDiscussionInfo, "setInviteStatus", pDiscussionInfo->m_InviteStatus);
							env->CallVoidMethod((jobject)m_pObj, nMethodId, obj);
							env->DeleteLocalRef(obj);
							printf("call method success");
						}
						else
							printf("NewObject fail");
					}
					else
						printf("constuctor not found");

				}
				else
					printf("class Message not found");
			}
			else
				printf("onReceived not found");

			env->DeleteLocalRef(cls);
		}
		else
			printf("GetObjectClass fail");
	}

	void OnError(int status) {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "OnError", "(I)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId, (int) status);
			}
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}
protected:
	void* m_pObj;
};

static jobject g_objMultiUsersListener = 0;


class MultiUsersListenerWrap : public MultiUsersListener {
public:
	MultiUsersListenerWrap(void* pObj) : m_pObj(pObj) {}
	virtual void OnReceive(UserInfo* pArray,int nCount)
	{
		printf("receive userInfo: %d", nCount);
		CJavaEnv oEvn;
		JNIEnv* env = oEvn;
		jclass cls = env->GetObjectClass((jobject)m_pObj);
		if (cls != NULL)
		{
			jmethodID nMethodId = env->GetMethodID(cls, "onReceived", "([Lio/rong/imlib/NativeObject$UserInfo;)V");
			if (nMethodId != NULL)
			{
				jclass clsUserInfo = (jclass)g_objUserInfo;
				if(clsUserInfo != NULL)
				{
					jmethodID constuctor_id = env->GetMethodID(clsUserInfo, "<init>", "()V");
					if(constuctor_id != NULL)
					{
						jobjectArray jo_array = env->NewObjectArray(nCount, (jclass)g_objUserInfo, 0);
						jobject obj;
						for(int j = 0 ; j < nCount ;j++) {
							if(constuctor_id != NULL)
								obj = env->NewObject(clsUserInfo, constuctor_id);
							else
								obj = env->AllocObject(clsUserInfo);
							if(obj != NULL)
							{
								UserInfo *p = &pArray[j];
								SetObjectValue_String(env, obj, clsUserInfo, "setUserId", p->m_Id);
								SetObjectValue_String(env, obj, clsUserInfo, "setUserName", p->m_Name);
								SetObjectValue_String(env, obj, clsUserInfo, "setUserPortrait", p->m_Portrait);
								env->SetObjectArrayElement(jo_array, j, obj);
								env->DeleteLocalRef(obj);
							}
							else
								printf("NewObject fail");
						}

						env->CallVoidMethod((jobject)m_pObj, nMethodId, jo_array);
						env->DeleteLocalRef(jo_array);
						printf("call method success");

					}
					else
						printf("constuctor not found");

				}
				else
					printf("class discussionInfo not found");
			}
			else
				printf("onReceived not found");

			env->DeleteLocalRef(cls);
		}
		else
			printf("GetObjectClass fail");
	}

	void OnError(int status) {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "OnError", "(I)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId, (int) status);
			}
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}
protected:
	void* m_pObj;

};


static jobject g_objMessageListener = 0;


class ReceiveMessageListenerWrap: public MessageListener {
public:
	ReceiveMessageListenerWrap(void* pObj) : m_pObj(pObj) {}
	virtual void OnReceive(Message* pMsg)
	{
		printf("receive message: %d, %s, %s, %s", pMsg->conversationType, pMsg->conversationId, pMsg->senderUserId, pMsg->content);
		//if(pMsg->content)
			//BizHexDump(pMsg->content,strlen(pMsg->content));

		CJavaEnv oEvn;
		JNIEnv* env = oEvn;
		jclass cls = env->GetObjectClass((jobject)m_pObj);
		if (cls != NULL)
		{
			jmethodID nMethodId = env->GetMethodID(cls, "onReceived", "(Lio/rong/imlib/NativeObject$Message;)V");
			if (nMethodId != NULL)
			{
				jclass clsMsg = (jclass)g_objMessage;
				if(clsMsg != NULL)
				{
					jmethodID constuctor_id = env->GetMethodID(clsMsg, "<init>", "()V");
					if (env->ExceptionCheck()){
						printf("-----------3,exception");
						env->ExceptionClear();
					}
					if(constuctor_id != NULL)
					{
						printf("######name####### %s", (char*)GetClassName(env, clsMsg));

						jobject obj = env->NewObject(clsMsg, constuctor_id);
						if(obj != NULL)
						{
							SetObjectValue_Int(env, obj, clsMsg, "setConversationType", pMsg->conversationType);
							SetObjectValue_String(env, obj, clsMsg, "setTargetId", pMsg->conversationId);
							SetObjectValue_Long(env, obj, clsMsg, "setMessageId", pMsg->messageId);
							SetObjectValue_Bool(env, obj, clsMsg, "setMessageDirection", pMsg->messageDirection);
							SetObjectValue_String(env, obj, clsMsg, "setSenderUserId", pMsg->senderUserId);
							SetObjectValue_Int(env, obj, clsMsg, "setReadStatus", pMsg->readStatus);
							SetObjectValue_Int(env, obj, clsMsg, "setSentStatus", pMsg->sentStatus);
							SetObjectValue_LongLong(env, obj, clsMsg, "setReceivedTime", pMsg->receivedTime);
							SetObjectValue_LongLong(env, obj, clsMsg, "setSentTime", pMsg->sentTime);
							SetObjectValue_String(env, obj, clsMsg, "setObjectName", pMsg->objectName);
							SetObjectValue_ByteArray(env, obj, clsMsg, "setContent", (const unsigned char*)(pMsg->content),strlen(pMsg->content));

							env->CallVoidMethod((jobject)m_pObj, nMethodId, obj);

							env->DeleteLocalRef(obj);
							printf("call method success");
						}
						else
							printf("NewObject fail");
					}
					else
						printf("constuctor not found");

				}
				else
					printf("class Message not found");
			}
			else
				printf("onReceived not found");

			env->DeleteLocalRef(cls);
		}
		else
			printf("GetObjectClass fail");
	}
protected:
	void* m_pObj;
};

class ConnectAckListenerWrap: public ConnectAckListener {
public:
	ConnectAckListenerWrap(void* pObj) :
			m_pObj(pObj) {
	}
	void operationComplete(ConnectAckStatus status,const char* multiTalkId) {
		printf("ConnectAckListener operationComplete:%d",status);

		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId =
			env->GetMethodID(cls, "operationComplete",
					"(ILjava/lang/String;)V");
			if (nMethodId != NULL) {
				jstring jmultiTalkId = env->NewStringUTF(multiTalkId);
				env->CallVoidMethod((jobject) m_pObj, nMethodId,(int)status,jmultiTalkId);
				env->DeleteLocalRef(jmultiTalkId);
			}
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}

protected:
	void* m_pObj;
};


static jobject g_objExceptionListener = 0;

class ExceptionListenerWrap: public ExceptionListener {
public:
	ExceptionListenerWrap(void* pObj) :
			m_pObj(pObj) {
	}

	void OnError(int status,const char* errorDescription) {
		printf("ExceptionListener onError:%d %s",status,errorDescription);

		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId =
			env->GetMethodID(cls, "onError",
					"(ILjava/lang/String;)V");
			if (nMethodId != NULL) {
				jstring jerrorDescription = env->NewStringUTF(errorDescription);
				env->CallVoidMethod((jobject) m_pObj, nMethodId,(int)status,jerrorDescription);
				env->DeleteLocalRef(jerrorDescription);
			}
			env->DeleteLocalRef(cls);
		}
		//env->DeleteGlobalRef((jobject) m_pObj);
		//delete this;
	}

protected:
	void* m_pObj;
};


class BizAckListenerWrap: public BizAckListener {
public:
	BizAckListenerWrap(void* pObj) :m_pObj(pObj) {
	}
	void operationComplete(OperationAckStatus opStatus,BizAckStatus status) {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "operationComplete",
					"(II)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId,(int)opStatus, (int) status);
			}
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}
protected:
	void* m_pObj;
};


class PublishAckListenerWrap: public PublishAckListener {
public:
	PublishAckListenerWrap(void* pObj) :
			m_pObj(pObj) {
	}
	void operationComplete(PublishAckStatus status) {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "operationComplete",
					"(I)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId, (int) status);
			}
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}
protected:
	void* m_pObj;
};

class CreateMultiTalkListenerWrap: public CreateMultiTalkListener {
public:
	CreateMultiTalkListenerWrap(void* pObj) :
			m_pObj(pObj) {
	}
	void OnSuccess(const char* multiTalkId) {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "OnSuccess",
					"(Ljava/lang/String;)V");
			if (nMethodId != NULL) {
				jstring jstr = env->NewStringUTF(multiTalkId);
				env->CallVoidMethod((jobject) m_pObj, nMethodId, jstr);
				env->DeleteLocalRef(jstr);
			}
			env->DeleteLocalRef(cls);
		}
	}
	void OnError(int status) {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "OnError", "(I)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId, (int) status);
			}
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}
protected:
	void* m_pObj;
};

class UserInfoOutputListenerWrap : public UserInfoOutputListener
{
public:
	UserInfoOutputListenerWrap(void* pObj) :m_pObj(pObj) {}
    virtual void OnResponse(const char* userId,const char* userName,const char* userPortrait)
    {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId =
					env->GetMethodID(cls, "onReceiveUserInfo",
							"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
			if (nMethodId != NULL) {
				jstring juserId = env->NewStringUTF(userId);
				jstring juserName = env->NewStringUTF(userName);
				jstring juserPortrait = env->NewStringUTF(userPortrait);
				env->CallVoidMethod((jobject) m_pObj, nMethodId, juserId,
						juserName, juserPortrait);
				env->DeleteLocalRef(juserId);
				env->DeleteLocalRef(juserName);
				env->DeleteLocalRef(juserPortrait);
			}
			env->DeleteLocalRef(cls);
		}
    }
    virtual void OnError(int status)
    {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
    }
protected:
	void* m_pObj;
};



class SendFileListenerWrap:public SendFileListener
{
public:
	SendFileListenerWrap(void* pObj) :m_pObj(pObj) {}
	virtual void OnProgress(int nProgress)
	{
		printf("---jni--- progress %d \n", nProgress);
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId =
			env->GetMethodID(cls, "OnProgress",
					"(I)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId,nProgress);
			}
			env->DeleteLocalRef(cls);
		}
	}
	virtual void OnError(int nErrorCode, const char* pszDescription)
	{
		printf("---jni--- send file error %d, %s \n", nErrorCode, pszDescription);
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId =
			env->GetMethodID(cls, "OnError",
					"(ILjava/lang/String;)V");
			if (nMethodId != NULL) {
				jstring jdesc = env->NewStringUTF(pszDescription);
				env->CallVoidMethod((jobject) m_pObj, nMethodId,nErrorCode,jdesc);
				env->DeleteLocalRef(jdesc);
			}
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}
protected:
	void* m_pObj;
};

class DownFileListenerWrap:public DownFileListener
{
public:
	DownFileListenerWrap(void* pObj) :m_pObj(pObj) {}
	virtual void OnProgress(int nProgress)
	{
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId =
			env->GetMethodID(cls, "OnProgress", "(I)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId,nProgress);
			}
			env->DeleteLocalRef(cls);
		}

	}
	virtual void OnError(int nErrorCode, const char* pszDescription)
	{
		printf("---jni--- down file error %d, %s \n", nErrorCode, pszDescription);
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jstring jdesc = env->NewStringUTF(pszDescription);
			if (nErrorCode == 0)
			{
				jmethodID nMethodId = env->GetMethodID(cls, "OnComplete", "(Ljava/lang/String;)V");
				if (nMethodId != NULL)
					env->CallVoidMethod((jobject) m_pObj, nMethodId, jdesc);
			}
			else
			{
				jmethodID nMethodId = env->GetMethodID(cls, "OnError", "(ILjava/lang/String;)V");
				if (nMethodId != NULL)
					env->CallVoidMethod((jobject) m_pObj, nMethodId,nErrorCode,jdesc);
			}
			env->DeleteLocalRef(jdesc);
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}

	virtual void OnData(const unsigned char* message, long nl)
	{
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "OnData", "([B)V");
			if (nMethodId != NULL) {
				jbyte *by = (jbyte*) message;
				jbyteArray jMessage = env->NewByteArray(nl);
				env->SetByteArrayRegion(jMessage, 0, nl, by);
				env->CallVoidMethod((jobject) m_pObj, nMethodId,jMessage);
				env->DeleteLocalRef(jMessage);
			}
			env->DeleteLocalRef(cls);
		}
	}
protected:
	void* m_pObj;
};

class EnvironmentChangeNotifyListenerWrap : public EnvironmentChangeNotifyListener
{
public:
	EnvironmentChangeNotifyListenerWrap(void* pObj){ m_pObj = pObj; }
    virtual ~EnvironmentChangeNotifyListenerWrap(){}
    /**
     *  环境改变，底层处理后的回调
     *
     *  @param nType 类型
     *  @param pData 附带数据
     */
    virtual void Complete(int nType, char* pData)
    {
    	CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "Complete", "(ILjava/lang/String;)V");
			if (nMethodId != NULL) {
				jstring jdesc = env->NewStringUTF(pData);
				env->CallVoidMethod((jobject) m_pObj, nMethodId, nType, jdesc);
				env->DeleteLocalRef(jdesc);
			}
			env->DeleteLocalRef(cls);
		}
    }
protected:
	void* m_pObj;
};

class WakeupQueryListenerWrap : public WakeupQueryListener
{
public:
	WakeupQueryListenerWrap(void* pObj){ m_pObj = pObj; }
    virtual ~WakeupQueryListenerWrap(){}
    /**
     *  请求唤醒
     *
     *  @param nType 请求类型
     */
    virtual void QueryWakeup(int nType)
    {
    	CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "QueryWakeup", "(I)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId, nType);
			}
			env->DeleteLocalRef(cls);
		}
    }
    /**
     *  释放唤醒锁
     */
    virtual void ReleaseWakup()
    {
    	CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "ReleaseWakup", "()V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId);
			}
			env->DeleteLocalRef(cls);
		}
    }
protected:
	void* m_pObj;
};

JNIEXPORT void Java_io_rong_imlib_NativeObject_setJNIEnv(JNIEnv* env,
		jobject obj, jobject objOnPublish) {
	printf("--jni-- setJNIEnv().");
	env->GetJavaVM(&g_jvm);
}
///ReleaseStringUTFChars

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    InitClient
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_io_rong_imlib_NativeObject_InitClient(JNIEnv *env,
		jobject,jstring jLocalPath,jstring jDatabase, jstring jDeviceId ,jstring jAppId,jstring jAppName) {
	printf("-----InitClient start-----");
	jboolean b_ret;
	if(jLocalPath == NULL){
		printf("-----jLocalPath is NULL-----\n");
		return -1;
	}

	if(jDatabase == NULL){
			printf("-----jDatabase is NULL-----\n");
			return -1;
		}

	if(jDeviceId == NULL){
			printf("-----jDeviceId is NULL-----\n");
			return -1;
		}

	if(jAppId == NULL){
			printf("-----jAppId is NULL-----\n");
			return -1;
		}

	if(jAppName == NULL) {
			printf("-----jAppName is NULL-----\n");
			return -1;
	}

	InitClientEx(CAutoJString(env,jAppName),CAutoJString(env,jLocalPath), CAutoJString(env,jDatabase),CAutoJString(env,jDeviceId),CAutoJString(env,jAppId));
	printf("-----InitClient end-----");
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    NativeConnect
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/rcloud/sdk/RCloudClient/OperationCompleteCallback;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_Connect
(JNIEnv *env, jobject, jstring jToken, jobject objCallback)
{
	printf("-----Connect start-----");
	jboolean b_ret;
	const char* token = env->GetStringUTFChars(jToken, &b_ret);
	jobject pObj = env->NewGlobalRef(objCallback);
	connectTo((char*)token,new ConnectAckListenerWrap(pObj));
	env->ReleaseStringUTFChars(jToken,token);
	printf("-----Connect end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SetMessageListener
(JNIEnv *env, jobject, jobject objCallback)
{
	printf("-----SetMessageListener start-----");
	if (g_objMessageListener)
		env->DeleteGlobalRef(g_objMessageListener);
	g_objMessageListener = env->NewGlobalRef(objCallback);
	SetMessageListener(new ReceiveMessageListenerWrap(g_objMessageListener));
	printf("-----SetMessageListener end-----");
}



/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    NativeDisconnect
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_Disconnect
(JNIEnv *env, jobject)
{
	printf("-----Disconnect start-----");
	disconnect();
	if (g_objMessageListener)
	{
		env->DeleteGlobalRef(g_objMessageListener);
		g_objMessageListener = 0;

	}

	printf("-----Disconnect end-----");
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    NativeSendPersonDirectMessage
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JLcom/rcloud/sdk/RCloudClient/OperationCompleteCallback;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SendSingleMessage
  (JNIEnv *env, jobject,jint transferType, jstring jUserId, jstring jClazzName, jbyteArray jpbMsg,jint jmsgLength, jlong messageId, jobject objCallback)
{
	printf("-----SendSingleMessage start-----");
	jboolean b_ret;
	const char* userId = env->GetStringUTFChars(jUserId, &b_ret);
	const char* clazzname = env->GetStringUTFChars(jClazzName, &b_ret);
	char* pszMsg = NULL;
	jbyte* pbMsg = (jbyte*)env->GetByteArrayElements(jpbMsg, 0);
	jsize len = env->GetArrayLength(jpbMsg);
	if((const char*)pbMsg) {
		pszMsg = new char[len+1];
		memset(pszMsg,0,len+1);
		strncpy(pszMsg,(const char*)pbMsg,len);
		//BizHexDump((const char*)pszMsg,len);
	}


	jobject pObj = env->NewGlobalRef(objCallback);
	SendSingleMessage(transferType,userId,clazzname,(const char*)pszMsg,messageId,new PublishAckListenerWrap(pObj));
	env->ReleaseStringUTFChars(jUserId,userId);
	env->ReleaseStringUTFChars(jClazzName,clazzname);
	env->ReleaseByteArrayElements(jpbMsg,pbMsg,0);
	if(pszMsg)
		delete [] pszMsg;
//	env->ReleaseStringUTFChars(jMsg,msg);
	printf("-----SendSingleMessage end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SendReceptionMessage
  (JNIEnv *env, jobject,jint transferType, jstring jUserId, jstring jClazzName, jbyteArray jpbMsg,jint jmsgLength, jlong messageId, jobject objCallback)
{
	printf("-----SendReceptionMessage start-----");
	jboolean b_ret;
	const char* userId = env->GetStringUTFChars(jUserId, &b_ret);
	const char* clazzname = env->GetStringUTFChars(jClazzName, &b_ret);
	char* pszMsg = NULL;
	jbyte* pbMsg = (jbyte*)env->GetByteArrayElements(jpbMsg, 0);
	jsize len = env->GetArrayLength(jpbMsg);
	if((const char*)pbMsg) {
		pszMsg = new char[len+1];
		memset(pszMsg,0,len+1);
		strncpy(pszMsg,(const char*)pbMsg,len);
		//BizHexDump((const char*)pszMsg,len);
	}


	jobject pObj = env->NewGlobalRef(objCallback);
	SendReceptionMessage(transferType,userId,clazzname,(const char*)pszMsg,messageId,new PublishAckListenerWrap(pObj));
	env->ReleaseStringUTFChars(jUserId,userId);
	env->ReleaseStringUTFChars(jClazzName,clazzname);
	env->ReleaseByteArrayElements(jpbMsg,pbMsg,0);
	if(pszMsg)
		delete [] pszMsg;
//	env->ReleaseStringUTFChars(jMsg,msg);
	printf("-----SendReceptionMessage end-----");
}

//
/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    NativeSendMultiTalkDirectMessage
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JLcom/rcloud/sdk/RCloudClient/OperationCompleteCallback;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SendMultiMessage
  (JNIEnv *env, jobject,jint transferType, jstring jGroupId, jstring jClazzName, jbyteArray jpbMsg,jint jmsgLength, jlong messageId, jobject objCallback)
{
	printf("-----SendDiscussionMessage start-----");
	jboolean b_ret;
	const char* userId = env->GetStringUTFChars(jGroupId, &b_ret);
	const char* clazzname = env->GetStringUTFChars(jClazzName, &b_ret);
	char* pszMsg = NULL;
	jbyte* pbMsg = (jbyte*)env->GetByteArrayElements(jpbMsg, 0);
	jsize len = env->GetArrayLength(jpbMsg);
	if((const char*)pbMsg) {
		pszMsg = new char[len+1];
		memset(pszMsg,0,len+1);
		strncpy(pszMsg,(const char*)pbMsg,len);
		//BizHexDump((const char*)pszMsg,len);
	}

	jobject pObj = env->NewGlobalRef(objCallback);
	SendDiscussionMessage(transferType,userId,clazzname,(const char*)pszMsg,messageId,new PublishAckListenerWrap(pObj));
	env->ReleaseStringUTFChars(jGroupId,userId);
	env->ReleaseStringUTFChars(jClazzName,clazzname);
	env->ReleaseByteArrayElements(jpbMsg,pbMsg,0);
//	env->ReleaseStringUTFChars(jMsg,msg);
	if(pszMsg)
		delete [] pszMsg;
	printf("-----SendDiscussionMessage end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SendGroupMessage
  (JNIEnv *env, jobject,jint transferType, jstring jGroupId, jstring jClazzName, jbyteArray jpbMsg,jint jmsgLength, jlong messageId, jobject objCallback)
{
	printf("-----SendGroupMessage start-----");
	jboolean b_ret;
	const char* userId = env->GetStringUTFChars(jGroupId, &b_ret);
	const char* clazzname = env->GetStringUTFChars(jClazzName, &b_ret);
	char* pszMsg = NULL;
	jbyte* pbMsg = (jbyte*)env->GetByteArrayElements(jpbMsg, 0);
	jsize len = env->GetArrayLength(jpbMsg);
	if((const char*)pbMsg) {
		pszMsg = new char[len+1];
		memset(pszMsg,0,len+1);
		strncpy(pszMsg,(const char*)pbMsg,len);
		//BizHexDump((const char*)pszMsg,len);
	}

	jobject pObj = env->NewGlobalRef(objCallback);
	SendGroupMessage(transferType,userId,clazzname,(const char*)pszMsg,messageId,new PublishAckListenerWrap(pObj));
	env->ReleaseStringUTFChars(jGroupId,userId);
	env->ReleaseStringUTFChars(jClazzName,clazzname);
	env->ReleaseByteArrayElements(jpbMsg,pbMsg,0);
//	env->ReleaseStringUTFChars(jMsg,msg);
	if(pszMsg)
		delete [] pszMsg;
	printf("-----SendGroupMessage end-----");
}



/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    NativeCreateMultiTalk
 * Signature: (Ljava/lang/String;Lcom/rcloud/sdk/CreateMultiTalkListener;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_CreateDiscussion
(JNIEnv *env, jobject, jstring jName, jobject objCallback)
{
//	env->NewObjectArray()
	printf("-----CreateDiscussion start-----");
	jboolean b_ret;
	char* name = (char*)env->GetStringUTFChars(jName, &b_ret);
	jobject pObj = env->NewGlobalRef(objCallback);
	CreateDiscussion(name,new CreateMultiTalkListenerWrap(pObj));
	env->ReleaseStringUTFChars(jName,name);
	printf("-----CreateDiscussion end-----");
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    RenameDiscussion
 * Signature: (Ljava/lang/String;Lcom/rcloud/sdk/PublishAckListener;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_RenameDiscussion
(JNIEnv *env, jobject, jstring jTargetId,jstring jName, jobject objCallback)
{
	printf("-----RenameDiscussion start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	RenameDiscussion(CAutoJString(env,jTargetId),CAutoJString(env,jName),new PublishAckListenerWrap(pObj));
	printf("-----RenameDiscussion end-----");
}


/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    SetInviteStatus
 * Signature: (Ljava/lang/Int;Lcom/rcloud/sdk/PublishAckListener;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SetInviteStatus
(JNIEnv *env, jobject, jstring targetId,jint inviteStatus, jobject objCallback)
{
	printf("-----SetInviteStatus start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	SetInviteStatus(CAutoJString(env,targetId),inviteStatus,new PublishAckListenerWrap(pObj));
	printf("-----SetInviteStatus end-----");
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    NativeSubscribeChannel
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/rcloud/sdk/RCloudClient/OperationCompleteCallback;)V
 */
//JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_NativeSubscribeChannel
//(JNIEnv *env, jobject, jstring jGroupId, jstring jUserIds, jobject objCallback)
//{
//	//const char* groupId, char* userIds, PublishAckListener* listener
//	printf("-----SubscribeChannel start-----");
//	jboolean b_ret;
//	const char* groupId = env->GetStringUTFChars(jGroupId, &b_ret);
//	char* userIds = (char*)env->GetStringUTFChars(jUserIds, &b_ret);
//	jobject pObj = env->NewGlobalRef(objCallback);
//	subscribeChannel(groupId,userIds,new PublishAckListenerWrap(pObj));
//	env->ReleaseStringUTFChars(jGroupId,groupId);
//	env->ReleaseStringUTFChars(jUserIds,userIds);
//	printf("-----SubscribeChannel end-----");
//}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    NativeSubscribeMultiTalk
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/rcloud/sdk/RCloudClient/OperationCompleteCallback;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_InviteMemberToDiscussion
(JNIEnv *env, jobject, jstring jGroupId, jobjectArray userArray, jobject objCallback)
{
	printf("-----InviteMemberToDiscussion start-----");

	int userCount = env->GetArrayLength(userArray);
	if(userCount == 0)
		return;

	TargetEntry userEntrys[userCount];

    for (int i=0; i<userCount; i++) {
        jstring userIdString = (jstring) env->GetObjectArrayElement(userArray, i);
        const char *userId = env->GetStringUTFChars(userIdString, 0);
        if(userId) {
        	strcpy(userEntrys[i].targetId,userId);
        	env->ReleaseStringUTFChars(userIdString,userId);
        }
        else {
        	memset(&(userEntrys[i].targetId),0,64);
        }
    }

	jboolean b_ret;
	const char* groupId = env->GetStringUTFChars(jGroupId, &b_ret);
	jobject pObj = env->NewGlobalRef(objCallback);
	InviteMemberToDiscussion(groupId,userEntrys,userCount,new PublishAckListenerWrap(pObj));
	env->ReleaseStringUTFChars(jGroupId,groupId);
	printf("-----InviteMemberToDiscussion end-----");
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    GetUserInfo
 * Signature: (Ljava/lang/String;Lcom/rcloud/sdk/RCloudClient/GetUserInfoOutputCallback;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_GetUserInfo
  (JNIEnv *env, jobject, jstring jUserId, jobject objCallback,jboolean fetchRemote = false)
{
	printf("-----GetUserInfo start-----");
	jboolean b_ret;
	const char* userId = env->GetStringUTFChars(jUserId, &b_ret);
	jobject pObj = env->NewGlobalRef(objCallback);
	GetUserInfo(userId,new UserInfoOutputListenerWrap(pObj),fetchRemote);
	env->ReleaseStringUTFChars(jUserId,userId);
	printf("-----GetUserInfo end-----");
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    NativeSendFile
 * Signature: (Ljava/lang/String;I[BJLcom/rcloud/sdk/RCloudClient/SendFileCallback;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SendFile
  (JNIEnv *env, jobject, jstring jtargetId, jint jcategory,jint jtype,jstring jKey, jbyteArray jpbPayload, jlong jnl, jobject jobjCallback)
{
	printf("-----SendFile start-----");
	jboolean b_ret;
	const char* targetId = env->GetStringUTFChars(jtargetId, &b_ret);
	const char* mimeKey = env->GetStringUTFChars(jKey, &b_ret);
	jbyte* pbPayload = (jbyte*)env->GetByteArrayElements(jpbPayload, 0);
	jobject pObj = env->NewGlobalRef(jobjCallback);
	SendFile(targetId,(int)jcategory,(int)jtype,mimeKey,(const unsigned char*)pbPayload,(long)jnl,new SendFileListenerWrap(pObj));
	env->ReleaseStringUTFChars(jtargetId,targetId);
	env->ReleaseByteArrayElements(jpbPayload,pbPayload,0);
	printf("-----SendFile end-----");
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    DownFile
 * Signature: (ILjava/lang/String;Lcom/rcloud/sdk/RCloudClient/DownFileCallback;)V
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_DownFile
  (JNIEnv *env, jobject, jstring jtargetId, jint jcategory,jint jtype, jstring jkey, jobject jobjCallback)
{
	printf("-----DownFile start-----");
	jboolean b_ret;
	const char* targetId = env->GetStringUTFChars(jtargetId, &b_ret);
	const char* key = env->GetStringUTFChars(jkey, &b_ret);
	jobject pObj = env->NewGlobalRef(jobjCallback);
	DownFile(targetId,(int)jcategory,(int)jtype,key,new DownFileListenerWrap(pObj));
	env->ReleaseStringUTFChars(jkey,key);
	printf("-----DownFile end-----");
}
/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    GetPagedMessage
 * Signature: (Ljava/lang/String;JII)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_io_rong_imlib_NativeObject_GetPagedMessageOld
  (JNIEnv *env, jobject, jstring targetId, jlong beginId, jint count, jint category)
{
	char* result = 0;
	if(GetPagedMessage(CAutoJString(env,targetId),beginId,count,category,&result))
	{
		jstring ret = env->NewStringUTF(result);
		if (result)
			delete result;
		return ret;
	}
	return NULL;
}

JNIEXPORT jobjectArray JNICALL Java_io_rong_imlib_NativeObject_GetPagedMessageEx
  (JNIEnv *env, jobject, jstring targetId, jlong beginId, jint count, jint category)
{
	Message* result = new Message[count];
	if(result == NULL)
		return NULL;
	int fetchCount = 0;
	if(GetPagedMessageEx(CAutoJString(env,targetId),beginId,count,category,result,&fetchCount))
	{
		jobjectArray jo_array = env->NewObjectArray(fetchCount, (jclass)g_objMessage, 0);
		for(int j = 0 ; j < fetchCount ;j++) {
			//发现java Message类，如果失败，程序返回
			jclass clsMsg = (jclass)g_objMessage;
			if(clsMsg == 0)
				return NULL;

			jobject obj;

			jmethodID constuctor_id = env->GetMethodID(clsMsg, "<init>", "()V");
			if(constuctor_id != NULL)
				obj = env->NewObject(clsMsg, constuctor_id);
			else
				obj = env->AllocObject(clsMsg);

			Message *pMsg = &result[j];
			SetObjectValue_Int(env, obj, clsMsg, "setConversationType", pMsg->conversationType);
			SetObjectValue_String(env, obj, clsMsg, "setConversationId", pMsg->conversationId);
			SetObjectValue_Long(env, obj, clsMsg, "setMessageId", pMsg->messageId);
			SetObjectValue_Int(env, obj, clsMsg, "setMessageDirection", pMsg->messageDirection);
			SetObjectValue_String(env, obj, clsMsg, "setSenderUserId", pMsg->senderUserId);
			SetObjectValue_Int(env, obj, clsMsg, "setReadStatus", pMsg->readStatus);
			SetObjectValue_Int(env, obj, clsMsg, "setSentStatus", pMsg->sentStatus);
			SetObjectValue_Long(env, obj, clsMsg, "setReceivedTime", pMsg->receivedTime);
			SetObjectValue_Long(env, obj, clsMsg, "setSentTime", pMsg->sentTime);
			SetObjectValue_String(env, obj, clsMsg, "setObjectName", pMsg->objectName);
			SetObjectValue_String(env, obj, clsMsg, "setContent", pMsg->content);
			//将对象obj添加到object array中
			if(j<count){
				env->SetObjectArrayElement(jo_array, j, obj);
			}else{
				break;
			}
		}
		delete result;
		return jo_array;
	}
	delete result;
	return NULL;
}
/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    GetLatestMessage
 * Signature: (Ljava/lang/String;II)Ljava/lang/String;
 */
//JNIEXPORT jstring JNICALL Java_io_rong_imlib_NativeObject_GetLatestMessage
//  (JNIEnv *env, jobject, jstring targetId, jint count, jint category)
//{
//	char* result = 0;
//	if(GetLatestMessage(CAutoJString(env,targetId),count,category,&result))
//	{
//		jstring ret = env->NewStringUTF(result);
//		if (result) delete result;
//		return ret;
//	}
//	return NULL;
//}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    DeleteMessage
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_ClearMessages
  (JNIEnv *env, jobject,jint categoryId, jstring targetId)
{
	return ClearMessages(CAutoJString(env,targetId),categoryId);
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    DeleteMessage
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_DeleteMessages
  (JNIEnv *env, jobject, jintArray idArray)
{
	int idCount = env->GetArrayLength(idArray);
	if(idCount == 0)
		return false;

	MessageEntry messageEntrys[idCount];
	jboolean flag = false;
	jint *messageIds = env->GetIntArrayElements(idArray, &flag);
	for (int i = 0; i<idCount; i++)
		messageEntrys[i].messageId = messageIds[i];

	env->ReleaseIntArrayElements( idArray , messageIds, 0);

	return DeleteMessage(messageEntrys,idCount);
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    SaveMessage
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL Java_io_rong_imlib_NativeObject_SaveMessage
  (JNIEnv *env, jobject, jbyteArray jpbMsg,jint jmsgLength, jstring targetId, jstring objectName, jstring sender_user_id, jint category)
{
	printf("-----SaveMessage start-----");
	if(targetId == NULL){
		printf("-----SaveMessage targetId is null-----");
		return -1;
	}

	if(objectName == NULL){
		printf("-----SaveMessage objectName is null-----");
		return -1;
	}

	if(sender_user_id == NULL){
		printf("-----SaveMessage sender_user_id is null-----");
		return -1;
	}

	long msgId = 0;
	jbyte* pbMsg = (jbyte*)env->GetByteArrayElements(jpbMsg, 0);
	jsize len = env->GetArrayLength(jpbMsg);
	if((const char*)pbMsg) {
		char* pszMsg = new char[len+1];
		memset(pszMsg,0,len+1);
		strncpy(pszMsg,(const char*)pbMsg,len);
		//BizHexDump((const char*)pszMsg,len);

		msgId = SaveMessage((const char*)pszMsg,CAutoJString(env,targetId),CAutoJString(env,objectName),
			CAutoJString(env,sender_user_id),category);
		delete []pszMsg;
	}
	env->ReleaseByteArrayElements(jpbMsg,pbMsg,0);

	return msgId;
}


//JNIEXPORT jstring JNICALL Java_io_rong_imlib_NativeObject_GetRecentConversationEx
//  (JNIEnv *env, jobject)
//{
//	Message* result = new Message[count];
//	if(result == NULL)
//		return NULL;
//	int fetchCount = 0;
//	if(GetPagedMessageEx(CAutoJString(env,targetId),beginId,count,category,result,&fetchCount))
//	{
//		jobjectArray jo_array = env->NewObjectArray(fetchCount,(jclass)g_objMessage, 0);
//		for(int j = 0 ; j < fetchCount ;j++) {
//			//发现java Message类，如果失败，程序返回
//			jclass clsMsg = (jclass)g_objMessage;
//			if(clsMsg == 0)
//				return NULL;
//
//			jobject obj;
//
//			jmethodID constuctor_id = env->GetMethodID(clsMsg, "<init>", "()V");
//			if(constuctor_id != NULL)
//				obj = env->NewObject(clsMsg, constuctor_id);
//			else
//				obj = env->AllocObject(clsMsg);
//
//			Message *pMsg = &result[j];
//			SetObjectValue_Int(env, obj, clsMsg, "setConversationType", pMsg->conversationType);
//			SetObjectValue_String(env, obj, clsMsg, "setConversationId", pMsg->conversationId);
//			SetObjectValue_Long(env, obj, clsMsg, "setMessageId", pMsg->messageId);
//			SetObjectValue_Int(env, obj, clsMsg, "setMessageDirection", pMsg->messageDirection);
//			SetObjectValue_String(env, obj, clsMsg, "setSenderUserId", pMsg->senderUserId);
//			SetObjectValue_Int(env, obj, clsMsg, "setReadStatus", pMsg->readStatus);
//			SetObjectValue_Int(env, obj, clsMsg, "setSentStatus", pMsg->sentStatus);
//			SetObjectValue_Long(env, obj, clsMsg, "setReceivedTime", pMsg->receivedTime);
//			SetObjectValue_Long(env, obj, clsMsg, "setSentTime", pMsg->sentTime);
//			SetObjectValue_String(env, obj, clsMsg, "setObjectName", pMsg->objectName);
//			SetObjectValue_String(env, obj, clsMsg, "setContent", pMsg->content);
//			//将对象obj添加到object array中
//			if(j<count){
//				env->SetObjectArrayElement(jo_array, j, obj);
//			}else{
//				break;
//			}
//		}
//		delete result;
//		return jo_array;
//	}
//	delete result;
//	return NULL;
//}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    DeleteConversation
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_RemoveConversation
  (JNIEnv *env, jobject,jint categoryId, jstring targetId)
{
	return RemoveConversation(CAutoJString(env,targetId),categoryId);
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    ClearUnread
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_ClearUnread
  (JNIEnv *env, jobject, int categoryId, jstring targetId)
{
	return ClearUnread(CAutoJString(env,targetId), categoryId);
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    SetIsTop
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_SetIsTop
  (JNIEnv *env, jobject, jint categoryId, jstring targetId, jboolean bTop)
{
	return SetIsTop(CAutoJString(env,targetId),categoryId,bTop);
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    GetTotalUnreadCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_io_rong_imlib_NativeObject_GetTotalUnreadCount
  (JNIEnv *env, jobject)
{
	return GetTotalUnreadCount();
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    GetUnreadCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_io_rong_imlib_NativeObject_GetUnreadCount
  (JNIEnv *env, jobject,jstring targetId,jint categoryId)
{
	if(targetId == NULL) return -1;
	return GetUnreadCount(CAutoJString(env,targetId),categoryId);
}



/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    GenerateKey
 * mimeType: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_io_rong_imlib_NativeObject_GenerateKey
  (JNIEnv *env, jobject, jint mimeType)
{
	char* result = new char[256];
	if (result && GenerateKey(mimeType,result))
	{
		jstring ret = env->NewStringUTF(result);
		delete result;
		return ret;
	}
	return NULL;
}

/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    RegisterMessageType
 * mimeType: ()Ljava/lang/String;
 */
JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_RegisterMessageType
  (JNIEnv *env, jobject, jstring clazz, jint operateBits)
{
	return RegisterMessageType(CAutoJString(env,clazz),operateBits);
}



JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_SetTextMessageDraft
  (JNIEnv *env, jobject, jint categoryId,jstring targetId, jstring content)
{
	return SetTextMessageDraft(CAutoJString(env,targetId),categoryId,CAutoJString(env,content));
}

JNIEXPORT jstring JNICALL Java_io_rong_imlib_NativeObject_GetTextMessageDraft
  (JNIEnv *env, jobject, jint categoryId, jstring targetId)
{
	char* result = 0;
	if (GetTextMessageDraft(CAutoJString(env,targetId),categoryId, &result))
	{
		if ( result )
		{
			jstring ret = env->NewStringUTF(result);
			delete result;
			return ret;
		}
	}
	return NULL;
}



JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_SetMessageExtra
  (JNIEnv *env, jobject, jlong messageId, jstring value)
{
	if(value == NULL)
		return SetTextMessageExtra(messageId,NULL);
	else
		return SetTextMessageExtra(messageId,CAutoJString(env,value));
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_QuitDiscussion
(JNIEnv *env, jobject, jstring jGroupId, jobject objCallback)
{
	printf("-----QuitDiscussion start-----");
	jboolean b_ret;
	const char* groupId = env->GetStringUTFChars(jGroupId, &b_ret);
	jobject pObj = env->NewGlobalRef(objCallback);
	QuitDiscussion(groupId,new PublishAckListenerWrap(pObj));
	env->ReleaseStringUTFChars(jGroupId,groupId);
	printf("-----QuitDiscussion end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_RemoveMemberFromDiscussion
(JNIEnv *env, jobject, jstring jGroupId, jstring jUserId, jobject objCallback)
{
	printf("-----RemoveMemberFromDiscussion start-----");
	jboolean b_ret;
	const char* groupId = env->GetStringUTFChars(jGroupId, &b_ret);
	char* userId = (char*)env->GetStringUTFChars(jUserId, &b_ret);
	jobject pObj = env->NewGlobalRef(objCallback);
	RemoveMemberFromDiscussion(groupId,userId,new PublishAckListenerWrap(pObj));
	env->ReleaseStringUTFChars(jGroupId,groupId);
	env->ReleaseStringUTFChars(jUserId,userId);
	printf("-----RemoveMemberFromDiscussion end-----");
}

/**
 *  接收应用的环境改变事件通知
 *
 *  @param nType     事件类型，101-网络切换，102-应用进入后台，103-应用进入前台，104-锁屏，105-心跳
 *  @param pbData    依据nType的事件附加数据，待定
 *  @param nDataSize 数据大小，字节数
 *  @param pListener 事件改变的回调
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_EnvironmentChangeNotify(
		JNIEnv *env, jobject, jint nType, jbyteArray jpbData, jint nDataSize, jobject objCallback)
{
	jbyte* pbPayload = 0;
	if (jpbData)
		pbPayload = (jbyte*)env->GetByteArrayElements(jpbData, 0);
	jobject pObj = env->NewGlobalRef(objCallback);
	EnvironmentChangeNotifyListenerWrap* p = new EnvironmentChangeNotifyListenerWrap((void*)pObj);
	EnvironmentChangeNotify(nType, (unsigned char*)pbPayload, nDataSize, p);
	if (jpbData && pbPayload)
		env->ReleaseByteArrayElements(jpbData,pbPayload,0);
}
/**
 *  android设置唤醒的监听器
 *
 *  @param pListener 唤醒监听器
 */
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SetWakeupQueryListener(
		JNIEnv *env, jobject, jobject objCallback)
{
	jobject pObj = env->NewGlobalRef(objCallback);
	SetWakeupQueryListener(new WakeupQueryListenerWrap((void*)pObj));
}

JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_SetReadStatus
  (JNIEnv *env, jobject, jlong messageId, jint status)
{
	return SetReadStatus(messageId,status);
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_GetDiscussionInfo(
		JNIEnv *env, jobject,jstring discussionId,jboolean fetchRemote,jobject objCallback)
{
	if (g_objDiscussionInfoListener)
		env->DeleteGlobalRef(g_objDiscussionInfoListener);
	jobject g_objDiscussionInfoListener = env->NewGlobalRef(objCallback);

	GetDiscussionInfo(CAutoJString(env,discussionId),2,fetchRemote,new DiscussionInfoListenerWrap(g_objDiscussionInfoListener));
}


JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SelfDiscussions(
		JNIEnv *env, jobject,jint startPage,jint countPerPage, jobject objCallback)
{
	if (g_objSelfDiscussionsListener)
		env->DeleteGlobalRef(g_objSelfDiscussionsListener);
	jobject g_objSelfDiscussionsListener = env->NewGlobalRef(objCallback);

	SelfDiscussions(startPage,countPerPage,new SelfDiscussionsListenerWrap(g_objSelfDiscussionsListener));
}


//JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_GetMultiUsers(
//		JNIEnv *env, jobject,jstring userIds, jobject objCallback)
//{
//	if (g_objMultiUsersListener)
//		env->DeleteGlobalRef(g_objMultiUsersListener);
//	jobject g_objMultiUsersListener = env->NewGlobalRef(objCallback);
//
//	GetMultiUsers(CAutoJString(env,userIds),new MultiUsersListenerWrap(g_objMultiUsersListener));
//}


JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SetExceptionListener
(JNIEnv *env, jobject, jobject objCallback)
{
	printf("-----SetExcptionListener start-----");
	if (g_objExceptionListener)
		env->DeleteGlobalRef(g_objExceptionListener);
	g_objExceptionListener = env->NewGlobalRef(objCallback);
	SetExceptionListener(new ExceptionListenerWrap(g_objExceptionListener));
	printf("-----SetExcptionListener end-----");
}


/*
 * Class:     io_rong_imlib_NativeObject
 * Method:    GetPagedMessage
 * Signature: (Ljava/lang/String;JII)Ljava/lang/String;
 */
JNIEXPORT jbyteArray JNICALL Java_io_rong_imlib_NativeObject_GetPagedMessage
  (JNIEnv *env, jobject, jstring targetId, jlong beginId, jint count, jint category)
{
	printf("-----GetPagedMessage start-----\n");
	char* result = 0;

	if(GetPagedMessage(CAutoJString(env,targetId),beginId,count,category,&result)) {
		printf("-----GetPagedMessage return true\n");
		if(result == 0)
			return NULL;

		int len = strlen(result);
		printf("-----GetPagedMessage result:%s length:%d\n",result,len);
		jbyteArray ret = env->NewByteArray(len);
		if(ret) {
			printf("-----GetPagedMessage NewByteArray success!\n");
			env->SetByteArrayRegion(ret, 0, len, (jbyte*)result);
		}
		if (result)
			delete result;
		printf("-----GetPagedMessage end-----\n");
		return ret;
	}
	else {
		if(result)
			delete result;

		printf("-----GetPagedMessage return false return default {\"result\":[]}\n");
		char defaultJson[] = "{\"result\":[]}";
		int len = strlen(defaultJson);
		printf("-----GetPagedMessage result:%s length:%d\n",defaultJson,len);
		jbyteArray ret = env->NewByteArray(len);
		if(ret) {
			printf("-----GetPagedMessage NewByteArray success!\n");
			env->SetByteArrayRegion(ret, 0, len, (jbyte*)defaultJson);
		}
		printf("-----GetPagedMessage end-----\n");
		return ret;
	}
	printf("-----GetPagedMessage end-----\n");
	return NULL;
}

JNIEXPORT jbyteArray JNICALL Java_io_rong_imlib_NativeObject_GetConversationList
(JNIEnv *env, jobject, jintArray idArray)
{
	printf("-----GetConversationList start-----\n");
	char* result = 0;

	int idCount = env->GetArrayLength(idArray);
	if(idCount == 0)
		return NULL;

	ConversationEntry conversationEntrys[idCount];
	jboolean flag = false;
	jint *typeIds = env->GetIntArrayElements(idArray, &flag);
	if(typeIds == NULL)
		return NULL;

	for (int i=0; i<idCount; i++)
		conversationEntrys[i].typeId = typeIds[i];


	env->ReleaseIntArrayElements( idArray , typeIds, 0);

	if(GetConversationList(conversationEntrys,idCount,&result)) {
		printf("-----GetConversationList return true\n");
		if(result == 0)
			return NULL;

		int len = strlen(result);
		printf("-----GetConversationList result:%s length:%d\n",result,len);
		jbyteArray ret = env->NewByteArray(len);
		if(ret) {
			printf("-----GetConversationList NewByteArray success!\n");
			env->SetByteArrayRegion(ret, 0, len, (jbyte*)result);
		}
		if (result)
			delete result;
		printf("-----GetConversationList end-----\n");
		return ret;
	}
	else {
		if(result)
			delete result;

		printf("-----GetConversationList return false return default {\"result\":[]}\n");
		char defaultJson[] = "{\"result\":[]}";
		int len = strlen(defaultJson);
		printf("-----GetConversationList result:%s length:%d\n",defaultJson,len);
		jbyteArray ret = env->NewByteArray(len);
		if(ret) {
			printf("-----GetConversationList NewByteArray success!\n");
			env->SetByteArrayRegion(ret, 0, len, (jbyte*)defaultJson);
		}
		printf("-----GetConversationList end-----\n");
		return ret;

	}
	printf("-----GetConversationList end-----\n");
	return NULL;

}



JNIEXPORT jboolean JNICALL Java_io_rong_imlib_NativeObject_SetMessageContent
  (JNIEnv *env, jobject, jlong messageId,jbyteArray jpbMsg)
{
	jboolean ret = false;
	jbyte* pbMsg = (jbyte*)env->GetByteArrayElements(jpbMsg, 0);
	jsize len = env->GetArrayLength(jpbMsg);
	if((const char*)pbMsg) {
		char* pszMsg = new char[len+1];
		memset(pszMsg,0,len+1);
		strncpy(pszMsg,(const char*)pbMsg,len);
		//BizHexDump((const char*)pszMsg,len);

		ret = SetMessageContent(messageId,(const char*)pszMsg);
		delete []pszMsg;
	}
	env->ReleaseByteArrayElements(jpbMsg,pbMsg,0);

	return ret;
}

JNIEXPORT jstring JNICALL Java_io_rong_imlib_NativeObject_SaveFileToCache
  (JNIEnv *env, jobject,  jstring jtargetId, jint jcategory,jint jtype,jstring jKey, jbyteArray jpbPayload, jlong jnl)
{
	printf("-----SaveFileToCache start-----");
	char* result = new char[256];
	if (result && SaveFileToCache(CAutoJString(env,jtargetId),jcategory,jtype,CAutoJString(env,jKey),(const unsigned char*)jpbPayload,(long)jnl,result))
	{
		jstring ret = env->NewStringUTF(result);
		delete result;
		return ret;
	}
	printf("-----SaveFileToCache end-----");
	return NULL;
}



JNIEXPORT jbyteArray JNICALL Java_io_rong_imlib_NativeObject_GetConversation
(JNIEnv *env, jobject,jstring jtargetId, jint jcategoryId)
{
	printf("-----GetConversation start-----\n");
	char* result = 0;

	if(GetConversation(CAutoJString(env,jtargetId),jcategoryId,&result)) {
		printf("-----GetConversation return true\n");
		if(result == 0)
			return NULL;

		int len = strlen(result);
		printf("-----GetConversation result:%s length:%d\n",result,len);
		jbyteArray ret = env->NewByteArray(len);
		if(ret) {
			printf("-----GetConversation NewByteArray success!\n");
			env->SetByteArrayRegion(ret, 0, len, (jbyte*)result);
		}
		if (result)
			delete result;
		printf("-----GetConversation end-----\n");
		return ret;
	}
	else {
		if(result)
			delete result;

		printf("-----GetConversation return false return default {\"result\":[]}\n");
		char defaultJson[] = "{\"result\":[]}";
		int len = strlen(defaultJson);
		printf("-----GetConversation result:%s length:%d\n",defaultJson,len);
		jbyteArray ret = env->NewByteArray(len);
		if(ret) {
			printf("-----GetConversation NewByteArray success!\n");
			env->SetByteArrayRegion(ret, 0, len, (jbyte*)defaultJson);
		}
		printf("-----GetConversation end-----\n");
		return ret;

	}
	printf("-----GetConversation end-----\n");
	return NULL;

}

//黑名单函数
//add
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_AddToBlacklist
(JNIEnv *env, jobject, jstring targetId, jobject objCallback)
{
	printf("-----AddToBlacklist start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	AddToBlacklist(CAutoJString(env,targetId),new PublishAckListenerWrap(pObj));
	printf("-----AddToBlacklist end-----");
}
//remove
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_RemoveFromBlacklist
(JNIEnv *env, jobject, jstring targetId, jobject objCallback)
{
	printf("-----RemoveFromBlacklist start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	RemoveFromBlacklist(CAutoJString(env,targetId),new PublishAckListenerWrap(pObj));
	printf("-----RemoveFromBlacklist end-----");
}
//query
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_GetBlacklistStatus
(JNIEnv *env, jobject, jstring targetId, jobject objCallback)
{
	printf("-----GetBlacklistStatus start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	GetBlacklistStatus(CAutoJString(env,targetId),new PublishAckListenerWrap(pObj));
	printf("-----GetBlacklistStatus end-----");
}
//query all


class BlacklistInfoListenerWrap: public BlacklistInfoListener {
public:
	BlacklistInfoListenerWrap(void* pObj) :m_pObj(pObj) {
	}

	void OnSuccess(const char* blockUserIds) {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "OnSuccess",
					"(Ljava/lang/String;)V");
			if (nMethodId != NULL) {
				jstring jstr = env->NewStringUTF(blockUserIds);
				env->CallVoidMethod((jobject) m_pObj, nMethodId, jstr);
				env->DeleteLocalRef(jstr);
			}
			env->DeleteLocalRef(cls);
		}
	}

	void OnError(int status) {
		CJavaEnv oEnv;
		JNIEnv *env = oEnv;
		jclass cls = env->GetObjectClass((jobject) m_pObj);
		if (cls != NULL) {
			jmethodID nMethodId = env->GetMethodID(cls, "OnError", "(I)V");
			if (nMethodId != NULL) {
				env->CallVoidMethod((jobject) m_pObj, nMethodId, (int) status);
			}
			env->DeleteLocalRef(cls);
		}
		env->DeleteGlobalRef((jobject) m_pObj);
		delete this;
	}
protected:
	void* m_pObj;
};


JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_GetBlacklist
(JNIEnv *env, jobject, jobject objCallback)
{
	printf("-----GetBlacklist start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	GetBlacklist(new BlacklistInfoListenerWrap(pObj));
	printf("-----GetBlacklist end-----");
}

//block push functins

//user functions
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_BlockUserPush
(JNIEnv *env, jobject, jstring targetId, jobject objCallback)
{
	printf("-----BlockUserPush start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	BlockPush(CAutoJString(env,targetId),1,new BizAckListenerWrap(pObj));
	printf("-----BlockUserPush end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_UnBlockUserPush
(JNIEnv *env, jobject, jstring targetId, jobject objCallback)
{
	printf("-----UnBlockUserPush start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	UnBlockPush(CAutoJString(env,targetId),1,new BizAckListenerWrap(pObj));
	printf("-----UnBlockUserPush end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_GetBlockPushUserStatus
(JNIEnv *env, jobject, jstring targetId,jboolean fetchRemote, jobject objCallback)
{
	printf("-----GetBlockPushUserStatus start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	GetBlockPushStatus(CAutoJString(env,targetId),1,fetchRemote,new BizAckListenerWrap(pObj));
	printf("-----GetBlockPushUserStatus end-----");
}

//block discussion

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_BlockDiscussionPush
(JNIEnv *env, jobject, jstring targetId, jobject objCallback)
{
	printf("-----BlockDiscussionPush start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	BlockPush(CAutoJString(env,targetId),2,new BizAckListenerWrap(pObj));
	printf("-----BlockDiscussionPush end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_UnBlockDiscussionPush
(JNIEnv *env, jobject, jstring targetId, jobject objCallback)
{
	printf("-----UnBlockDiscussionPush start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	UnBlockPush(CAutoJString(env,targetId),2,new BizAckListenerWrap(pObj));
	printf("-----UnBlockDiscussionPush end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_GetBlockPushDiscussionStatus
(JNIEnv *env, jobject, jstring targetId, jboolean fetchRemote,jobject objCallback)
{
	printf("-----GetBlockPushDiscussionStatus start-----");
	jobject pObj = env->NewGlobalRef(objCallback);
	GetBlockPushStatus(CAutoJString(env,targetId),2,fetchRemote,new BizAckListenerWrap(pObj));
	printf("-----GetBlockPushDiscussionStatus end-----");
}

//群组接口
JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_SyncGroup
(JNIEnv *env, jobject, jobjectArray idArray, jobjectArray nameArray,jobject objCallback)
{
	printf("-----SyncGroup start-----");

	int idCount = env->GetArrayLength(idArray);
	if(idCount == 0)
		return;

	int nameCount = env->GetArrayLength(nameArray);
	if(nameCount == 0)
		return;

	if(idCount != nameCount)
		return;

	TargetEntry groupEntrys[idCount];

    for (int i=0; i< idCount; i++) {
    	jstring idString = (jstring) env->GetObjectArrayElement(idArray, i);
	    const char *groupId = env->GetStringUTFChars(idString, 0);
	    if(groupId) {
	    	strcpy(groupEntrys[i].targetId,groupId);
	        env->ReleaseStringUTFChars(idString,groupId);
	    }
	    else {
	    	memset(&(groupEntrys[i].targetId),0,64);
	    }

	    jstring nameString = (jstring) env->GetObjectArrayElement(nameArray, i);
	    const char *groupName = env->GetStringUTFChars(nameString, 0);
	    if(groupName) {
	    	strcpy(groupEntrys[i].targetName,groupName);
	    	env->ReleaseStringUTFChars(nameString,groupName);
	    }
	    else {
	    	memset(&(groupEntrys[i].targetName),0,128);
	    }
    }

    jboolean b_ret;
    jobject pObj = env->NewGlobalRef(objCallback);
    SyncGroups(groupEntrys,idCount,new PublishAckListenerWrap(pObj));
    printf("-----SyncGroup end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_QuitGroup
(JNIEnv *env, jobject, jstring groupId, jstring groupName,jobject objCallback)
{
	printf("-----QuitGroup start-----");
	TargetEntry groupEntrys[1];
	strcpy(groupEntrys[0].targetId,CAutoJString(env,groupId));
	strcpy(groupEntrys[0].targetName,CAutoJString(env,groupName));

	jboolean b_ret;
	jobject pObj = env->NewGlobalRef(objCallback);
	QuitGroup(groupEntrys,1,new PublishAckListenerWrap(pObj));
	printf("-----QuitGroup end-----");
}

JNIEXPORT void JNICALL Java_io_rong_imlib_NativeObject_JoinGroup
(JNIEnv *env, jobject, jstring groupId, jstring groupName,jobject objCallback)
{
	printf("-----JoinGroup start-----");
	TargetEntry groupEntrys[1];
	strcpy(groupEntrys[0].targetId,CAutoJString(env,groupId));
	strcpy(groupEntrys[0].targetName,CAutoJString(env,groupName));

	jboolean b_ret;
	jobject pObj = env->NewGlobalRef(objCallback);
	JoinGroup(groupEntrys,1,new PublishAckListenerWrap(pObj));
	printf("-----JoinGroup end-----");
}


#ifdef __cplusplus
}
#endif
