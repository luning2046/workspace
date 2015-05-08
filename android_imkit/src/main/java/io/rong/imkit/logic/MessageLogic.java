package io.rong.imkit.logic;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.model.UIDiscussion;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.service.RCloudService;
import io.rong.imkit.utils.HTMLDecoder;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ConnectionStatusListener;
import io.rong.imlib.RongIMClient.ConversationType;
import io.rong.imlib.RongIMClient.CreateDiscussionCallback;
import io.rong.imlib.RongIMClient.Discussion;
import io.rong.imlib.RongIMClient.DownloadMediaCallback;
import io.rong.imlib.RongIMClient.GetDiscussionCallback;
import io.rong.imlib.RongIMClient.MediaType;
import io.rong.imlib.RongIMClient.Message;
import io.rong.imlib.RongIMClient.OnReceiveMessageListener;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.RongIMClient.SendMessageCallback;
import io.rong.imlib.RongIMClient.TextMessage;
import io.rong.voipkit.activity.CalledSideActivity;
import io.rong.voipkit.message.VoipAcceptMessage;
import io.rong.voipkit.message.VoipCallMessage;
import io.rong.voipkit.message.VoipFinishMessage;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class MessageLogic extends BaseLogic implements Handler.Callback {
    private static final String TAG = "MessageLogic";

    public static final String ACTION_P2P_MESSAGE_SEND = "io.rong.imkit.Message.logic.p2p_send";
    public static final String ACTION_P2P_MESSAGE_RECEVICE = "io.rong.imkit.Message.logic.p2p_recevice";
    public static final String ACTION_GROUP_MESSAGE_RECEVICE = "io.rong.imkit.Message.logic.recevice";
    public static final String ACTION_DISCUSSION_MESSAGE_SEND = "io.rong.imkit.Message.logic.discussion_send";
    public static final String ACTION_DISCUSSION_MESSAGE_RECEVICE = "io.rong.imkit.Message.logic.discussion_recevice";
    public static final String ACTION_DISCUSSION_CREATE = "io.rong.imkit.Message.logic.discussion.create";
    public static final String ACTION_DISCUSSION_REMOVE_MEMBER = "io.rong.imkit.Message.logic.discussion.remove.member";
    public static final String ACTION_DISCUSSION_INFO_GET = "io.rong.imkit.Message.logic.discussion.info.get";
    public static final String ACTION_DISCUSSION_QUIT = "io.rong.imkit.Message.logic.discussion.quit";
    public static final String ACTION_DISCUSSION_INVITE_MEMBER = "io.rong.imkit.Message.logic.discussion.invite_member";
    public static final String ACTION_DISCUSSION_UPDATE_NAME = "io.rong.imkit.Message.logic.discussion.update_name";

    public static final String ACTION_MESSAGE_IMAGE_UPLOAD = "io.rong.imkit.Message.logic.message.image.upload";
    public static final String ACTION_MESSAGE_IMAGE_DOWNLOAD = "io.rong.imkit.Message.logic.message.image.download";
    public static final String ACTION_CHATROOM_MESSAGE_RECEVICE = "io.rong.imkit.Message.logic.message.chatroom.recevice";


    public static final String TARGET_ID = "target_id";

    public static final String DISCUSSION_NAME = "multi_talk_name";
    public static final String DISCUSSION_MEMBER_ID = "multi_talk_id_array";
    public static final String DISCUSSION_ID = "multi_talk_id";
    public static final String DISCUSSION_IS_OPEN_INVITE_STATUS = "discussion_is_open_invite_status";
    public static final String DISCUSSION_OBJECT = "discussion_object";
    // 统一命名
    public static final String INTENT_API_OPERATION_STATUS = "intent_api_operation_status";
    public static final String INTENT_STATUE_VALUE = "intent_statue_value";

    public static final String SEND_MESSAGE_STATE = "send_message_state";
    public static final String MESSAGE_ID_TEMP_SEND_MESSAGE_RETURN = "message_id_temp_send_message_return";
    public static final String MESSAGE_OBJ_SEND_MESSAGE_RETURN = "message_OBJ_send_message_return";

    public static final String INTENT_MESSAGE_FILE_DOWN_TYPE = "intent_message_file_download_type";
    public static final String INTENT_MESSAGE_FILE_DOWN_RES_KEY = "intent_message_file_download_res_key";
    public static final String INTENT_MESSAGE_FILE_DOWN_RES_PATH = "intent_message_file_download_res_path";
    public static final String INTENT_MESSAGE_FILE_DOWN_PROGRESS = "intent_message_file_download_progress";

    public static final String INTENT_CONVERSATION_TYPE = "intent_conversation_type";
    public static final String INTENT_NEW_MESSAGE_BLOCK = "intent_new_message_block";
    public static final String INTENT_IS_COMPLETE = "intent_is_complete";

    public static final String INTENT_USER_ID = "intnet_user_id";

    public static final String ACTION_RECEVICE_NETWORD_CONNECT_STATE = "io.rong.imkit.Message.logic.recevice_network_connect_state";
    public static final String INTENT_NETWORK_CONNECT_STATE = "intent_network_connect_state";


    public static final String ACTION_SET_DISCUSSION_INVITE_STATUS = "action_set_discussion_invite_status";
    public static final String ACTION_SET_BLOCK_PUSH_STATUS = "action_set_block_push_status";
    public static final String ACTION_GET_BLOCK_PUSH_STATUS = "action_get_block_push_status";


    private RCloudService fCloudService;
    private Handler mHandle;

    public MessageLogic(RCloudService fCloudService) {
        super(fCloudService);

        this.fCloudService = fCloudService;

        List<String> actions = new ArrayList<String>();
        actions.add(ACTION_P2P_MESSAGE_SEND);
        actions.add(ACTION_P2P_MESSAGE_RECEVICE);
        actions.add(ACTION_MESSAGE_IMAGE_UPLOAD);

        actions.add(ACTION_DISCUSSION_CREATE);
        actions.add(ACTION_DISCUSSION_MESSAGE_SEND);
        actions.add(ACTION_DISCUSSION_QUIT);
        actions.add(ACTION_MESSAGE_IMAGE_DOWNLOAD);
        actions.add(ACTION_DISCUSSION_INFO_GET);
        actions.add(ACTION_DISCUSSION_REMOVE_MEMBER);
        actions.add(ACTION_DISCUSSION_INVITE_MEMBER);
        actions.add(ACTION_DISCUSSION_UPDATE_NAME);
        actions.add(ACTION_SET_DISCUSSION_INVITE_STATUS);
        actions.add(ACTION_SET_BLOCK_PUSH_STATUS);
        actions.add(ACTION_GET_BLOCK_PUSH_STATUS);

        fCloudService.registerAction(this, actions);

        registerReceviceMessage();

        receviceConnectStateChange();

        Log.d(TAG, "MessageLogic----MessageLogic---");

        mHandle = new Handler(this);
    }
//===================================
    @Override
    public void onHandleAction(final Intent intent) {

        if (intent == null)
            return;

        final String action = intent.getAction();

        Log.d(TAG, "onHandleAction----->:" + action);

        if (ACTION_P2P_MESSAGE_SEND.equals(action)) {// 发P2P消息
            sendMessage(intent);
        } else if (ACTION_MESSAGE_IMAGE_DOWNLOAD.equals(action)) {// 下载图片
            downloadFile(intent);
        } else if (ACTION_DISCUSSION_CREATE.equals(action)) {// groupId 创建多人会话
            createDiscussion(intent);
        } else if (ACTION_DISCUSSION_INFO_GET.equals(action)) {
            getDiscussion(intent);
        } else if (ACTION_DISCUSSION_REMOVE_MEMBER.equals(action)) {
            removeMemberFromDiscussion(intent);
        } else if (ACTION_DISCUSSION_QUIT.equals(action)) {
            quitDiscussion(intent);
        } else if (ACTION_DISCUSSION_INVITE_MEMBER.equals(action)) {
            inviteMemberToDiscussion(intent);
        } else if (ACTION_DISCUSSION_UPDATE_NAME.equals(action)) {
            updateDiscussionName(intent);
        } else if (ACTION_SET_DISCUSSION_INVITE_STATUS.equals(action)) {
            setDiscussionInviteStatus(intent);
        } else if (ACTION_SET_BLOCK_PUSH_STATUS.equals(action)) {
            setBlockPushStatus(intent);
        } else if (ACTION_GET_BLOCK_PUSH_STATUS.equals(action)) {
            getBlockPushStatus(intent);
        }

    }

//===================================================发送消息接口==============================================================================================
    private final void sendMessage(final Intent intent) {

        final String action = intent.getAction();

        Log.d("MessageLogic", action);

        final UIMessage msg = intent.getParcelableExtra(UIMessage.MESSAGE_OBJ);

        final long tempMessageId = msg.getMessageId();

        Message message = null;

        try {

            if (RCloudContext.getInstance() != null && RCloudContext.getInstance().getRongIMClient() != null) {

                message = RCloudContext.getInstance().getRongIMClient().sendMessage(msg.getConversationType(), msg.getTargetId(), msg.getContent(), new SendMessageCallback() {

                    @Override
                    public void onSuccess() {
                        Log.d("MessageLogic---onHandleAction--sendMessage", "onSuccess:---two--two--two--two" + action);

                        Intent intentArg = intent;
                        intentArg.putExtra(SEND_MESSAGE_STATE, true);
                        intentArg.putExtra(INTENT_IS_COMPLETE, true);
                        intentArg.putExtra(MESSAGE_ID_TEMP_SEND_MESSAGE_RETURN, tempMessageId);
                        intentArg.putExtra(INTENT_MESSAGE_FILE_DOWN_PROGRESS, -1);
                        fCloudService.sendBroadcast(intentArg);
                    }

                    @Override
                    public void onProgress(int progress) {
                        Log.d("MessageLogic---onHandleAction--sendMessage", "onProgress:" + progress + "%");
                        Intent intentArg = intent;
                        intentArg.putExtra(MESSAGE_ID_TEMP_SEND_MESSAGE_RETURN, tempMessageId);
                        intentArg.putExtra(INTENT_MESSAGE_FILE_DOWN_PROGRESS, progress);
                        intentArg.putExtra(INTENT_IS_COMPLETE, false);
                        fCloudService.sendBroadcast(intentArg);
                    }

                    @Override
                    public void onError(final ErrorCode errorCode) {

                        mHandle.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("MessageLogic---onHandleAction--sendMessage", "onError:---two--two--two--two" + errorCode.getValue());
                                Intent intentArg = intent;
                                intentArg.putExtra(SEND_MESSAGE_STATE, false);
                                intentArg.putExtra(INTENT_IS_COMPLETE, true);
                                intentArg.putExtra(MESSAGE_ID_TEMP_SEND_MESSAGE_RETURN, tempMessageId);
                                fCloudService.sendBroadcast(intent);
                            }
                        }, 500);

                    }

                });

                Log.d("MessageLogic---onHandleAction--sendMessage", "one one one one---" + action);
                message.setExtra(msg.getExtra());
                RCloudContext.getInstance().getRongIMClient().setMessageExtra(message.getMessageId(), message.getExtra());
                UIMessage uimessage = new UIMessage(message);
//				uimessage.setMessageId(tempMessageId);

                intent.putExtra(MESSAGE_OBJ_SEND_MESSAGE_RETURN, uimessage);
                intent.putExtra(MESSAGE_ID_TEMP_SEND_MESSAGE_RETURN, tempMessageId);
                intent.putExtra(INTENT_IS_COMPLETE, false);
                fCloudService.sendBroadcast(intent);
            }

        } catch (Exception e) {
            Log.d("MessageLogic---onHandleAction--sendMessage", "message send is fail");
            e.printStackTrace();

            Intent intentError = new Intent(action);
            intentError.putExtra(SEND_MESSAGE_STATE, false);
            intent.putExtra(INTENT_IS_COMPLETE, false);
            intentError.putExtra(MESSAGE_ID_TEMP_SEND_MESSAGE_RETURN, msg.getMessageId());

            fCloudService.sendBroadcast(intentError);
        }
    }

