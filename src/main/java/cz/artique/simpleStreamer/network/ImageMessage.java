package cz.artique.simpleStreamer.network;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

public class ImageMessage extends AbstractMessage {

	private byte[] data;
	
	public ImageMessage(byte[] data) {
		this();
		this.data = data;
	}

	public ImageMessage() {
		super("image");
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject asJSONObject() {
		String dataString = Base64.encodeBase64String(getData());

		JSONObject jsonObject = super.asJSONObject();
		jsonObject.put("data", dataString);
		return jsonObject;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
