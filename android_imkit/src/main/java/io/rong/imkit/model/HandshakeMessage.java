package io.rong.imkit.model;

import android.os.Parcel;

import io.rong.imlib.MessageTag;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.MessageContent;

@MessageTag(value = "RC:HsMsg", flag = 0)
public class HandshakeMessage extends RongIMClient.TextMessage {


    public HandshakeMessage() {

    }


    public HandshakeMessage(byte[] data) {

    }


    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] encode() {
        return "{\"type\":1}".getBytes();
    }

}
