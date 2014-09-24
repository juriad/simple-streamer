package cz.artique.simpleStreamer.network;

import org.json.simple.JSONObject;

public abstract class AbstractMessage implements Message {

	private final String type;

	public AbstractMessage(String type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject asJSONObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", getType());
		return jsonObject;
	}

	@Override
	public String getType() {
		return type;
	}
}
