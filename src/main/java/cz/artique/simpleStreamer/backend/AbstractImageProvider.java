package cz.artique.simpleStreamer.backend;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.interconnect.Crate;

public abstract class AbstractImageProvider implements ImageProvider {
	static final Logger logger = LogManager
			.getLogger(AbstractImageProvider.class.getName());

	private boolean end;
	protected Crate crate;
	private ImageProviderState state = ImageProviderState.UNINITIALIZED;
	private List<ImageProviderListener> listeners;
	private String name;

	public AbstractImageProvider(String name) {
		this.name = name;
		this.crate = new Crate();
		listeners = new ArrayList<ImageProviderListener>();
	}

	@Override
	public Crate getCrate() {
		return crate;
	}

	@Override
	public ImageProviderState getState() {
		return state;
	}

	protected void setState(ImageProviderState state) {
		if (!this.state.equals(state)) {
			this.state = state;
			fireStateChanged();
		}
	}

	@Override
	public synchronized void terminate() {
		logger.info(this + " Terminate called.");
		end = true;
	}

	protected synchronized boolean isEnd() {
		return end;
	}

	@Override
	public synchronized void addImageProviderListener(ImageProviderListener l) {
		logger.info(this + " Added listener " + l + ".");
		listeners.add(l);
	}

	protected synchronized void fireStateChanged() {
		logger.info(this + " State changed to " + getState()
				+ "; notifying listeners.");
		for (ImageProviderListener l : listeners) {
			l.stateChanged(this);
		}
	}

	protected synchronized void fireImageAvailable() {
		logger.debug(this + " A new image number " + getCrate().getImageNumber()
				+ " is available.");
		for (ImageProviderListener l : listeners) {
			l.imageAvailable(this);
		}
	}
	
	protected synchronized void fireError() {
		for (ImageProviderListener l : listeners) {
			l.error(this);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

}
