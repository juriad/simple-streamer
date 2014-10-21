package cz.artique.simpleStreamer.compression;

public class CompressorException extends Exception {

	private static final long serialVersionUID = 1L;

	public CompressorException() {
	}

	public CompressorException(String message) {
		super(message);
	}

	public CompressorException(Throwable cause) {
		super(cause);
	}

	public CompressorException(String message, Throwable cause) {
		super(message, cause);
	}

	public CompressorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
