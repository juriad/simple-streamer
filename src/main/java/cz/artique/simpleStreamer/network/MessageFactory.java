package cz.artique.simpleStreamer.network;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

import cz.artique.simpleStreamer.compression.ImageFormat;

public class MessageFactory {

	private byte[] parseData(JSONObject object)
			throws MalformedMessageException {
		Object dataObj = object.get("data");
		if (dataObj == null || !(dataObj instanceof String)) {
			throw new MalformedMessageException(
					"Missing or wrong type of data attribute");
		}
		String dataString = (String) dataObj;
		byte[] data = Base64.decodeBase64(dataString);
		return data;
	}

	private int parsePositiveInt(JSONObject object, String attribute)
			throws MalformedMessageException {
		Object longObj = object.get(attribute);
		if (longObj == null || !(longObj instanceof Long)) {
			throw new MalformedMessageException("Missing or wrong type of "
					+ attribute + " attribute");
		}

		int intValue = ((Long) longObj).intValue();
		if (intValue <= 0) {
			throw new MalformedMessageException(attribute
					+ "cannot be negative");
		}
		return intValue;
	}

	private ImageFormat parseFormat(JSONObject object)
			throws MalformedMessageException {
		Object formatObj = object.get("format");
		if (formatObj == null || !(formatObj instanceof String)) {
			throw new MalformedMessageException(
					"Missing or wrong type of format attribute");
		}

		String formatString = (String) formatObj;
		try {
			return ImageFormat.fromString(formatString);
		} catch (Exception e) {
			throw new MalformedMessageException("Wrong image format");
		}
	}

	public Message parseMessage(JSONObject object)
			throws MalformedMessageException {
		Object typeObj = object.get("type");
		if (typeObj == null || !(typeObj instanceof String)) {
			throw new MalformedMessageException(
					"Missing or wrong type of message type attribute");
		}

		Message message;

		String type = (String) typeObj;
		if (type.equals("startstream")) {
			StartStreamMessage m = new StartStreamMessage();
			m.setFormat(parseFormat(object));
			m.setWidth(parsePositiveInt(object, "width"));
			m.setHeight(parsePositiveInt(object, "height"));
			message = m;
		} else if (type.equals("image")) {
			ImageMessage m = new ImageMessage();
			m.setData(parseData(object));
			message = m;
		} else if (type.equals("stopstream")) {
			StopStreamMessage m = new StopStreamMessage();
			message = m;
		} else {
			throw new MalformedMessageException("Unknown message type");
		}

		return message;
	}
}
