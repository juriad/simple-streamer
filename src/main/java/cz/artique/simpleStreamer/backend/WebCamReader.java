package cz.artique.simpleStreamer.backend;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import com.github.sarxos.webcam.Webcam;

public class WebCamReader extends AbstractImageProvider implements Runnable {

	private int width;
	private int height;
	private int rate;
	private Webcam webcam;

	public WebCamReader(int width, int height, int rate) {
		super("Local webcam");
		this.width = width;
		this.height = height;
		this.rate = rate;

		webcam = Webcam.getDefault();
		webcam.setViewSize(new Dimension(width, height));
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void run() {
		webcam.open();
		setState(ImagePrioviderState.INITIALIZED);

		while (!isEnd()) {
			BufferedImage image = webcam.getImage();
			DataBufferByte dataBuffer = (DataBufferByte) image.getRaster()
					.getDataBuffer();
			byte[] rawImage = dataBuffer.getData();
			crate.setImage(rawImage);
			fireImageAvailable();
			setState(ImagePrioviderState.RUNNING);
			try {
				Thread.sleep((long) (1000.0 / rate));
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		setState(ImagePrioviderState.OBSOLETE);
		webcam.close();
	}

}
