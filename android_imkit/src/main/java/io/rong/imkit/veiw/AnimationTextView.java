/*
 * 创建日期：2013-3-31
 */
package io.rong.imkit.veiw;

import io.rong.imkit.veiw.gif.GifDrawable;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;


public class AnimationTextView extends TextView {

    private static final String TAG = "AnimationTextView";
    private GifDrawable mDrawable;

    /**
     * 此Hander必须为静态，否则如果两个同时显示在
     * 屏幕上的View的mDrawable相同，会导致向ui线
     * 程中放两个message，动画播放速率快
     */
    private static AnimationHandler mHandler;

    public AnimationTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public AnimationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public AnimationTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    /**
     * 当mDrawable == who才开始重绘
     */
    @Override
    protected boolean verifyDrawable(Drawable who) {
        // TODO Auto-generated method stub
        return mDrawable == who;
    }

    public void startGifAnimation() {
        setNextFrame();
    }

    /**
     * 播放下一帧动画
     */
    private void setNextFrame() {
        if (null == mDrawable || !mDrawable.isOnStarted() || mDrawable.getAnimationView().size() <= 0) {
            return;
        }

        if (null == mHandler) {
            mHandler = new AnimationHandler();
        }
        Message message = mHandler.obtainMessage();
        message.what = ((Object)mDrawable).hashCode();
        AnimationHolder holder = new AnimationHolder();
        holder.textView = this;
        holder.gifDrawable = mDrawable;
        message.obj = holder;
        mHandler.sendMessageAtTime(message, SystemClock.uptimeMillis() + mDrawable.getDuration(0));
    }

    /**
     * @return Returns the mDrawable.
     */
    public GifDrawable getGifDrawable() {
        return mDrawable;
    }

    /**
     */
    public void seGifDrawable(GifDrawable gifDrawable) {
        this.mDrawable = gifDrawable;
    }

    /**
     * 在handleMessage的时候刷新GifDrawable所在的View,
     * 并播放下一帧,播放前线清除ui线程中此GifDrawable相关
     * 的消息
     */
    public static class AnimationHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            AnimationHolder holder = (AnimationHolder) msg.obj;
            removeMessages(((Object)holder.gifDrawable).hashCode());
            holder.gifDrawable.ChangeToNextFrame();
            for (SoftReference<AnimationTextView> reference : holder.gifDrawable.getAnimationView()) {
                if (null != reference.get()) {
                    reference.get().invalidateDrawable(holder.gifDrawable);
                }
            }
            holder.textView.setNextFrame();
        }
    }

    /**
     * 在线程中使用此对象来访问AnimationTextView,GifDrawable
     */
    private class AnimationHolder {
        AnimationTextView textView;
        GifDrawable gifDrawable;
    }
}
