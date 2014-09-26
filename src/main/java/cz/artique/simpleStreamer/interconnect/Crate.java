package cz.artique.simpleStreamer.interconnect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Crate {
	static final Logger logger = LogManager.getLogger(Crate.class.getName());

	private int imageNumber = -1;

	private byte[] image = null;

	public Crate() {
	}

	public synchronized void setImage(byte[] image) {
		this.image = image;
		imageNumber++;
		this.notifyAll();
	}

	public synchronized byte[] getImage(int greaterThan) {
		while (getImageNumber() <= greaterThan) {
			logger.info("Waiting for an image with number higher than "
					+ greaterThan);
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		return image;
	}

	public synchronized int getImageNumber() {
		return imageNumber;
	}

}
