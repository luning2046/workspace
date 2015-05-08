//
//  BizListener.h
//  rcsdk
//
//  Created by FCloud on 14-5-8.
//  Copyright (c) 2014年 FCloud. All rights reserved.
//

#ifndef rcsdk_BizListener_h
#define rcsdk_BizListener_h
enum ConnectAckStatus {
    ACCEPTED = 0,
    //网络层定义
    CONNECT_SUCCESS = 2000,
    CONNECT_PROTO_VERSION_ERROR,
    IDENTIFIER_REJECTED,
    SERVER_UNAVAILABLE,
    BAD_USERNAME_OR_PASSWORD,
    NOT_AUTHORIZED,
    CONNECT_REDIRECT,
    CONNECT_APPNAME_MISMATCH,
    
    //RC层定义
    CLIENT_NOT_INIT = 10000,
    DATABASE_ERROR //数据库操作失败 1.目录无权限 2.创建目录失败 3.打开数据库失败 4.初始化数据库表失败

};

enum DisConnectAckStatus {
    //RC层定义
    DIS_CLIENT_NOT_INIT = 10000
};

// 通知sdk的异常码在此定义
//typedef enum
//{
//    success = 0,
//    
//    neterr_channel_invalid = 100,
//    neterr_connect_fail    = 101,
//    neterr_send_fail       = 102,
//    
//    ack_timeout     = 900,
//    send_fail       = 901,
//    connect_timeout = 902,
//    queryack_nodata = 903,
//    remote_close    = 904,
//    
//    neterr_disconnect_base    = 1000,
//    neterr_disconnect_kick    = 1001,
//    neterr_disconnect_unknown = 1002,
//    
//    connect_success               = 2000,
//    connect_proto_version_error,
//    connect_id_reject,
//    connect_server_unavaliable,
//    connect_user_or_pwd_error,
//    connect_not_authorized,
//    connect_redirect,
//    connect_appname_mismatch,
//    
//    net_unavaliable = 3001,
//    navfail,
//    
//    data_incomplete = 4001,
//} error_type;


class ConnectAckListener
{
public:
    virtual ~ConnectAckListener(){}
    virtual void operationComplete(ConnectAckStatus status,const char* userId)=0;
};

class DisConnectAckListener
{
public:
    virtual ~DisConnectAckListener(){}
    virtual void operationComplete(DisConnectAckStatus status)=0;
};


enum PublishAckStatus
{
    SUCCESS = 0,
    TIMEOUT=3001
};

class PublishAckListener
{
public:
    virtual ~PublishAckListener(){}
    virtual void operationComplete(PublishAckStatus status)=0;
};

//=====
enum OperationAckStatus
{
    OP_SUCCESS = 0,
    OP_PARAMETER_ERROR,
    OP_TIMEOUT=3001
};

enum BizAckStatus
{
    BIZ_DEFAULT = 0,
    BIZ_BLOCK_MESSAGE_NOTIFY = 100, //新消息阻止枚举
};

class BizAckListener
{
public:
    virtual ~BizAckListener(){}
    virtual void operationComplete(OperationAckStatus opStatus,BizAckStatus bizStatus)=0;
    
};

//=====

class CreateMultiTalkListener
{
public:
    virtual ~CreateMultiTalkListener(){}
    virtual void OnSuccess(const char* multiTalkId)=0;
	virtual void OnError(int status)=0;
};

class MultiTalkMessageListener
{
public:
    virtual ~MultiTalkMessageListener(){}
    virtual void process(const char* className,const char* fromUserId, const char* fromGroupId, long time, const char* message,long nl,long id)=0;
};

class GroupMessageListener
{
public:
    virtual ~GroupMessageListener(){}
    virtual void process(const char* className,const char* fromUserId, const char* fromGroupId, long time, const char* message,long nl,long id)=0;
};

class PersonMessageListener
{
public:
    virtual ~PersonMessageListener(){}
    virtual void process(const char* className,const char* fromUserId, long time, const char* message,long nl,long id)=0;
};

class RequestSessionIdListener
{
public:
    virtual ~RequestSessionIdListener(){}
    virtual void OnResponse(int sessionId)=0;
    virtual void OnError(int status)=0;
};

