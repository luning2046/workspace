package io.rong.imkit.common;

import android.content.Context;
import android.content.ContextWrapper;

/**
 * Created by DragonJ on 14-7-21.
 */
public class MessageContext extends ContextWrapper {

    IVoiceHandler mVoiceHandler;

    public MessageContext(Context base, IVoiceHandler voiceHandler) {
        super(base);
        mVoiceHandler = voiceHandler;
    }

    public IVoiceHandler getVoiceHandler()
    {
        return mVoiceHandler;
    }
}
