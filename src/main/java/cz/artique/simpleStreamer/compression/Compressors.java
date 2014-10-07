package cz.artique.simpleStreamer.compression;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum Compressors {

	COMPRESSORS;

	static final Logger logger = LogManager.getLogger(Compressors.class
			.getName());

	private Map<ImageFormat, Compressor> compressors = new HashMap<ImageFormat, Compressor>();

	public void registerCompressor(Compressor compressor) {
		compressors.put(compressor.getFormat(), compressor);
		logger.info("Registered compressor for format "
				+ compressor.getFormat());
	}

	public Compressor getCompressor(ImageFormat format) {
		return compressors.get(format);
	}

}
