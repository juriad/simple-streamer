package cz.artique.simpleStreamer.backend.server;

import java.net.InetAddress;

public interface PeerInitializerCallback {
	void onSuccess(InetAddress hostname, int port);

	void onError(InetAddress hostname, int port, Throwable t);
}
