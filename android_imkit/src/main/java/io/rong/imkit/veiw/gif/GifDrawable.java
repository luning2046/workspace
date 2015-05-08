package io.rong.imkit.veiw.gif;

import io.rong.imkit.veiw.AnimationTextView;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class GifDrawable extends AnimationDrawable implements GifAction {

    private static final String TAG = "GifDrawable";
    /**
     * 当前帧
     */
    private int mCurrentIndex = 0;
    /**
     * 异步解码器
     */
    private GifDecoder mDecoder;
    /**
     * 缓存需要刷新的View
     * 此集合尽量不要太大，避免内存泄露。
     * 需要在listview.RecyclerListener.onMovedToScrapHeap方法中
     * 释放GifDrawable引用的view。
     */
    private final ArrayList<SoftReference<AnimationTextView>> cachedAnimationViews;
    /**
     * 表情文件路径
     */
    public String mFilePath;
    /**
     * 表情转义符
     */
    public String mEmotionCode;
    /**
     * 播放标识，默认为false
     * 再listview滑动停止的时候置为true，这样可以
     * 在快速滑动、以及刚进入activity的时候避免向主线程
     * 发刷新的消息。
     */
    private boolean onStarted = false;
    private final Context mContext;

    /**
     * @param context &#x7cfb;&#x7edf;context
     * @param
     * @param
     */
    public GifDrawable(Context context, String emotionCode, String path) {
        mContext = context;
        mFilePath = path;
        mEmotionCode = emotionCode;
        cachedAnimationViews = new ArrayList<SoftReference<AnimationTextView>>();
        try {
            InputStream is;
            if (path.contains("Emotions")) {
                is = new FileInputStream(path);
            } else {
                is = context.getAssets().open(path);
            }
            mDecoder = new GifDecoder(is, this);
            Bitmap image = mDecoder.getFirstFrame().image;
            BitmapDrawable drawable = new BitmapDrawable(context.getResources(), image);
            addFrame(drawable, mDecoder.getFirstFrame().delay);
            freeDecoder(mDecoder);
            if (path.contains("Emotions")) {
                is = new FileInputStream(path);
            } else {
                is = context.getAssets().open(path);
            }
            mDecoder = new GifDecoder(is, this);
            mDecoder.start();
        } catch (Exception e) {
            // TODO: handle exception
//			if (LogF.DEBUG) {
            Log.d(TAG, "GifDrawable constructor has Exception:" + e.getMessage());
//			}
        }
    }

    public void addAnimationView(AnimationTextView animationView) {
        try {
            //已经缓存不再次缓存
            for (SoftReference<AnimationTextView> reference : cachedAnimationViews) {
                if (reference.get() != null) {
                    if (reference.get().equals(animationView)) {
                        return;
                    }
                }
            }
            cachedAnimationViews.add(new SoftReference<AnimationTextView>(animationView));
        } catch (Exception e) {
            // TODO: handle exception
//			if (LogF.DEBUG) {
            Log.d(TAG, "addAnimationView has excetion:" + e.getMessage());
//			}
        }
    }

    public ArrayList<SoftReference<AnimationTextView>> getAnimationView() {
        return cachedAnimationViews;
    }

    /**
     * 需要在listview.RecyclerListener.onMovedToScrapHeap方法中
     * 释放GifDrawable引用的view。
     *
     * @param view
     */
    public void removeAnimationView(View view) {
        cachedAnimationViews.remove(view);
    }

    public void ChangeToNextFrame() {
        int number = getNumberOfFrames();
        if (mCurrentIndex >= number) {
            mCurrentIndex = 0;
        }
        selectDrawable(mCurrentIndex++);
    }

    private void freeDecoder(GifDecoder gifDecoder) {
        if (gifDecoder != null) {
            gifDecoder.free();
            gifDecoder = null;
        }
    }

    @Override
    public void parseOk(boolean parseStatus, int frameIndex) {
        if (parseStatus && frameIndex != 1 && !mDecoder.isGetFirst()) {
            handler.sendEmptyMessage(frameIndex);
        }
    }

    public boolean isOnStarted() {
        return onStarted;
    }

    public void setOnStarted(boolean onStarted) {
        this.onStarted = onStarted;
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            GifFrame frame = mDecoder.next();
            if (frame == null) {
                return;
            }
            Bitmap image = frame.image;
            BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), image);
            drawable.setFilterBitmap(true);
            addFrame(drawable, frame.delay);
            setVisible(true, false);
        }
    };
}
