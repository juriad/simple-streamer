package cz.artique.simpleStreamer.frontend;

import java.net.InetAddress;

public interface DisplayerListener {
	void applicationClosing();

	void newPeer(InetAddress hostname, int port);
}
