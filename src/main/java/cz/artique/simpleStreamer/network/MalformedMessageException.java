package cz.artique.simpleStreamer.network;

public class MalformedMessageException extends Exception {

	private static final long serialVersionUID = 1L;

	public MalformedMessageException(String msg) {
		super(msg);
	}

}
