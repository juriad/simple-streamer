package cz.artique.simpleStreamer.compression;

public interface Compressor {
	byte[] compress(byte[] input);

	byte[] uncompress(byte[] input);

	ImageFormat getFormat();
}
