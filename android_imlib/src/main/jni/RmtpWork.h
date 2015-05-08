//
//  RmtpWork.h
//  Sockets
//
//  Created by test on 14-3-31.
//  Copyright (c) 2014年 HYJ. All rights reserved.
//
//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖保佑                  永无BUG
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？

#ifndef __Sockets__RmtpWork__
#define __Sockets__RmtpWork__

#ifdef __cplusplus
extern "C" {
#endif
    
    //========异常码定义开始============================================
    // 通知sdk的异常码在此定义
    typedef enum
    {
        success = 0,
        
        neterr_channel_invalid = 100,
        neterr_connect_fail    = 101,
        neterr_send_fail       = 102,
        
        ack_timeout     = 900,
        send_fail       = 901,
        connect_timeout = 902,
        queryack_nodata = 903,
        remote_close    = 904,
        
        neterr_disconnect_base    = 1000,
        neterr_disconnect_kick    = 1001,
        neterr_disconnect_unknown = 1002,
        
        connect_success               = 2000,
        connect_proto_version_error,
        connect_id_reject,
        connect_server_unavaliable,
        connect_user_or_pwd_error,
        connect_not_authorized,
        connect_redirect,
        connect_appname_mismatch,
        
        net_unavaliable = 3001,
        nav_sc_error,
        nav_node_not_found,
        
        data_incomplete = 4001,
    } error_type;
    //========异常码定义结束============================================
    

    class ICallback
    {
    public:
        virtual ~ICallback(){}
        virtual bool Callme(unsigned char* pbData, unsigned long nl) = 0;
        virtual void Error(error_type eType, const char* pszDescription) = 0;
    };
    typedef void(*MQTTNOTIFY)(void* pChannel, const char* pszMethod, const char* pszTargetId, int nQos, bool bRetain, const unsigned char* pbData, unsigned long nl, ICallback*);
    typedef void(*MQTTEXCEPTION)(int nCode, const char* pszDescription);
    
    bool  SetExceptionFunction(MQTTEXCEPTION pfnExceptionProc);
    bool  SetNotifyFunction(MQTTNOTIFY pfnNotify);
    void* CreateAChannel(const char* pszAppName, const char* pszLocalPath, const char* pszClientId, const char* pszAppId, const char* pszToken, ICallback*);
    void  DestroyChannel(void* pChannel);
    void  SendPublish(void* pChannel, const char* pszMethod, const char* pszTargetId, int nQos, bool bRetain, const unsigned char* pbPayload, unsigned long nl, ICallback*);
    void  SendQuery(void* pChannel, const char* pszMethod, const char* pszTargetId, int nQos, bool bRetain, const unsigned char* pbPayload, unsigned long nl, ICallback*);
    
    void SendAppMessage(const char* pszMethod, const char* pszTarget, const char* pszClassname, const unsigned char* pbPayload, long cbSize, ICallback* pCallback);
    
    class IUpdownCallback
    {
    public:
        virtual ~IUpdownCallback(){}
        virtual void Progress(int nPer) = 0;
        virtual void Error(error_type, const char* pszDesc) = 0;
        virtual void Data(long nLen, const unsigned char* pszData) = 0;
    };
    void UploadFile(const char* pszUploadToken, const char* pszFilename, const unsigned char* pbData, long lSize, const char* pszMime, IUpdownCallback*);
    void DownloadFile(const char* pszUrl, IUpdownCallback* pCallback);
    
    //101-网络切换，102-应用进入后台，103-应用进入前台，104-锁屏，105-心跳，106-屏幕解锁
    typedef enum {
        env_none = 0,
        env_switchnet      = 101, //附加数据pbData含义： 0-网络不可用，1-网络可用，w22G-wifi to 2G,w23G-wifi to 3G, w24G-wifi to 4G,
                                  //                   2G2w-2G to wifi, 3G2w-3G to wifi, 4G2w-4G to wifi
        env_background     = 102,
        env_foreground     = 103,
        env_screenlock     = 104,
        env_heartbeat      = 105,
        env_screenunlock   = 106,
        env_background_delay_timeout = 107, // ios后台申请延时时间到
    } env_event;
    void NotifyEnvironmentChange(void* pChannel, env_event nType, unsigned char* pbData, int nDataSize, ICallback* pCallback);
    void SetWakeupQueryCallback(void* pChannel, ICallback* pCallback);
    
#ifdef __cplusplus
}
#endif

#endif /* defined(__Sockets__RmtpWork__) */
