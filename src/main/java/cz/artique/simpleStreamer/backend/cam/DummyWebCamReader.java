package cz.artique.simpleStreamer.backend.cam;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImageProviderState;

public class DummyWebCamReader extends AbstractWebcamReader {
	static final Logger logger = LogManager
			.getLogger(DummyWebCamReader.class.getName());

	public DummyWebCamReader(int width, int height, int rate) {
		super("Dummy webcam", width, height, rate);

		Thread reader = new Thread(this);
		reader.start();
		logger.info("Dummy webcam reader's thread started.");
	}

	private BufferedImage paintImage(int index) {
		int width = getWidth();
		int height = getHeight();
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.BLUE);
		g2d.fillOval(0, 0, width, height);
		g2d.setColor(Color.YELLOW);
		g2d.setFont(g2d.getFont().deriveFont(30.0f));
		g2d.drawString("Dummy", width / 4, height / 4);
		g2d.setFont(g2d.getFont().deriveFont(20.0f));
		g2d.drawString(index + "", width / 2, height / 2);
		return image;
	}

	@Override
	public void run() {
		setState(ImageProviderState.INITIALIZED);

		while (!isEnd()) {
			BufferedImage image = paintImage(getCrate().getImageNumber() + 1);
			DataBufferByte dataBuffer = (DataBufferByte) image.getRaster()
					.getDataBuffer();
			byte[] rawImage = dataBuffer.getData();
			crate.setImage(rawImage);
			fireImageAvailable();
			setState(ImageProviderState.RUNNING);
			try {
				Thread.sleep(getRate());
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		setState(ImageProviderState.OBSOLETE);
	}
}
