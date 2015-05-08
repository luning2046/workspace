package io.rong.imkit.demo;

import android.app.Application;

import io.rong.imkit.RongIM;

/**
 * Created by zhjchen on 14-3-20.
 */
public class DemoApplication extends Application {

    private static final String IS_FIRST = "is_first";

    public static final String APP_KEY = "e0x9wycfx7flq";
//    public static final String APP_KEY="z3v5yqkbv8v30";

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * IMKit SDK调用第一步 初始化
         * 第一个参数，  context上下文
         * 第二个参数，APPKey换成自己的appkey
         * 第三个参数，push消息通知所要打个的action页面
         * 第四个参数，push消息中可以自定义push图标
         */
        RongIM.init(this, APP_KEY, R.drawable.ic_launcher);

//===================主要功能配置通过http访问网络的代码==========================DemoApi是访问http的入口======================================
        DemoContext.getInstance().init(this);

        try {
            System.loadLibrary("imdemo");
        } catch (UnsatisfiedLinkError e) { }
    }
}