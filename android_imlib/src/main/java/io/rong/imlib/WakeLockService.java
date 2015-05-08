package io.rong.imlib;

import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

abstract class WakeLockService extends Service {

    private WakeLock mWakeLock;

    protected void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
            mWakeLock.setReferenceCounted(false);

            mWakeLock.acquire();
        }
    }


    protected void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            try {
                mWakeLock.release();
            } catch (RuntimeException e) {

            }
            mWakeLock = null;
        }
    }
}
