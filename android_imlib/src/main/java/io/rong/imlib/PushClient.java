package io.rong.imlib;

import io.rong.imlib.PushProtocalStack.ConnAckMessage;
import io.rong.imlib.PushProtocalStack.ConnectMessage;
import io.rong.imlib.PushProtocalStack.DisconnectMessage;
import io.rong.imlib.PushProtocalStack.Message;
import io.rong.imlib.PushProtocalStack.MessageInputStream;
import io.rong.imlib.PushProtocalStack.MessageOutputStream;
import io.rong.imlib.PushProtocalStack.PingReqMessage;
import io.rong.imlib.PushProtocalStack.PublishMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

import android.util.Log;

class PushClient {
	private MessageInputStream in;
	private Socket socket;
	private MessageOutputStream out;
	public OutputStream os;
	private PushReader reader;
	private Semaphore connectionAckLock;
	private int messageId = 0;
	private ClientListener listener;
	private ConnectMessage connectMsg;
	private ConnectCallback connectCallback;

	public PushClient(String clientId, String appkey, String token, ClientListener listener) {
		this.listener = listener;
		connectMsg = new ConnectMessage(clientId, true, 300);
		connectMsg.setCredentials(appkey, token);
		connectMsg.setWill("clientInfo", String.format("%s-%s-%s", "testClient", "1.0", "1.0"));
	}

	public void connect(String host, int port, ConnectCallback callback) throws UnknownHostException, IOException, InterruptedException {
		socket = new Socket(host, port);
		InputStream is = socket.getInputStream();
		in = new MessageInputStream(is);
		os = socket.getOutputStream();
		out = new MessageOutputStream(os);
		reader = new PushReader();
		reader.start();
		connectCallback = callback;
		connectionAckLock = new Semaphore(0);
		out.writeMessage(connectMsg);
		connectionAckLock.acquire();

		while (true) {
			Thread.sleep(240000);
			ping();
		}
	}

	public void ping() throws IOException {
		out.writeMessage(new PingReqMessage());
	}

	public void disconnect() throws IOException {
		socket.close();
	}

	public void disconnectByNormal(){
		DisconnectMessage msg = new DisconnectMessage(DisconnectMessage.DisconnectionStatus.CLOSURE);
		try {
			out.writeMessage(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void handleMessage(Message msg) throws IOException {
		if (msg == null) {
			return;
		}
		Log.d("Method ", "========================" + msg.getType());
		switch (msg.getType()) {
		case CONNACK:
			connectionAckLock.release();
			if (connectCallback != null) {
				ConnAckMessage connAckMsg = (ConnAckMessage) msg;
				connectCallback.connected(connAckMsg);
			}
			break;
		case PINGRESP:
			break;
		case PUBLISH:
			PublishMessage publishMsg = (PublishMessage) msg;
			if (listener != null) {
				listener.messageArrived(publishMsg);
			}
			break;
		default:
			break;
		}
	}

	private class PushReader extends Thread {

		@Override
		public void run() {
			Message msg;
			try {
				while (true) {
                    try {
                        Thread.sleep(100);
                    }
                    catch(Exception e){

                    }
					msg = in.readMessage();
					if (msg != null) {
						handleMessage(msg);
					}
				}
			} catch (IOException e) {
			}
		}
	}

	public interface ClientListener {

		void messageArrived(PublishMessage msg);

	}

	public interface ConnectCallback {
		void connected(ConnAckMessage msg) throws IOException;
	}
}
