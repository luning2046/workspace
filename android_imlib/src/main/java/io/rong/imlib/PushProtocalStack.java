package io.rong.imlib;

import io.rong.imlib.PushProtocalStack.Message.Header;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class PushProtocalStack {
	public static class ConnAckMessage extends Message {

		public final static int MESSAGE_LENGTH = 2;

		public enum ConnectionStatus {
			ACCEPTED,

			UNACCEPTABLE_PROTOCOL_VERSION,

			IDENTIFIER_REJECTED,

			SERVER_UNAVAILABLE,

			BAD_USERNAME_OR_PASSWORD,

			NOT_AUTHORIZED, REDIRECT
		}

		private ConnectionStatus status;
		private String userId;

		public ConnAckMessage() {
			super(Type.CONNACK);
		}

		public ConnAckMessage(Header header) throws IOException {
			super(header);
		}

		public ConnAckMessage(ConnectionStatus status) {
			super(Type.CONNACK);
			if (status == null) {
				throw new IllegalArgumentException("The status of ConnAskMessage can't be null");
			}
			this.status = status;
		}

		@Override
		protected int messageLength() {
			int length = MESSAGE_LENGTH;

			if (userId != null && !userId.isEmpty()) {
				length += FormatUtil.toWMtpString(userId).length;
			}

			return length;
		}

		@Override
		protected void readMessage(InputStream in, int msgLength) throws IOException {
			// Ignore first byte
			in.read();
			int result = in.read();
			switch (result) {
			case 0:
				status = ConnectionStatus.ACCEPTED;
				break;
			case 1:
				status = ConnectionStatus.UNACCEPTABLE_PROTOCOL_VERSION;
				break;
			case 2:
				status = ConnectionStatus.IDENTIFIER_REJECTED;
				break;
			case 3:
				status = ConnectionStatus.SERVER_UNAVAILABLE;
				break;
			case 4:
				status = ConnectionStatus.BAD_USERNAME_OR_PASSWORD;
				break;
			case 5:
				status = ConnectionStatus.NOT_AUTHORIZED;
				break;
			case 6:
				status = ConnectionStatus.REDIRECT;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported CONNACK code: " + result);
			}

			if (msgLength > MESSAGE_LENGTH) {
				DataInputStream dis = new DataInputStream(in);
				userId = dis.readUTF();
			}
		}

		@Override
		protected void writeMessage(OutputStream out) throws IOException {
			out.write(0x00);
			switch (status) {
			case ACCEPTED:
				out.write(0x00);
				break;
			case UNACCEPTABLE_PROTOCOL_VERSION:
				out.write(0x01);
				break;
			case IDENTIFIER_REJECTED:
				out.write(0x02);
				break;
			case SERVER_UNAVAILABLE:
				out.write(0x03);
				break;
			case BAD_USERNAME_OR_PASSWORD:
				out.write(0x04);
				break;
			case NOT_AUTHORIZED:
				out.write(0x05);
				break;
			case REDIRECT:
				out.write(0x06);
				break;
			default:
				throw new UnsupportedOperationException("Unsupported CONNACK code: " + status);
			}

			if (userId != null && !userId.isEmpty()) {
				DataOutputStream dos = new DataOutputStream(out);
				dos.writeUTF(userId);
				dos.flush();
			}
		}

		public ConnectionStatus getStatus() {
			return status;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getUserId() {
			return userId;
		}

		@Override
		public void setDup(boolean dup) {
			throw new UnsupportedOperationException("CONNACK messages don't use the DUP flag.");
		}

		@Override
		public void setRetained(boolean retain) {
			throw new UnsupportedOperationException("CONNACK messages don't use the RETAIN flag.");
		}

		@Override
		public void setQos(QoS qos) {
			throw new UnsupportedOperationException("CONNACK messages don't use the QoS flags.");
		}

	}

	public static class ConnectMessage extends Message {

		private static int CONNECT_HEADER_SIZE = 12;

		private String protocolId = "MQIsdp";
		private byte protocolVersion = 3;
		private String clientId;
		private int keepAlive;
		private String username;
		private String password;
		private boolean cleanSession;
		private String willTopic;
		private String will;
		private QoS willQoS;
		private boolean retainWill;
		private boolean hasUsername;
		private boolean hasPassword;
		private boolean hasWill;

		public ConnectMessage() {
			super(Type.CONNECT);
		}

		public ConnectMessage(Header header) throws IOException {
			super(header);
		}

		public ConnectMessage(String clientId, boolean cleanSession, int keepAlive) {
			super(Type.CONNECT);
			if (clientId == null || clientId.length() > 64) {
				throw new IllegalArgumentException("Client id cannot be null and must be at most 64 characters long: " + clientId);
			}
			this.clientId = clientId;
			this.cleanSession = cleanSession;
			this.keepAlive = keepAlive;
		}

		@Override
		protected int messageLength() {
			int payloadSize = FormatUtil.toWMtpString(clientId).length;
			payloadSize += FormatUtil.toWMtpString(willTopic).length;
			payloadSize += FormatUtil.toWMtpString(will).length;
			payloadSize += FormatUtil.toWMtpString(username).length;
			payloadSize += FormatUtil.toWMtpString(password).length;
			return payloadSize + CONNECT_HEADER_SIZE;
		}

		@Override
		protected void readMessage(InputStream in, int msgLength) throws IOException {
			DataInputStream dis = new DataInputStream(in);
			protocolId = dis.readUTF();
			protocolVersion = dis.readByte();
			byte cFlags = dis.readByte();
			hasUsername = (cFlags & 0x80) > 0;
			hasPassword = (cFlags & 0x40) > 0;
			retainWill = (cFlags & 0x20) > 0;
			willQoS = QoS.valueOf(cFlags >> 3 & 0x03);
			hasWill = (cFlags & 0x04) > 0;
			cleanSession = (cFlags & 0x20) > 0;
			keepAlive = dis.read() * 256 + dis.read();
			clientId = dis.readUTF();
			if (hasWill) {
				willTopic = dis.readUTF();
				will = dis.readUTF();
			}
			if (hasUsername) {
				try {
					username = dis.readUTF();
				} catch (EOFException e) {
					// ignore
				}
			}
			if (hasPassword) {
				try {
					password = dis.readUTF();
				} catch (EOFException e) {
					// ignore
				}
			}

		}

		@Override
		protected void writeMessage(OutputStream out) throws IOException {
			DataOutputStream dos = new DataOutputStream(out);
			dos.writeUTF(protocolId);
			dos.write(protocolVersion);
			int flags = cleanSession ? 2 : 0;
			flags |= hasWill ? 0x04 : 0;
			flags |= willQoS == null ? 0 : willQoS.val << 3;
			flags |= retainWill ? 0x20 : 0;
			flags |= hasPassword ? 0x40 : 0;
			flags |= hasUsername ? 0x80 : 0;
			dos.write((byte) flags);
			dos.writeChar(keepAlive);

			dos.writeUTF(clientId);
			if (hasWill) {
				dos.writeUTF(willTopic);
				dos.writeUTF(will);
			}
			if (hasUsername) {
				dos.writeUTF(username);
			}
			if (hasPassword) {
				dos.writeUTF(password);
			}

			dos.flush();
		}

		public void setCredentials(String username) {
			setCredentials(username, null);
		}

		public void setCredentials(String username, String password) {

			if ((username == null || username.isEmpty()) && (password != null && !password.isEmpty())) {
				throw new IllegalArgumentException("It is not valid to supply a password without supplying a username.");
			}

			this.username = username;
			this.password = password;
			hasUsername = this.username != null;
			hasPassword = this.password != null;

		}

		public void setWill(String willTopic, String will) {
			setWill(willTopic, will, QoS.AT_MOST_ONCE, false);
		}

		public void setWill(String willTopic, String will, QoS willQoS, boolean retainWill) {
			if ((willTopic == null ^ will == null) || (will == null ^ willQoS == null)) {
				throw new IllegalArgumentException("Can't set willTopic, will or willQoS value independently");
			}

			this.willTopic = willTopic;
			this.will = will;
			this.willQoS = willQoS;
			this.retainWill = retainWill;
			this.hasWill = willTopic != null;
		}

		@Override
		public void setDup(boolean dup) {
			throw new UnsupportedOperationException("CONNECT messages don't use the DUP flag.");
		}

		@Override
		public void setRetained(boolean retain) {
			throw new UnsupportedOperationException("CONNECT messages don't use the RETAIN flag.");
		}

		@Override
		public void setQos(QoS qos) {
			throw new UnsupportedOperationException("CONNECT messages don't use the QoS flags.");
		}

		public String getProtocolId() {
			return protocolId;
		}

		public byte getProtocolVersion() {
			return protocolVersion;
		}

		public String getClientId() {
			return clientId;
		}

		public int getKeepAlive() {
			return keepAlive;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		public boolean isCleanSession() {
			return cleanSession;
		}

		public String getWillTopic() {
			return willTopic;
		}

		public String getWill() {
			return will;
		}

		public QoS getWillQoS() {
			return willQoS;
		}

		public boolean isWillRetained() {
			return retainWill;
		}

		public boolean hasUsername() {
			return hasUsername;
		}

		public boolean hasPassword() {
			return hasPassword;
		}

		public boolean hasWill() {
			return hasWill;
		}

	}

	public static class DisconnectMessage extends Message {
		public final static int MESSAGE_LENGTH = 2;

		public enum DisconnectionStatus {
			RECONNECT,

			OTHER_DEVICE_LOGIN,

			CLOSURE
		}

		private DisconnectionStatus status;

		public DisconnectMessage(Header header) throws IOException {
			super(header);
		}

		public DisconnectMessage(DisconnectionStatus status) {
			super(Type.DISCONNECT);
			if (status == null) {
				throw new IllegalArgumentException("The status of ConnAskMessage can't be null");
			}
			this.status = status;
		}

		public DisconnectMessage() {
			super(Type.DISCONNECT);
		}

		@Override
		protected int messageLength() {
			return MESSAGE_LENGTH;
		}

		@Override
		protected void readMessage(InputStream in, int msgLength) throws IOException {
			// Ignore first byte
			in.read();
			int result = in.read();
			switch (result) {
			case 0:
				status = DisconnectionStatus.RECONNECT;
				break;
			case 1:
				status = DisconnectionStatus.OTHER_DEVICE_LOGIN;
				break;
			case 2:
				status = DisconnectionStatus.CLOSURE;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported CONNACK code: " + result);
			}
		}

		@Override
		protected void writeMessage(OutputStream out) throws IOException {
			out.write(0x00);
			switch (status) {
			case RECONNECT:
				out.write(0x00);
				break;
			case OTHER_DEVICE_LOGIN:
				out.write(0x01);
				break;
			case CLOSURE:
				out.write(0x02);
				break;
			default:
				throw new UnsupportedOperationException("Unsupported CONNACK code: " + status);
			}
		}

		public DisconnectionStatus getStatus() {
			return status;
		}

		@Override
		public void setDup(boolean dup) {
			throw new UnsupportedOperationException("DISCONNECT message does not support the DUP flag");
		}

		@Override
		public void setQos(QoS qos) {
			throw new UnsupportedOperationException("DISCONNECT message does not support the QoS flag");
		}

		@Override
		public void setRetained(boolean retain) {
			throw new UnsupportedOperationException("DISCONNECT message does not support the RETAIN flag");
		}

	}

	public static abstract class Message {
		// @formatter:off
		public enum Type {
			CONNECT(1), CONNACK(2), PUBLISH(3), PUBACK(4), QUERY(5), // PUBREC
			QUERYACK(6), // PUBREL
			QUERYCON(7), // PUBCOMP
			SUBSCRIBE(8), SUBACK(9), UNSUBSCRIBE(10), UNSUBACK(11), PINGREQ(12), PINGRESP(13), DISCONNECT(14);

			final private int val;

			Type(int val) {
				this.val = val;
			}

			static Type valueOf(int i) {
				for (Type t : Type.values()) {
					if (t.val == i)
						return t;
				}
				return null;
			}
		}

		public static class Header {

			private Type type;
			private boolean retain;
			private QoS qos = QoS.AT_MOST_ONCE;
			private boolean dup;

			private Header(Type type, boolean retain, QoS qos, boolean dup) {
				this.type = type;
				this.retain = retain;
				this.qos = qos;
				this.dup = dup;
			}

			public Header(byte flags) {
				retain = (flags & 1) > 0;
				qos = QoS.valueOf((flags & 0x6) >> 1);
				dup = (flags & 8) > 0;
				type = Type.valueOf((flags >> 4) & 0xF);
			}

			public Type getType() {
				return type;
			}

			private byte encode() {
				byte b = 0;
				b = (byte) (type.val << 4);
				b |= retain ? 1 : 0;
				b |= qos.val << 1;
				b |= dup ? 8 : 0;
				return b;
			}

			@Override
			public String toString() {
				return "Header [type=" + type + ", retain=" + retain + ", qos=" + qos + ", dup=" + dup + "]";
			}

		}

		private final Header header;
		private byte headerCode;

		public Message(Type type) {
			header = new Header(type, false, QoS.AT_MOST_ONCE, false);
		}

		public Message(Header header) throws IOException {
			this.header = header;
		}

		final void read(InputStream in) throws IOException {
			int msgLength = readMsgLength(in);
			readMessage(in, msgLength);
		}

		public final void write(OutputStream out) throws IOException {
			this.headerCode = header.encode();
			out.write(headerCode);
			writeMsgCode(out);
			writeMsgLength(out);
			writeMessage(out);
		}

		private int readMsgLength(InputStream in) throws IOException {
			int msgLength = 0;
			int multiplier = 1;
			int digit;
			do {
				digit = in.read();
				msgLength += (digit & 0x7f) * multiplier;
				multiplier *= 128;
			} while ((digit & 0x80) > 0);
			return msgLength;
		}

		private void writeMsgLength(OutputStream out) throws IOException {
			int val = messageLength();

			do {
				byte b = (byte) (val & 0x7F);
				val >>= 7;
				if (val > 0) {
					b |= 0x80;
				}
				out.write(b);
			} while (val > 0);
		}

		private void writeMsgCode(OutputStream out) throws IOException {
			int val = messageLength();
			int code = this.headerCode;

			do {
				byte b = (byte) (val & 0x7F);
				val >>= 7;
				if (val > 0) {
					b |= 0x80;
				}
				code = code ^ b;
			} while (val > 0);

			out.write(code);
		}

		public final byte[] toBytes() {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				write(baos);
			} catch (IOException e) {
			}
			return baos.toByteArray();
		}

		protected int messageLength() {
			return 0;
		}

		protected void writeMessage(OutputStream out) throws IOException {
		}

		protected void readMessage(InputStream in, int msgLength) throws IOException {

		}

		public void setRetained(boolean retain) {
			header.retain = retain;
		}

		public boolean isRetained() {
			return header.retain;
		}

		public void setQos(QoS qos) {
			header.qos = qos;
		}

		public QoS getQos() {
			return header.qos;
		}

		public void setDup(boolean dup) {
			header.dup = dup;
		}

		public boolean isDup() {
			return header.dup;
		}

		public Type getType() {
			return header.type;
		}
	}

	public static class MessageInputStream implements Closeable {

		private InputStream in;

		public MessageInputStream(InputStream in) {
			this.in = in;
		}

		public Message readMessage() throws IOException {
			byte flags = (byte) in.read();
//			Log.d("flag", "flag is ==== " + flags);
			Header header = new Header(flags);
			Message msg = null;
			if (header.getType() == null) {
				return null;
			}
			switch (header.getType()) {
			case CONNACK:
				msg = new ConnAckMessage(header);
				break;
			case PUBLISH:
				msg = new PublishMessage(header);
				break;

			case PINGRESP:
				msg = new PingRespMessage(header);
				break;
			case CONNECT:
				msg = new ConnectMessage(header);
				break;
			case PINGREQ:
				msg = new PingReqMessage(header);
				break;
			case DISCONNECT:
				msg = new DisconnectMessage(header);
				break;
			default:
				throw new UnsupportedOperationException("No support for deserializing " + header.getType() + " messages");
			}
			in.read();
			msg.read(in);
			return msg;
		}

		public void close() throws IOException {
			in.close();
		}
	}

	public static class MessageOutputStream {

		private final OutputStream out;

		public MessageOutputStream(OutputStream out) {
			this.out = out;
		}

		public void writeMessage(Message msg) throws IOException {
			msg.write(out);
			out.flush();
		}

	}

	public static class PingReqMessage extends Message {

		public PingReqMessage() {
			super(Type.PINGREQ);
		}

		public PingReqMessage(Header header) throws IOException {
			super(header);
		}

		@Override
		public void setDup(boolean dup) {
			throw new UnsupportedOperationException("PINGREQ message does not support the DUP flag");
		}

		@Override
		public void setQos(QoS qos) {
			throw new UnsupportedOperationException("PINGREQ message does not support the QoS flag");
		}

		@Override
		public void setRetained(boolean retain) {
			throw new UnsupportedOperationException("PINGREQ message does not support the RETAIN flag");
		}

	}

	public static class PingRespMessage extends Message {

		public PingRespMessage(Header header) throws IOException {
			super(header);
		}

		public PingRespMessage() {
			super(Type.PINGRESP);
		}
	}

	public static class PublishMessage extends RetryableMessage {

		private String topic;
		private byte[] data;
		private String targetId;
		private int date;

		public PublishMessage(Header header) throws IOException {
			super(header);
		}

		@Override
		protected int messageLength() {
			return 0;
		}

		@Override
		protected void writeMessage(OutputStream out) throws IOException {
			super.writeMessage(out);
		}

		@Override
		protected void readMessage(InputStream in, int msgLength) throws IOException {
			int pos = 14;
			DataInputStream dis = new DataInputStream(in);
			dis.readLong();
			date = dis.readInt();
			topic = dis.readUTF();
			targetId = dis.readUTF();
			pos += FormatUtil.toWMtpString(topic).length;
			pos += FormatUtil.toWMtpString(targetId).length;
			super.readMessage(in, msgLength);
			data = new byte[msgLength - pos];
			dis.read(data);
		}

		public String getTopic() {
			return topic;
		}

		public byte[] getData() {
			return data;
		}

		public int getServerTime() {
			return this.date;
		}

		public String getTargetId() {
			return targetId;
		}

		public String getDataAsString() {
			return FormatUtil.toString(data);
		}

	}

	public static enum QoS {
		AT_MOST_ONCE(0), AT_LEAST_ONCE(1), EXACTLY_ONCE(2), DEFAULT(3);

		final public int val;

		QoS(int val) {
			this.val = val;
		}

		static QoS valueOf(int i) {
			for (QoS q : QoS.values()) {
				if (q.val == i)
					return q;
			}
			throw new IllegalArgumentException("Not a valid QoS number: " + i);
		}
	}

	public static abstract class RetryableMessage extends Message {

		private int messageId;

		public RetryableMessage(Header header) throws IOException {
			super(header);
		}

		public RetryableMessage(Type type) {
			super(type);
		}

		@Override
		protected int messageLength() {
			return 2;
		}

		@Override
		protected void writeMessage(OutputStream out) throws IOException {
			int id = getMessageId();
			int lsb = id & 0xFF;
			int msb = (id & 0xFF00) >> 8;
			out.write(msb);
			out.write(lsb);
		}

		@Override
		protected void readMessage(InputStream in, int msgLength) throws IOException {
			int msgId = in.read() * 0xFF + in.read();
			setMessageId(msgId);
		}

		public void setMessageId(int messageId) {
			this.messageId = messageId;
		}

		public int getMessageId() {
			return messageId;
		}

	}

	public static class FormatUtil {

		public static String dumpByteArray(byte[] bytes) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				byte b = bytes[i];
				int iVal = b & 0xFF;
				int byteN = Integer.parseInt(Integer.toBinaryString(iVal));
				sb.append(String.format("%1$02d: %2$08d %3$1c %3$d\n", i, byteN, iVal));
			}
			return sb.toString();
		}

		public static byte[] toWMtpString(String s) {
			if (s == null) {
				return new byte[0];
			}
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(byteOut);
			try {
				dos.writeUTF(s);
				dos.flush();
			} catch (IOException e) {
				// SHould never happen;
				return new byte[0];
			}
			return byteOut.toByteArray();
		}

		public static String toString(byte[] data) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bais);
			try {
				return dis.readUTF();
			} catch (IOException e) {
			}
			return null;
		}
	}

}
