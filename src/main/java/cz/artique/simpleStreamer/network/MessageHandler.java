package cz.artique.simpleStreamer.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MessageHandler {
	static final Logger logger = LogManager.getLogger(MessageHandler.class
			.getName());

	private MessageFactory messageFactory;

	private BufferedInputStream is;

	private BufferedOutputStream os;

	public MessageHandler(Socket socket) throws IOException {
		is = new BufferedInputStream(socket.getInputStream());
		os = new BufferedOutputStream(socket.getOutputStream());
		messageFactory = new MessageFactory();
	}

	private String readMessage() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int c;
		do {
			c = is.read();
			buffer.write(c);
		} while (c != '}');
		return buffer.toString("UTF-8");
	}

	public Message receiveMessage() throws MalformedMessageException,
			IOException {
		String input = readMessage();
		Object object = JSONValue.parse(input);
		if (object == null || !(object instanceof JSONObject)) {
			throw new MalformedMessageException(
					"Packet content is not a valid JSON object.");
		}
		JSONObject message = (JSONObject) object;
		return messageFactory.parseMessage(message);
	}

	public void sendMessage(Message message) throws IOException {
		JSONObject obj = message.asJSONObject();
		String jsonString = obj.toJSONString();
		logger.info("Sending:" + jsonString);
		byte[] buf = jsonString.getBytes();
		os.write(buf);
	}
}
