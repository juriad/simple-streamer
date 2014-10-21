package cz.artique.simpleStreamer.compression;

public interface Compressor {
	byte[] compress(byte[] input) throws CompressorException;

	byte[] uncompress(byte[] input) throws CompressorException;

	ImageFormat getFormat();
}
