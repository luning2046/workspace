package io.rong.imlib;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by DragonJ on 14-6-22.
 */
class RongWakeLock {

    PowerManager.WakeLock mLock;
    Context mContext;

    RongWakeLock(Context context){
        mContext = context;
    }

    protected void acquireWakeLock(int timeout) {
        acquireWakeLock(timeout,PowerManager.PARTIAL_WAKE_LOCK);
    }

    protected void acquireWakeLock(int timeout, int level) {
        if (mLock == null) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mLock = pm.newWakeLock(level, this.getClass().getCanonicalName());
            if(timeout <= 0)
                mLock.acquire();
            else
                mLock.acquire(timeout);
        }
    }

    protected void acquireWakeLock() {
        acquireWakeLock(0);
    }

    protected void releaseWakeLock() {
        if (mLock != null && mLock.isHeld()) {
            mLock.release();
            mLock = null;
        }
    }
}
