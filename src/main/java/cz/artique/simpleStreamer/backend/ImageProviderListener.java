package cz.artique.simpleStreamer.backend;

import java.util.EventListener;

public interface ImageProviderListener extends EventListener {
	void stateChanged(ImageProvider provider);

	void imageAvailable(ImageProvider provider);

	void error(ImageProvider provider);
}
