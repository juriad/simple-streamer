package cz.artique.simpleStreamer.network;

import org.json.simple.JSONObject;

public interface Message {
	JSONObject asJSONObject();

	String getType();
}
