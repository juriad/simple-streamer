package cz.artique.simpleStreamer.backend.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.ImageProviderListener;
import cz.artique.simpleStreamer.backend.ImageProviderState;
import cz.artique.simpleStreamer.backend.Peer;
import cz.artique.simpleStreamer.compression.ImageFormat;
import cz.artique.simpleStreamer.interconnect.CleverList;

public class PeerInitializer {
	static final Logger logger = LogManager.getLogger(PeerInitializer.class
			.getName());
	private CleverList<ImageProvider> imageProviders;
	private int rate;
	private ImageProvider localProvider;

	public PeerInitializer() {
	}

	public PeerInitializer(CleverList<ImageProvider> imageProviders, int rate,
			ImageProvider localProvider) {
		this.imageProviders = imageProviders;
		this.rate = rate;
		this.localProvider = localProvider;
	}

	private void watchState(ImageProvider provider) {
		provider.addImageProviderListener(new ImageProviderListener() {
			@Override
			public void stateChanged(ImageProvider provider) {
				end(provider);
			}

			@Override
			public void imageAvailable(ImageProvider provider) {
				// ignore
			}

			@Override
			public void error(ImageProvider provider) {
				end(provider);
			}

			private void end(ImageProvider provider) {
				// this makes sure that as soon as the peer becomes
				// obsolete, it will be removed from the lists.
				if (ImageProviderState.OBSOLETE.equals(provider.getState())) {
					logger.info("Provider " + provider
							+ " became obsolete; removing it from the list.");
					imageProviders.remove(provider);
				}
			}
		});
	}

	public void createPeerFromSocket(Socket socket) throws IOException {
		Peer peer = new Peer(socket, localProvider, ImageFormat.RAW, rate);
		watchState(peer);
		logger.info("Adding peer " + peer + " into the list.");
		imageProviders.add(peer);
	}

	public void createPeer(final InetAddress host, final int port,
			final PeerInitializerCallback callback) {
		new Thread() {
			int timeout = 5000;
			Socket socket = null;

			public void run() {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress(host, port), timeout);
				} catch (IOException e) {
					logger.error(
							"Could not connect to host " + host.getHostName()
									+ " and port " + port, e);

					try {
						if (socket != null) {
							socket.close();
						}
					} catch (Exception e2) {
						logger.error("Failed to close socket", e2);
					}

					logger.info("Will skip this peer.");
					if (callback != null) {
						callback.onError(host, port, e);
					}
					return;
				}

				try {
					createPeerFromSocket(socket);
				} catch (Exception e) {
					logger.error(
							"Could not create peer for " + host.getHostName()
									+ " and port " + port, e);
					logger.info("Will skip this peer.");
					if (callback != null) {
						callback.onError(host, port, e);
					}
				}
				if (callback != null) {
					callback.onSuccess(host, port);
				}
			}
		}.start();
	}

	public void createPeers(List<InetAddress> remoteHosts,
			List<Integer> remotePorts, PeerInitializerCallback callback) {
		int count = remoteHosts.size() < remotePorts.size() ? remoteHosts
				.size() : remotePorts.size();
		if (remoteHosts.size() != remotePorts.size()) {
			logger.warn("The number of remotes does not equal to number of ports given.");
		}
		logger.info("Will connect to " + count + " peers.");

		logger.info("Creating peers.");
		for (int i = 0; i < count; i++) {
			InetAddress host = remoteHosts.get(i);
			Integer port = remotePorts.get(i);
			createPeer(host, port, callback);
		}
	}
}
