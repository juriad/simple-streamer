package cz.artique.simpleStreamer.backend.cam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;

import cz.artique.simpleStreamer.backend.ImageProviderState;

public class RealWebCamReader extends AbstractWebcamReader {
	static final Logger logger = LogManager.getLogger(RealWebCamReader.class
			.getName());

	private Webcam webcam;

	public RealWebCamReader(int width, int height, int rate) {
		super("Local webcam", width, height, rate);

		webcam = Webcam.getDefault();
		logger.info("Found a real webcam " + webcam.getName());
		webcam.setViewSize(new Dimension(width, height));
		if (webcam.isOpen()) {
			throw new WebcamException("Webcam is already in use.");
		}

		Thread reader = new Thread(this);
		reader.start();
		logger.info("Dummy webcam reader's thread started.");
	}

	@Override
	public void run() {
		webcam.open();
		setState(ImageProviderState.INITIALIZED);

		while (!isEnd()) {
			BufferedImage image = webcam.getImage();
			crate.setImage(image);
			fireImageAvailable();
			setState(ImageProviderState.RUNNING);
			try {
				Thread.sleep(getRate());
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		setState(ImageProviderState.OBSOLETE);
		webcam.close();
	}

}