//==================================================获得用户信息   登录时已经获取到所有用户的信息=========================================================================
    public static void getUserInfo(String userId, final GetUserInfoCallback getUserInfoCallback) {

        if (RCloudContext.getInstance().getGetUserInfoProvider() != null) {

            RongIMClient.UserInfo userInfo = RCloudContext.getInstance().getGetUserInfoProvider().getUserInfo(userId);

            if (userInfo != null) {
                if (getUserInfoCallback != null) {
                    getUserInfoCallback.onSuccess(userInfo);
                }
            } else {
                RCloudContext.getInstance().getRongIMClient().getUserInfo(userId, new RongIMClient.GetUserInfoCallback() {
                    @Override
                    public void onSuccess(RongIMClient.UserInfo user) {
                        getUserInfoCallback.onSuccess(user);
                    }

                    @Override
                    public void onError(ErrorCode errorCode) {
                        getUserInfoCallback.onError();
                    }
                });
            }
        } else {
            RCloudContext.getInstance().getRongIMClient().getUserInfo(userId, new RongIMClient.GetUserInfoCallback() {
                @Override
                public void onSuccess(RongIMClient.UserInfo user) {
                    getUserInfoCallback.onSuccess(user);
                }

                @Override
                public void onError(ErrorCode errorCode) {
                    getUserInfoCallback.onError();
                }
            });
        }
    }

    public static interface GetUserInfoCallback {
        public void onSuccess(RongIMClient.UserInfo user);

        public void onError();
    }

    private String peerUserPhoteUri = "";

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

            VoipCallMessage voipMessage = (VoipCallMessage) msg.obj;
            Intent videoChatInt = new Intent(fCloudService, CalledSideActivity.class);
            videoChatInt.putExtra("VoipCallMessage", voipMessage);
            videoChatInt.putExtra("appId", RCloudContext.getInstance().getAppKey());
            videoChatInt.putExtra("peerUserPhoteUri", peerUserPhoteUri);
            videoChatInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fCloudService.startActivity(videoChatInt);
        }
    };

    /**
 //=========================================时时  接收消息=====================================================================================
     */
    private final void registerReceviceMessage() {
        try {
                RCloudContext.getInstance().getRongIMClient().setOnReceiveMessageListener(new OnReceiveMessageListener() {

                    @Override
                    public void onReceived(Message message) {
                        Log.d(TAG, "--registerReceviceMessage----onReceived-----date:" + message.getReceivedTime());

                        if (message != null && message.getConversationType() != null) {
                            Intent intent = null;

                            if (message.getConversationType() == ConversationType.PRIVATE) {
                                intent = new Intent(ACTION_P2P_MESSAGE_RECEVICE);

                                Log.i("afff", "===================================message====type=======" + message.getContent());
                                if (message.getContent() instanceof TextMessage) {
                                    TextMessage textMessage = (TextMessage) message.getContent();
                                    String str = HTMLDecoder.decode(HTMLDecoder.decode(textMessage.getContent()));

                                    Log.i("RCLoudClient", "==================--registerReceviceMessage---------:" + message.getReceivedTime() + "==" + str);
                                } else if (message.getContent() instanceof VoipCallMessage) {
                                    final VoipCallMessage voipMessage = (VoipCallMessage) message.getContent();
                                    Log.i("fff", "=========================voipCallMessage=================down===============" + voipMessage.getIp() + "===" + voipMessage.getFromId());
                                    getUserInfo(voipMessage.getFromId(), new GetUserInfoCallback() {
                                        public void onSuccess(RongIMClient.UserInfo user) {
                                            Log.i("aff", "==================onSuccess====================" + user.getName() + "==" + user.getUserId() + "=--=" + user.getPortraitUri());
                                            android.os.Message msg = mHandler.obtainMessage();
                                            String uname = user.getName();
                                            peerUserPhoteUri = user.getPortraitUri();
                                            if (uname == null || "".equals(uname)) {
                                                uname = "未知联系人";
                                            }
                                            voipMessage.setFromUserName(uname);
                                            msg.obj = voipMessage;
                                            mHandler.sendMessage(msg);
                                        }

                                        public void onError() {
                                            Log.i("aff", "==================onError====================");
                                            android.os.Message msg = mHandler.obtainMessage();
                                            voipMessage.setFromUserName("未知联系人");
                                            msg.obj = voipMessage;
                                            mHandler.sendMessage(msg);
                                        }
                                    });
                                    return;
                                } else if (message.getContent() instanceof VoipAcceptMessage) {
                                    Intent i = new Intent();//向voip端的activity发送 消息广播
                                    i.putExtra("messageContent", (VoipAcceptMessage) message.getContent());//被叫方，接受通话请求
                                    i.setAction("com.ccrc.avtest.action.reciveMsg");
                                    fCloudService.sendBroadcast(i);//发送广播
                                    return;
                                } else if (message.getContent() instanceof VoipFinishMessage) {
                                    Intent i = new Intent();//向voip端的activity发送 消息广播
                                    i.putExtra("messageContent", (VoipFinishMessage) message.getContent());//被叫方，接受通话请求
                                    i.setAction("com.ccrc.avtest.action.reciveMsg");
                                    fCloudService.sendBroadcast(i);//发送广播
                                    return;
                                }


                            } else if (message.getConversationType() == ConversationType.DISCUSSION) {
                                intent = new Intent(ACTION_DISCUSSION_MESSAGE_RECEVICE);
                            } else if (message.getConversationType() == ConversationType.CHATROOM) {
                                intent = new Intent(ACTION_CHATROOM_MESSAGE_RECEVICE);
                            } else if (message.getConversationType() == ConversationType.GROUP) {
                                intent = new Intent(ACTION_GROUP_MESSAGE_RECEVICE);
                            }

                            if (message.getContent() instanceof RongIMClient.UnknowMessage) {
                                return;
                            }

                            UIMessage uiMessage = new UIMessage(message);
                            intent.putExtra(UIMessage.MESSAGE_OBJ, uiMessage);
                            intent.putExtra(TARGET_ID, uiMessage.getTargetId());
//                            fCloudService.sendBroadcast(intent);
                            mHandle.obtainMessage(0, intent).sendToTarget();
                            fCloudService.newMessageNotifycation(uiMessage);
                        }

                        if (RCloudContext.getInstance().getOnReceiveMessageListener() != null) {
                            RCloudContext.getInstance().getOnReceiveMessageListener().onReceived(message);
                        }
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "--registerReceviceMessage----error-----");
        }
    }

    @Override
    public boolean handleMessage(android.os.Message msg) {
        Intent intent = (Intent) msg.obj;
        fCloudService.sendBroadcast(intent);
        return false;
    }
    
    /**
     * 媒体类型 1图片 2 Audio 3 Video
     */
    private final void downloadFile(final Intent intentArg) {

        if (intentArg == null) {
            return;
        }

        final String targetId = intentArg.getStringExtra(TARGET_ID);

        final int code = intentArg.getIntExtra(INTENT_MESSAGE_FILE_DOWN_TYPE, 1);
        final ConversationType conversationType = ConversationType.setValue(code);
        final String resKey = intentArg.getStringExtra(INTENT_MESSAGE_FILE_DOWN_RES_KEY);

        RCloudContext.getInstance().getRongIMClient().downloadMedia(conversationType, targetId, MediaType.IMAGE, resKey, new DownloadMediaCallback() {

            @Override
            public void onProgress(int progress) {

                Intent intent = intentArg;
                intent.putExtra(INTENT_MESSAGE_FILE_DOWN_PROGRESS, progress);
                intent.putExtra(INTENT_IS_COMPLETE, false);
                fCloudService.sendBroadcast(intent);
            }

            @Override
            public void onSuccess(String localMediaPath) {
                Log.d("MessageLogic", "downloadFile---onSuccess--" + localMediaPath);

                Intent intent = intentArg;
                intent.putExtra(INTENT_MESSAGE_FILE_DOWN_RES_PATH, localMediaPath);
                intent.putExtra(INTENT_IS_COMPLETE, true);
                intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                fCloudService.sendBroadcast(intent);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                Log.d("MessageLogic", "downloadFile---errorCode--");

                Intent intent = intentArg;
                intent.putExtra(INTENT_IS_COMPLETE, true);
                intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                fCloudService.sendBroadcast(intent);
            }

        });
    }

    /**
     * 讨论组邀请成员 ACTION_DISCUSSION_INVITE_MEMBER
     *
     * @param intent
     */
    public void inviteMemberToDiscussion(final Intent intent) {

        final String discussionId = intent.getStringExtra(DISCUSSION_ID);
        ArrayList<String> userIdList = intent.getStringArrayListExtra(DISCUSSION_MEMBER_ID);

        RCloudContext.getInstance().getRongIMClient().inviteMemberToDiscussion(discussionId, userIdList, new OperationCallback() {

            @Override
            public void onSuccess() {
                intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                fCloudService.sendBroadcast(intent);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                fCloudService.sendBroadcast(intent);
            }
        });
    }

    /**
     * 创建讨论组
     *
     * @param intent
     */
    private final void createDiscussion(final Intent intent) {
        Log.d(TAG, "entenr createDiscussion---->");

        String name = intent.getStringExtra(DISCUSSION_NAME);
        ArrayList<String> userIdList = intent.getStringArrayListExtra(DISCUSSION_MEMBER_ID);

        RCloudContext.getInstance().getRongIMClient().createDiscussion(name, userIdList, new CreateDiscussionCallback() {

            @Override
            public void onSuccess(String discussionId) {
                Log.d(TAG, "entenr createDiscussion---->onSuccess");

                intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                intent.putExtra(DISCUSSION_ID, discussionId);
                fCloudService.sendBroadcast(intent);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                Log.d(TAG, "entenr createDiscussion---->onError" + errorCode.getValue());

                intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                fCloudService.sendBroadcast(intent);
            }

        });

    }

    /**
     * 删除讨论组成员 API_OPERATION_STATUS
     *
     * @param intent
     */
    public void removeMemberFromDiscussion(final Intent intent) {

        Log.d(TAG, "enter removeMemberFromDiscussion---->");

        String discussionId = intent.getStringExtra(DISCUSSION_ID);
        String userId = intent.getStringExtra(INTENT_USER_ID);

        RCloudContext.getInstance().getRongIMClient().removeMemberFromDiscussion(discussionId, userId, new OperationCallback() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "enter removeMemberFromDiscussion----onSuccess--->");
                intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                fCloudService.sendBroadcast(intent);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                Log.d(TAG, "enter removeMemberFromDiscussion----onError--->");
                intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                fCloudService.sendBroadcast(intent);
            }

        });
    }

    public void getDiscussion(final Intent intent) {

        Log.d(TAG, "enter getDiscussion---->");

        String discussionId = intent.getStringExtra(DISCUSSION_ID);

        RCloudContext.getInstance().getRongIMClient().getDiscussion(discussionId, true, new GetDiscussionCallback() {

            @Override
            public void onSuccess(Discussion discussion) {

                Log.d(TAG, "enter getDiscussion---onSuccess->");
//                Log.d(TAG, "enter getDiscussion---onSuccess->" + discussion.getName());
                intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                intent.putExtra(DISCUSSION_OBJECT, new UIDiscussion(discussion));
                mHandle.obtainMessage(0,intent).sendToTarget();

//                fCloudService.sendBroadcast(intent);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                Log.d(TAG, "enter getDiscussion---onError->");
                intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                fCloudService.sendBroadcast(intent);

            }

        });
    }

    /**
     * 退出讨论组
     *
     * @param intent
     */
    public void quitDiscussion(final Intent intent) {
        Log.d(TAG, "quitDiscussion------");

        String discussionId = intent.getStringExtra(DISCUSSION_ID);

        RCloudContext.getInstance().getRongIMClient().quitDiscussion(discussionId, new OperationCallback() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "quitDiscussion------onSuccess");
                intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                fCloudService.sendBroadcast(intent);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                Log.d(TAG, "quitDiscussion------onError");
                intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                fCloudService.sendBroadcast(intent);
            }

        });
    }

    /**
     * 修改讨论名字
     *
     * @param intent
     */
    public void updateDiscussionName(final Intent intent) {

        String discussionId = intent.getStringExtra(DISCUSSION_ID);
        String name = intent.getStringExtra(DISCUSSION_NAME);

        RCloudContext.getInstance().getRongIMClient().setDiscussionName(discussionId, name, new RongIMClient.UpdateDiscussionNameCallback() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "updateDiscussionName------onSuccess");
                intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                fCloudService.sendBroadcast(intent);
            }

            @Override
            public void onError(RongIMClient.UpdateDiscussionNameCallback.ErrorCode errorCode) {
                Log.d(TAG, "updateDiscussionName------onError");
                intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                fCloudService.sendBroadcast(intent);
            }

        });

    }

    /**
     * 设置讨论组邀请状态
     *
     * @param intent
     */
    public void setDiscussionInviteStatus(final Intent intent) {
        String discussionId = intent.getStringExtra(DISCUSSION_ID);
        boolean isOpen = intent.getBooleanExtra(DISCUSSION_IS_OPEN_INVITE_STATUS, true);

        RCloudContext.getInstance().getRongIMClient().setDiscussionInviteStatus(discussionId, isOpen, new RongIMClient.SetInviteStatusCallback() {

            @Override
            public void onComplete(int opStatus) {
                Log.d(TAG, "setDiscussionInviteStatus------status:"+opStatus);
                intent.putExtra(INTENT_API_OPERATION_STATUS, opStatus);
                fCloudService.sendBroadcast(intent);
            }

        });
    }

    /**
     * 设置不接收新消息通知
     *
     * @param intent
     */
    public void setBlockPushStatus(final Intent intent) {

        String targetId = intent.getStringExtra(TARGET_ID);
        boolean isBlock = intent.getBooleanExtra(INTENT_NEW_MESSAGE_BLOCK, false);

        int conversationTypeValue = intent.getIntExtra(INTENT_CONVERSATION_TYPE, 0);
        ConversationType conversationType = ConversationType.setValue(conversationTypeValue);

        if (ConversationType.DISCUSSION == conversationType) {

            RCloudContext.getInstance().getRongIMClient().setBlockPushDiscussionStatus(targetId, isBlock, new RongIMClient.SetBlockStatusCallback() {
                @Override
                public void onSuccess(int status) {
                    Log.d(TAG, "setBlockPushStatus------onSuccess--status:" + status);
                    intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                    intent.putExtra(INTENT_STATUE_VALUE, status);
                    fCloudService.sendBroadcast(intent);
                }

                @Override
                public void onError() {
                    Log.d(TAG, "setBlockPushStatus------onError");
                    intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                    fCloudService.sendBroadcast(intent);
                }
            });
        } else if (ConversationType.PRIVATE == conversationType) {

            RCloudContext.getInstance().getRongIMClient().setBlockPushUserStatus(targetId, isBlock, new RongIMClient.SetBlockStatusCallback() {

                @Override
                public void onSuccess(int status) {
                    Log.d(TAG, "setBlockPushStatus------onSuccess--status:" + status);
                    intent.putExtra(INTENT_STATUE_VALUE, status);
                    intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                    fCloudService.sendBroadcast(intent);
                }

                @Override
                public void onError() {
                    Log.d(TAG, "setBlockPushStatus------onError");
                    intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                    fCloudService.sendBroadcast(intent);
                }
            });
        }
    }

    public void getBlockPushStatus(final Intent intent) {

        String targetId = intent.getStringExtra(TARGET_ID);

        int conversationTypeValue = intent.getIntExtra(INTENT_CONVERSATION_TYPE, 0);
        ConversationType conversationType = ConversationType.setValue(conversationTypeValue);

        if (ConversationType.PRIVATE == conversationType) {

            RCloudContext.getInstance().getRongIMClient().getBlockPushUserStatus(targetId, false, new RongIMClient.SetBlockStatusCallback() {
                @Override
                public void onSuccess(int status) {
                    Log.d(TAG, "getBlockPushStatus------onSuccess--status:" + status);
                    intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                    intent.putExtra(INTENT_STATUE_VALUE, status);
                    fCloudService.sendBroadcast(intent);
                }

                @Override
                public void onError() {
                    Log.d(TAG, "getBlockPushStatus------onError");
                    intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                    fCloudService.sendBroadcast(intent);
                }
            });
        } else if (ConversationType.DISCUSSION == conversationType) {
            RCloudContext.getInstance().getRongIMClient().getBlockPushDiscussionStatus(targetId, false, new RongIMClient.SetBlockStatusCallback() {

                @Override
                public void onSuccess(int status) {
                    Log.d(TAG, "getBlockPushStatus------onSuccess--status:" + status);
                    intent.putExtra(INTENT_STATUE_VALUE, status);
                    intent.putExtra(INTENT_API_OPERATION_STATUS, true);
                    fCloudService.sendBroadcast(intent);
                }

                @Override
                public void onError() {
                    Log.d(TAG, "getBlockPushStatus------onError");
                    intent.putExtra(INTENT_API_OPERATION_STATUS, false);
                    fCloudService.sendBroadcast(intent);
                }
            });
        }
    }


    @SuppressWarnings("static-access")
    public void receviceConnectStateChange() {
        RCloudContext.getInstance().getRongIMClient().setConnectionStatusListener(new ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus status) {
                Log.d(TAG, "receviceConnectStateChange---onChanged--ConnectionStatus:" + status);

                Intent intent = new Intent(ACTION_RECEVICE_NETWORD_CONNECT_STATE);
                intent.putExtra(INTENT_NETWORK_CONNECT_STATE, status.getValue());
//                fCloudService.sendBroadcast(intent);
                mHandle.obtainMessage(0, intent).sendToTarget();
            }
        });
    }
}