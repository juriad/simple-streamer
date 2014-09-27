package cz.artique.simpleStreamer.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MessageHandler {
	static final Logger logger = LogManager.getLogger(MessageHandler.class
			.getName());

	private MessageFactory messageFactory;

	private BufferedReader is;

	private BufferedWriter os;

	private Socket socket;

	public MessageHandler(Socket socket) throws IOException {
		this.socket = socket;
		is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		os = new BufferedWriter(
				new OutputStreamWriter(socket.getOutputStream()));
		messageFactory = new MessageFactory();
	}

	private String readMessage() throws IOException {
		StringBuilder sb = new StringBuilder();
		int c;
		do {
			c = is.read();
			if (c == -1) {
				logger.error(this
						+ " Stream end reached before message end found.");
				throw new IOException("Stream ended before the end of packet");
			}
			sb.append((char) c);
		} while (c != '}');
		return sb.toString();
	}

	public Message receiveMessage() throws MalformedMessageException,
			IOException {
		String input = readMessage();
		logger.debug(this + " Received: " + input);
		Object object = JSONValue.parse(input);
		if (object == null || !(object instanceof JSONObject)) {
			throw new MalformedMessageException(
					"Packet content is not a valid JSON object.");
		}
		JSONObject jsonMessage = (JSONObject) object;
		Message message = messageFactory.parseMessage(jsonMessage);
		logger.info(this + " Received message of type: " + message.getType());
		return message;
	}

	public void sendMessage(Message message) throws IOException {
		JSONObject obj = message.asJSONObject();
		String jsonString = obj.toJSONString();
		logger.info(this + " Sending message of type: " + message.getType());
		logger.debug(this + " Sending:" + jsonString);
		os.write(jsonString);
	}

	public void close() {
		logger.info(this + " Closing socket.");
		try {
			socket.close();
		} catch (IOException e) {
			logger.error("Failed to close socket.", e);
		}
	}

	@Override
	public String toString() {
		return "Message handler for " + socket.getInetAddress().getHostName()
				+ ":" + socket.getPort();
	}
}
