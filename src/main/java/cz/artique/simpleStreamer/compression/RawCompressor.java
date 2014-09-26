package cz.artique.simpleStreamer.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RawCompressor implements Compressor {
	static final Logger logger = LogManager.getLogger(RawCompressor.class
			.getName());

	public RawCompressor() {
	}

	@Override
	public byte[] compress(byte[] input) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
					byteArrayOutputStream);
			gzipOutputStream.write(input);
			gzipOutputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.debug("Compression ratio %f\n",
				(1.0f * input.length / byteArrayOutputStream.size()));
		return byteArrayOutputStream.toByteArray();
	}

	@Override
	public byte[] uncompress(byte[] input) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(input)),
					out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return out.toByteArray();
	}

	@Override
	public ImageFormat getFormat() {
		return ImageFormat.RAW;
	}

}
