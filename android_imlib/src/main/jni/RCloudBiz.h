//
//  RCloudBiz.h
//  rcsdk
//
//  Created by FCloud on 14-5-7.
//  Copyright (c) 2014年 FCloud. All rights reserved.
//

#ifndef rcsdk_Biz_h
#define rcsdk_Biz_h
#include "BizListener.h"

#include <string>
using namespace std;

class ICallback;
#ifdef __cplusplus
extern "C" {
#endif

    ///
    ///初始化全局实例，appId
    ///
    void* InitClient(const char* appName,const char* localPath,const char* deviceId,const char* appid); //ios
    void* InitClientEx(const char* appName,const char* localPath,const char* databasePath,const char* deviceId,const char* appid); //android
    ///
    ///建立服务器连接，localPath:数据库和配置文件的目录，后面不要带/,token,listener
    ///
    void connectTo(const char* token,ConnectAckListener* listener);
    ///
    ///断开服务器连接，这里会删除全局实例
    ///
    void disconnect();
    ///
    ///发送二人消息，transferType:传输类别【s，n，p】 targetId:对方Id，clazzname:对象名称 msg:json内容 dbId:消息在数据库中的Id
    ///
    void SendSingleMessage(const int transferType,const char* targetId,  const char* clazzname,const char* msg, long dbId,PublishAckListener* listener);
    ///
    ///发送讨论组消息，transferType:传输类别【s，n，p】groupId:讨论组Id，clazzname:对象名称 msg:json内容 dbId:消息在数据库中的Id
    ///
    void SendDiscussionMessage(const int transferType,const char* groupId,  const char* clazzname,const char* msg,long dbId,PublishAckListener* listener);
    
    ///
    ///发送群组消息，transferType:传输类别【s，n，p】groupId:群组Id，clazzname:对象名称 msg:json内容 dbId:消息在数据库中的Id
    ///
    void SendGroupMessage(const int transferType,const char* groupId,  const char* clazzname,const char* msg,long dbId,PublishAckListener* listener);
    
    ///
    ///发送客服消息，transferType:传输类别【s，n，p】groupId:讨论组Id，clazzname:对象名称 msg:json内容 dbId:消息在数据库中的Id
    ///
    void SendReceptionMessage(const int transferType,const char* targetId,  const char* clazzname,const char* msg,long dbId,PublishAckListener* listener);

    
    ///
    ///生成文件KEY nType:媒体类型1图片 2Audio 3Video
    ///
    bool GenerateKey(int nType,char *mimeKey);
    ///
    ///发送文件缓存文件 nType:媒体类型1图片 2Audio 3Video
    ///
    bool SaveFileToCache(const char* targetId,int categoryId,int nType,const char* pszKey, const unsigned char* pbData, unsigned long nl,char* fileName);
    
    ///
    ///上传文件 nType:媒体类型1图片 2Audio 3Video
    ///
    void SendFile(const char* targetId,int categoryId,int nType,const char* pszKey,const unsigned char* pbData, long nl, SendFileListener* pListener);
    ///
    ///下载文件，nType:媒体类型1图片 2Audio 3Video
    ///
    void DownFile(const char* targetId,int categoryId,int nType, const char* pszKey, DownFileListener* pListener);

    void SendFileWithUrl(const char* targetId,int categoryId,int nType,const unsigned char* pbData, long nl, SendFileListener* pListener);
    void DownFileWithUrl(const char* targetId,int categoryId,int nType,const char* pszUrl, DownFileListener* pListener);
    ///
    ///讨论组操作
    ///
    ///
    ///创建讨论组 discussionName:讨论组名称
    ///
    void CreateDiscussion(const char* discussionName, CreateMultiTalkListener* listener);
    ///
    ///退出讨论组 targetId:讨论组id
    ///
    void QuitDiscussion(const char* groupId, PublishAckListener* listener);
    ///
    ///邀请加入讨论组 targetId:讨论组id userIds用\n分割，表示邀请的用户id列表
    ///
    void InviteMemberToDiscussion(const char* groupId, const TargetEntry userIds[],int idCount, PublishAckListener* listener);
    ///
    ///将某人踢出讨论组 targetId:讨论组id userId，表示踢出的用户id
    ///
    void RemoveMemberFromDiscussion(const char* userId,const char* groupId, PublishAckListener* listener);
    ///
    ///获取讨论组信息 targetId:讨论组id
    ///
    void GetDiscussionInfo(const char* groupId,int categoryId,bool fetchRemote, DiscussionInfoListener* listener);
    ///
    ///获取用户所有讨论组信息 startPage:起始页 countPerPage: 每页记录数
    ///
    void SelfDiscussions(int startPage, int countPerPage, SelfDiscussionsListener* listener);
    ///讨论组改名 discussionName:讨论组名称
    ///
    void RenameDiscussion(const char* targetId,const char* discussionName, PublishAckListener* listener);
    ///讨论组权限更改 discussionName:讨论组名称
    ///
    void SetInviteStatus(const char* targetId,int inviteStatus, PublishAckListener* listener);
    
    
//  设置消息监听
    void SetMessageListener(MessageListener* listener);
//  设置异常监听
    void SetExceptionListener(ExceptionListener* listener);
//  注册一种数据，决定是否入库
    bool RegisterMessageType(const char* clazzName,const unsigned int operateBits);
    /**
     *  接收应用的环境改变事件通知
     *
     *  @param nType     事件类型，101-网络切换，102-应用进入后台，103-应用进入前台，104-锁屏，105-心跳，106-屏幕解锁
     *  @param pbData    依据nType的事件附加数据，待定
     *  @param nDataSize 数据大小，字节数
     *  @param pListener 事件改变的回调
     */
    void EnvironmentChangeNotify(int nType, unsigned char* pbData, int nDataSize, EnvironmentChangeNotifyListener* pListener);
    /**
     *  android设置唤醒的监听器
     *
     *  @param pListener 唤醒监听器
     */
    void SetWakeupQueryListener(WakeupQueryListener* pListener);
    
    

    ///
    ///category 参数 1 p2p 2 讨论组 3 群
    ///
    long SaveMessage(const char* content,const char* targetId,const char* clazzName,const char* senderId,int categoryId);
    ///
    ///设置草稿
    ///
    bool SetTextMessageDraft(const char* targetId,const int categoryId,const char* draftMessage);
    ///
    ///设置消息内容
    ///
    bool SetMessageContent(long messageId,const char* content);
    ///
    ///设置附加信息
    ///
    bool SetTextMessageExtra(const long messageId,const char* extraMessage);
    ///
    ///设置置顶
    ///
    bool SetIsTop(const char* targetId,const int categoryId,bool bTop);
    ///
    ///设置消息读取状态
    ///
    bool SetReadStatus(long messageId,int readStatus);
    
    bool SaveUserInfo(const char* userId,const char* userName,const char* portraitUrl,long long updateTime,const char* remarkName,int categoryId);

    
    ///
    ///获取最近的会话列表result需要在外面delete
    ///{"result":[{"target_id":"1001","last_message_id":254,"conversation_title":"","unread_count":28,"conversation_category":1,"is_top":0,"content":"1content4","message_direction":1,"read_status":0,"receive_time":1401092236,"send_time":1401092236,"object_name":"TextMessage","send_status":0,"sender_user_id":"1002"}]}
    ///
    bool GetConversationList(const ConversationEntry conversationDict[],int conversationCount,char** result);
    ///
    ///获取分页消息，result需要在外面delete
    ///{"result":[{"id":252,"content":"中文2","target_id":"1003","message_direction":true,"read_status":0,"receive_time":1401092236,"send_time":1401092236,"object_name":"TextMessage","send_status":0,"sender_user_id":"1002","conversation_category":1}]}
    ///
    bool GetPagedMessage(const char* targetId,long beginId,int count,int categoryId,char** result);
    ///
    ///获取最近消息，result需要在外面delete
    ///
    bool GetLatestMessage(const char* targetId,int count,int categoryId,char** result);
    ///
    ///获取总的未读消息数
    ///
    int GetTotalUnreadCount();
    
    ///
    ///获取用户、讨论组、群组的总的未读消息数
    ///
    ///targetId 用户或讨论组的id
    ///categoryId 类别 1-用户 2-讨论组 3-群组
    ///
    int GetUnreadCount(const char* targetId,int categoryId);
    
    ///
    ///获取草稿
    ///
    bool GetTextMessageDraft(const char* targetId,const int categoryId,char** draftMessage);
    ///
    ///获取用户信息。userId：用户id或者targetId.这里会先查数据库，数据库有信息直接返回，没有则查服务器
    ///
    void GetUserInfo(const char* userId,UserInfoOutputListener* listener,bool fetchRemote=false);
    ///
    ///获取最近的会话列表result需要在外面delete,单条
    ///{"result":[{"target_id":"1001","last_message_id":254,"conversation_title":"","unread_count":28,"conversation_category":1,"is_top":0,"content":"1content4","message_direction":1,"read_status":0,"receive_time":1401092236,"send_time":1401092236,"object_name":"TextMessage","send_status":0,"sender_user_id":"1002"}]}
    ///
    bool GetConversation(const char* targetId,int categoryId,char** result);
    
    ///
    ///删除消息，id用逗号分割，如1,2,3,会拼到sql的where in中
    ///
//    bool DeleteMessage(const char* idList);
    bool DeleteMessage(const MessageEntry messageDict[],int messageCount);
    ///
    ///删除消息，targetId用户或者讨论组id，categoryId 类别id 1 p2p 2 讨论组 3 群
    ///
    bool ClearMessages(const char* targetId,const int categoryId);
    ///
    ///删除某个会话
    ///
    bool RemoveConversation(const char* targetId,const int categoryId);
    ///
    ///清除某人的未读状态
    ///
    bool ClearUnread(const char* targetId,const int categoryId);
    ///
    ///以16进制打印数据
    ///
    void BizHexDump(const char* pData,int nLength);
    
    //黑名单系列函数
    void AddToBlacklist(const char* targetId, PublishAckListener* listener);
    void RemoveFromBlacklist(const char* targetId, PublishAckListener* listener);
    void GetBlacklistStatus(const char* targetId, PublishAckListener* listener);
    void GetBlacklist(BlacklistInfoListener* listener);
    //阻止用户push信息函数 阻止状态 0-未阻止 100-已阻止
    
    void BlockPush(const char* targetId, int categoryId,BizAckListener* listener);
    void UnBlockPush(const char* targetId, int categoryId,BizAckListener* listener);
    void GetBlockPushStatus(const char* targetId, int categoryId, bool fetchRemote,BizAckListener* listener);
    void SetBlockMessageOptions(const char* targetId,int categoryId,BizAckStatus status);
    
    void SetInfoListener(UserInfoOutputListener* listener);
    bool GetPagedMessageEx(const char* targetId,long beginId,int count,int categoryId,Message* result,int *fetchCount);
    ///
    ///设置是否接收消息
    ///
    bool BlockMessage(const char* targetId,const int categoryId,const bool bBlock);
    //同步群组，将订阅群组发送到服务器
    void SyncGroups(TargetEntry groupIds[],int idCount,PublishAckListener* listener);
    void JoinGroup(TargetEntry groupIds[],int idCount,PublishAckListener* listener);
    void QuitGroup(TargetEntry groupIds[],int idCount,PublishAckListener* listener);
    
    
#ifdef __cplusplus
}
#endif

#endif
