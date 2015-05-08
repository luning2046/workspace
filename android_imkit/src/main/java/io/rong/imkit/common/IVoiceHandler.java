package io.rong.imkit.common;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by DragonJ on 14-7-19.
 */
public interface IVoiceHandler {
    public class VoiceException extends RuntimeException {
        public VoiceException(Throwable e) {
            super(e);
        }
    }

    public interface OnPlayListener {
        public void onPlay();

        public void onCover(boolean limited);

        public void onStop();
    }

    public interface OnRecListener {
        public void onRec();

        public void onCover(boolean limited);

        public void onCompleted(Uri uri);
    }

    public void setPlayListener(OnPlayListener listener);

    public void setRecListener(OnRecListener listener);

    public void play(Uri... uris) throws VoiceException;

    public void stop();

    public Uri getCurrentPlayUri();

    public void startRec() throws VoiceException;

    public Uri stopRec(boolean save);

    public int getCurrentDb();

    public class VoiceHandler implements IVoiceHandler, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, SensorEventListener {

        private AudioManager mAudioManager;
        private SensorManager mSensorManager;
        private PowerManager mPowerManager;

        private MediaPlayer mMediaPlayer;
        private MediaRecorder mMediaRecorder;
        private Sensor mSensor;
        private Context mContext;
        private PowerManager.WakeLock mLock;

        private File recRoot;
        private Uri mCurrentRecUri;
        private List<Uri> mUriCollections;

        private OnPlayListener mPlayListener;
        private OnRecListener mRecListener;

        static final int PLAY_VOICE_COLLECTIONS = 1;

        public VoiceHandler(Context context, File recRoot) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            this.recRoot = recRoot;

            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            mLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "VoiceHandler");
        }

        public void setPlayListener(OnPlayListener listener) {
            mPlayListener = listener;
        }

        public void setRecListener(OnRecListener listener) {
            mRecListener = listener;
        }

        private void play(Uri uri) throws VoiceException {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    mLock.acquire(mp.getDuration());

                    if (mPlayListener != null)
                        mPlayListener.onPlay();
                    if (mSensor != null) {
                        mSensorManager.registerListener(VoiceHandler.this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                    }
                }
            });

            try {
                mMediaPlayer.setDataSource(mContext, uri);
                mMediaPlayer.prepare();
            } catch (IllegalArgumentException e) {
                throw new VoiceException(e);
            } catch (SecurityException e) {
                throw new VoiceException(e);
            } catch (IllegalStateException e) {
                throw new VoiceException(e);
            } catch (IOException e) {
                throw new VoiceException(e);
            }
        }

        public void play(Uri... uris){
            if(uris==null||uris.length == 0)
                return;

            mUriCollections = new LinkedList<Uri>(Arrays.asList(uris));

            play(mUriCollections.get(0));
        }

        public void stop() {
            if(mMediaPlayer == null)
                return;
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mUriCollections = null;
            if (mPlayListener != null) {
                mPlayListener.onStop();
            }
            mSensorManager.unregisterListener(VoiceHandler.this);
        }

        private void completePlay(){
            if(mMediaPlayer == null)
                return;
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            if (mPlayListener != null) {
                mPlayListener.onStop();
            }

            mSensorManager.unregisterListener(VoiceHandler.this);
        }


        public Uri getCurrentPlayUri() {
            if(mUriCollections == null || mUriCollections.size() == 0)
                return null;

            return mUriCollections.get(0);
        }

        public void startRec() throws VoiceException {

            if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                stop();

            mAudioManager.setMode(AudioManager.MODE_NORMAL);

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mCurrentRecUri = Uri.fromFile(new File(recRoot, UUID.randomUUID().toString()));
            mMediaRecorder.setOutputFile(mCurrentRecUri.getPath());
            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                throw new VoiceException(e);
            }
            try {
                mMediaRecorder.start();
            } catch (RuntimeException ex) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
                throw new VoiceException(ex);
            }

            mSensorManager.registerListener(VoiceHandler.this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

            if (mRecListener != null)
                mRecListener.onRec();

            mLock.acquire();
        }

        public Uri stopRec(boolean save) throws VoiceException {
            if (mMediaRecorder == null)
                return null;

            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            } catch (RuntimeException e) {
                throw new VoiceException(e);
            }

            Uri temp = mCurrentRecUri;
            mCurrentRecUri = null;

            mSensorManager.unregisterListener(VoiceHandler.this);

            if (mRecListener != null&&save)
                mRecListener.onCompleted(temp);

            if(!save){
                File file = new File(temp.getPath());
                if(file.exists())
                    file.delete();
            }

            mLock.release();

            return temp;
        }

        public int getCurrentDb() {
            if (mMediaRecorder == null)
                return 0;
            return mMediaRecorder.getMaxAmplitude() / 600;
        }


        @Override
        public void onCompletion(MediaPlayer mp) {
            completePlay();
            if (mPlayListener != null) {
                mPlayListener.onStop();
            }

            if(mUriCollections != null && mUriCollections.size() > 0){
                mUriCollections.remove(0);
            }
            if(mUriCollections == null || mUriCollections.size() == 0)
                return;

            play(mUriCollections.get(0));
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mMediaPlayer.reset();
            return false;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float value = event.values[0];
            Log.d(getClass().getCanonicalName(),event.values[0]+" "+mSensor.getMaximumRange());

            if (value >= mSensor.getMaximumRange()/2) {
                if (mAudioManager.getMode() != AudioManager.MODE_NORMAL)
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);

                if (mPlayListener != null) {
                    mPlayListener.onCover(false);
                }

                if (mRecListener != null) {
                    mRecListener.onCover(false);
                }
            } else {
                if (mAudioManager.getMode() != AudioManager.MODE_IN_CALL) {
                    mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                }

                if (mPlayListener != null) {
                    mPlayListener.onCover(true);
                }

                if (mRecListener != null) {
                    mRecListener.onCover(true);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }
}
