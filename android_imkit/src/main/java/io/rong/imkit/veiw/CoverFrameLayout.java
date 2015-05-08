package io.rong.imkit.veiw;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.utils.ResourceUtils;

import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

public class CoverFrameLayout extends FrameLayout {
    CoverHandler mCoverHandler;
    View mTriggerView;
    int mCoverViewResId;
    int mTriggerViewResId;
    int mTriggerLimit;
    float mTriggerCenterX = 0;
    float mTriggerCenterY = 0;
    OnTriggeredTouchListener mTriggeredTouchListener;

    public CoverFrameLayout(android.content.Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, ResourceUtils.getStyleablesResourceId(context, "CoverFrameLayout"));

        mCoverViewResId = a.getResourceId(ResourceUtils.getStyleableResourceId(context, "CoverFrameLayout_coverView"), 0);
        mTriggerViewResId = a.getResourceId(ResourceUtils.getStyleableResourceId(context, "CoverFrameLayout_triggerView"), 0);
        mTriggerLimit = a.getInt(ResourceUtils.getStyleableResourceId(context, "CoverFrameLayout_triggerLimit"), 200);

        if (mCoverViewResId == 0 || mTriggerViewResId == 0)
            throw new RuntimeException("CoverView or TriggerView not define");
    }


    @Override
    protected void onAttachedToWindow() {
        View view = findViewById(mCoverViewResId);
        if (!(view instanceof CoverHandler))
            throw new RuntimeException("CoverView not impl CoverHandler");
        mCoverHandler = (CoverHandler)view;
        mCoverHandler.setStatus(CoverHandler.STATUS_NORMAL);
        mTriggerView = findViewById(mTriggerViewResId);
        mTriggerView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        Rect rect = new Rect();
        mTriggerView.getHitRect(rect);
        mTriggerCenterX = rect.centerX();
        mTriggerCenterY = rect.centerY();
        super.onAttachedToWindow();
    }

    boolean mHasTrigger;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(this.getClass().getCanonicalName(), ev.toString());

        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            mCoverHandler.setStatus(CoverHandler.STATUS_NORMAL);
        }

        if (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP) {
            if (mHasTrigger) {
                MotionEvent event = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), ev.getAction(), mTriggerCenterX, mTriggerCenterY, ev.getMetaState());
                mTriggerView.dispatchTouchEvent(ev);

                mHasTrigger = false;
                mCoverHandler.setStatus(CoverHandler.STATUS_CLOSE);

                if (mTriggeredTouchListener != null) {
                    mTriggeredTouchListener.OnTriggeredTouchEvent(event,this);
                }
            }
        }

        if (mHasTrigger) {

            MotionEvent event = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), ev.getAction(), mTriggerCenterX, mTriggerCenterY, ev.getMetaState());
            mTriggerView.dispatchTouchEvent(event);

            if (mTriggeredTouchListener != null && ev.getEventTime() - ev.getDownTime() > mTriggerLimit) {
                mTriggeredTouchListener.OnTriggeredTouchEvent(ev,this);
            }

            if ((mCoverHandler.getStatus()&CoverHandler.STATUS_MASK) == 0) {
                mCoverHandler.setStatus(CoverHandler.STATUS_OPEN);
            }
        }
        return true;
    }

    public CoverHandler getCoverHandler() {
        return mCoverHandler;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(this.getClass().getCanonicalName(), ev.toString());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (mTriggerView.getVisibility() != View.VISIBLE)
            return super.onInterceptTouchEvent(ev);

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            Rect rect = new Rect();
            int offsetX = 0;
            int offsetY = 0;

            View tempView = mTriggerView;

            while (tempView.getParent() != this) {
                if (tempView.getParent() instanceof View) {
                    tempView = (View) tempView.getParent();
                } else {
                    break;
                }
                offsetY += tempView.getTop();
                offsetX += tempView.getLeft();
            }


            mTriggerView.getHitRect(rect);
            mHasTrigger = rect.contains((int) ev.getX() - offsetX, (int) ev.getY() - offsetY);
        }

        if (mHasTrigger) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setOnTriggeredTouchListener(OnTriggeredTouchListener listener) {
        mTriggeredTouchListener = listener;
    }

    public interface OnTriggeredTouchListener {
        public void OnTriggeredTouchEvent(MotionEvent event, CoverFrameLayout coverFrameLayout);
    }

    interface CoverHandler {
        static final int STATUS_NORMAL = 0x0000FFFF;
        static final int STATUS_OPEN = 0x0001FFFF;
        static final int STATUS_CLOSE = 0x0002FFFF;
        static final int STATUS_MASK = 0xFFFF0000;

        void setStatus(int status);

        int getStatus();
    }
}
