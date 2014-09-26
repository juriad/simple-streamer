package cz.artique.simpleStreamer.network;

import org.json.simple.JSONObject;

import cz.artique.simpleStreamer.compression.ImageFormat;

public class StartStreamMessage extends AbstractMessage {

	private ImageFormat format;

	private int width;

	private int height;
	
	public StartStreamMessage(ImageFormat format, int width, int height) {
		this();
		this.format = format;
		this.width = width;
		this.height = height;
	}

	public StartStreamMessage() {
		super("startstream");
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject asJSONObject() {
		JSONObject jsonObject = super.asJSONObject();
		jsonObject.put("format", format.toString());
		jsonObject.put("width", width);
		jsonObject.put("height", height);
		return jsonObject;
	}

	public ImageFormat getFormat() {
		return format;
	}

	public void setFormat(ImageFormat format) {
		this.format = format;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
