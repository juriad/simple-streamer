package cz.artique.simpleStreamer.backend.cam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.AbstractImageProvider;

public abstract class AbstractWebcamReader extends AbstractImageProvider
		implements WebCamReader, Runnable {
	static final Logger logger = LogManager
			.getLogger(AbstractWebcamReader.class.getName());

	private int width;
	private int height;
	private int rate;

	public AbstractWebcamReader(String name, int width, int height, int rate) {
		super(name);
		this.width = width;
		this.height = height;
		this.rate = rate;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

}
