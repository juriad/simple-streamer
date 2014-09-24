package cz.artique.simpleStreamer.backend;

import java.util.ArrayList;
import java.util.List;

import cz.artique.simpleStreamer.Crate;

public abstract class AbstractImageProvider implements ImageProvider {

	private boolean end;
	protected Crate crate;
	private ImagePrioviderState state = ImagePrioviderState.UNINITIALIZED;
	private List<ImageProviderListener> listeners;

	public AbstractImageProvider(Crate myCrate) {
		this.crate = myCrate;
		listeners = new ArrayList<ImageProviderListener>();
	}

	@Override
	public Crate getCrate() {
		return crate;
	}

	@Override
	public ImagePrioviderState getState() {
		return state;
	}

	protected void setState(ImagePrioviderState state) {
		if (!this.state.equals(state)) {
			this.state = state;
			fireStateChanged();
		}
	}

	@Override
	public synchronized void terminate() {
		end = true;
	}

	protected synchronized boolean isEnd() {
		return end;
	}

	@Override
	public synchronized void addImageProviderListener(ImageProviderListener l) {
		listeners.add(l);
	}

	protected void fireStateChanged() {
		for (ImageProviderListener l : listeners) {
			l.stateChanged(this);
		}
	}

	protected void fireImageAvailable() {
		for (ImageProviderListener l : listeners) {
			l.imageAvailable(this);
		}
	}

}
