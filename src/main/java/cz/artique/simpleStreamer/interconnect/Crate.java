package cz.artique.simpleStreamer.interconnect;

import java.awt.image.BufferedImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Crate {
	static final Logger logger = LogManager.getLogger(Crate.class.getName());

	private CrateImage image = null;

	public Crate() {
	}

	public synchronized void setImage(CrateImage image) {
		this.image = image;
		this.notifyAll();
	}

	public synchronized void setImage(BufferedImage image) {
		CrateImage crateImage = new CrateImage(getImageNumber() + 1, image);
		setImage(crateImage);
	}

	public synchronized void setImage(byte[] image, int width, int height) {
		CrateImage crateImage = new CrateImage(getImageNumber() + 1, image,
				width, height);
		setImage(crateImage);
	}

	public synchronized CrateImage getImage(int greaterThan) {
		while (getImageNumber() <= greaterThan) {
			logger.debug("Waiting for an image with number higher than "
					+ greaterThan);
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		return image;
	}

	public synchronized int getImageNumber() {
		if (image == null) {
			return -1;
		}
		return image.getNumber();
	}

}
