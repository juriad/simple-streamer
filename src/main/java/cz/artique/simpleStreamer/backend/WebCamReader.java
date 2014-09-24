package cz.artique.simpleStreamer.backend;

import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;

import cz.artique.simpleStreamer.Crate;

public class WebCamReader extends AbstractImageProvider implements Runnable {

	private OpenIMAJGrabber grabber;
	private int width;
	private int height;
	private Device device;
	private int fps;

	public WebCamReader(Crate myCrate, int width, int height, int fps) {
		super(myCrate);
		this.width = width;
		this.height = height;
		this.fps = fps;

		grabber = new OpenIMAJGrabber();
		device = grabber.getVideoDevices().get().getDevice(0).get();
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
		boolean started = grabber.startSession(getWidth(), getHeight(), fps,
				Pointer.pointerTo(device));
		if (!started) {
			throw new RuntimeException("Not able to start native grabber!");
		}
		setState(ImagePrioviderState.INITIALIZED);

		while (!isEnd()) {
			/* Get a frame from the webcam. */
			grabber.nextFrame();
			/* Get the raw bytes of the frame. */
			byte[] rawImage = grabber.getImage().getBytes(160 * 120 * 3);
			crate.setImage(rawImage);
			fireImageAvailable();
			setState(ImagePrioviderState.RUNNING);
		}
		setState(ImagePrioviderState.OBSOLETE);
		grabber.stopSession();
	}

}
