package cz.artique.simpleStreamer.interconnect;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

public class CrateImage {

	int number;

	byte[] rawImage;

	BufferedImage bufferedImage;

	private int width;

	private int height;

	public CrateImage(int number, byte[] data, int width, int height) {
		this.number = number;
		rawImage = data;
		this.width = width;
		this.height = height;
	}

	public CrateImage(int number, BufferedImage image) {
		this.number = number;
		bufferedImage = image;
		width = image.getWidth();
		height = image.getHeight();
	}

	public int getNumber() {
		return number;
	}

	public byte[] getRawImage() {
		if (rawImage == null) {
			DataBuffer buffer = bufferedImage.getRaster().getDataBuffer();
			DataBufferByte dataBuffer = (DataBufferByte) buffer;
			rawImage = dataBuffer.getData();
		}
		return rawImage;
	}

	private int[] toIntArray(byte[] barr) {
		int[] result = new int[barr.length];
		for (int i = 0; i < barr.length; i++) {
			result[i] = barr[i];
		}
		return result;
	}

	public BufferedImage getBufferedImage() {
		if (bufferedImage == null) {
			bufferedImage = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_3BYTE_BGR);
			WritableRaster raster = bufferedImage.getRaster();
			raster.setPixels(0, 0, getWidth(), getHeight(),
					toIntArray(rawImage));
		}
		return bufferedImage;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