class UserInfoOutputListener
{
public:
    virtual ~UserInfoOutputListener(){}
    virtual void OnResponse(const char* userId,const char* userName,const char* userPortrait)=0;
    virtual void OnError(int status)=0;
};

class UserInfo
{
public:
    UserInfo();
    ~UserInfo();
    char    m_Id[128];
    char    m_Name[128];
    char    m_Portrait[1024];
    char    m_Remark[64];
    int     m_Category;
    int     m_BlockMessageNotify;
    long long   m_UpdateTime;
};


class MultiUsersListener
{
public:
    virtual ~MultiUsersListener(){}
    virtual void OnReceive(UserInfo *,int)=0;
    virtual void OnError(int status)=0;
};


class SendFileListener
{
public:
    virtual ~SendFileListener(){}
    virtual void OnProgress(int nProgress) = 0;
    virtual void OnError(int nErrorCode, const char* pszDescription) = 0;
};

class DownFileListener : public SendFileListener
{
public:
    virtual ~DownFileListener(){}
    virtual void OnData(const unsigned char* pbData, long nl) = 0;
};

class SendImageListener
{
public:
    virtual ~SendImageListener(){}
    virtual void OnProgress(int nProgress) = 0;
    virtual void OnError(int nErrorCode) = 0;
    virtual void OnUploadSuccess() = 0;
    virtual void OnSendSuccess() = 0;
};

class DiscussionInfo
{
public:
    DiscussionInfo();
    ~DiscussionInfo();
    char* m_Id;
    char* m_Name;
    int m_Category;
    char* m_AdminId;
    char* m_UserIds;
    int   m_InviteStatus;
    long long m_StoreTime;
    int     m_BlockMessageNotify;
    
};

class DiscussionInfoListener
{
public:
    virtual ~DiscussionInfoListener(){}
    virtual void OnReceive(DiscussionInfo *p)=0;
	virtual void OnError(int status)=0;
};

class SelfDiscussionsListener
{
public:
    virtual ~SelfDiscussionsListener(){}
    virtual void OnReceive(DiscussionInfo *arr,int n)=0;
	virtual void OnError(int status)=0;
};

class ExceptionListener
{
public:
    virtual ~ExceptionListener(){}
	virtual void OnError(int status,const char* description)=0;
};


//消息接口
class Message
{
public:
    Message();
    ~Message();
    int conversationType;
    char* conversationId;
    long messageId;
    bool messageDirection;
    char* senderUserId;
    int readStatus;
    int sentStatus;
    long long receivedTime;
    long long sentTime;
    char* objectName;
    char* content;
    char* extraMessage;
};

class MessageListener
{
public:
    virtual ~MessageListener(){}
    virtual void OnReceive(Message* p) = 0;
};


class ConversationMessage
{
public:
    ConversationMessage();
    ~ConversationMessage();
    int conversationType;
    char* conversationId;
    long messageId;
    bool messageDirection;
    char* senderUserId;
    int readStatus;
    int sentStatus;
    long long receivedTime;
    long long sentTime;
    char* objectName;
    char* content;
    char* extraMessage;
};


class EnvironmentChangeNotifyListener
{
public:
    virtual ~EnvironmentChangeNotifyListener(){}
    /**
     *  环境改变，底层处理后的回调
     *
     *  @param nType 类型
     *  @param pData 附带数据
     */
    virtual void Complete(int nType, char* pData) = 0;
};

class WakeupQueryListener
{
public:
    virtual ~WakeupQueryListener(){}
    /**
     *  请求唤醒
     *
     *  @param nType 请求类型
     */
    virtual void QueryWakeup(int nType) = 0;
    /**
     *  释放唤醒锁
     */
    virtual void ReleaseWakup() = 0;
};

//黑名单用户列表 多个id以回车分割
class BlacklistInfoListener
{
public:
    virtual ~BlacklistInfoListener(){}
    virtual void OnSuccess(const char* blockUserIds)=0;
	virtual void OnError(int status)=0;
};


//使用\n 或者 逗号分割的字串统一修改
//消息id条目
struct MessageEntry
{
    long messageId;
};
//用户讨论组群组id条目
struct TargetEntry
{
    char targetId[64];
    char targetName[256];
    TargetEntry();
    bool operator < (const TargetEntry &c)const;
};

//用户讨论组群组id条目
struct ConversationEntry
{
    int typeId;
};


#endif
