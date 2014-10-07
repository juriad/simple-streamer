package cz.artique.simpleStreamer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.ImageProviderListener;
import cz.artique.simpleStreamer.backend.ImageProviderState;
import cz.artique.simpleStreamer.backend.Peer;
import cz.artique.simpleStreamer.backend.cam.DummyWebCamReader;
import cz.artique.simpleStreamer.backend.cam.RealWebCamReader;
import cz.artique.simpleStreamer.backend.cam.WebCamReader;
import cz.artique.simpleStreamer.compression.Compressors;
import cz.artique.simpleStreamer.compression.ImageFormat;
import cz.artique.simpleStreamer.compression.RawCompressor;
import cz.artique.simpleStreamer.frontend.Displayer;
import cz.artique.simpleStreamer.frontend.DisplayerListener;
import cz.artique.simpleStreamer.interconnect.CleverList;

public class App {
	static final Logger logger = LogManager.getLogger(App.class.getName());

	private CleverList<ImageProvider> imageProviders;

	private AppArgs arguments;

	private WebCamReader webCamReader;

	private boolean endListening = false;

	/**
	 * If any thread throws an exception, whole application should crash.
	 */
	private static void setupThreading() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error("A thread " + t.getName() + "(" + t.getId()
						+ ") threw an exception; killing whole application.", e);
				System.exit(1);
			}
		});
	}

	public App(AppArgs arguments) {
		this.arguments = arguments;
		setupCompressors();

		List<InetAddress> remoteHosts = arguments.getRemoteHosts();
		List<Integer> remotePorts = arguments.getRemotePorts();

		int count = remoteHosts.size() < remotePorts.size() ? remoteHosts
				.size() : remotePorts.size();
		if (remoteHosts.size() != remotePorts.size()) {
			logger.warn("The number of remotes does not equal to number of ports given.");
		}
		logger.info("Will connect to " + count + " peers.");
		imageProviders = new CleverList<ImageProvider>();
		webCamReader = createWebCamReader();

		logger.info("Scheduling GUI creation.");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Displayer(imageProviders)
						.addDisplayerListener(new DisplayerListener() {
							@Override
							public void applicationClosing() {
								endListening = true;
							}

							@Override
							public void newPeer(final InetAddress hostname,
									final int port) {
								logger.info("Adding new peer; this may take a while, preocessing in a new thread.");
								new Thread(new Runnable() {
									@Override
									public void run() {
										try {
											createPeer(hostname, port);
										} catch (Exception e) {
											logger.info("Ignoring this failed peer.");
										}
									}
								}).start();
								;
							}
						});
			}
		});

		createPeers(count, arguments.getRemoteHosts(),
				arguments.getRemotePorts());
	}

	private void setupCompressors() {
		Compressors.COMPRESSORS.registerCompressor(new RawCompressor());
	}

	private WebCamReader createWebCamReader() {
		int rate = 20; // wait between images (50FPS)

		WebCamReader webCamReader = null;

		if (!arguments.isDummy()) {
			try {
				webCamReader = new RealWebCamReader(arguments.getWidth(),
						arguments.getHeight(), rate);
				logger.info("Created a real webcam reader.");
			} catch (FileNotFoundException e) {
			}
		}

		if (webCamReader == null) {
			webCamReader = new DummyWebCamReader(arguments.getWidth(),
					arguments.getHeight(), rate);
			logger.info("Created a dummy webcam reader.");
		}

		imageProviders.add(webCamReader);
		watchState(webCamReader);

		return webCamReader;
	}

	private void watchState(ImageProvider provider) {
		provider.addImageProviderListener(new ImageProviderListener() {
			@Override
			public void stateChanged(ImageProvider provider) {
				// this makes sure that as soon as the peer becomes
				// obsolete, it will be removed from the lists.
				if (ImageProviderState.OBSOLETE.equals(provider.getState())) {
					logger.info("Provider " + provider
							+ " became obsolete; removing it from the list.");
					imageProviders.remove(provider);
				}
			}

			@Override
			public void imageAvailable(ImageProvider provider) {
				// ignore
			}
		});
	}

	private void createPeerFromSocket(Socket socket) throws IOException {
		try {
			Peer peer = new Peer(socket, webCamReader, ImageFormat.RAW,
					arguments.getRate());
			watchState(peer);
			logger.info("Adding peer " + peer + " into the list.");
			imageProviders.add(peer);
		} catch (Exception e) {
			logger.error("Could not create peer for "
					+ socket.getInetAddress().getHostName() + " and port "
					+ socket.getPort(), e);
			throw e;
		}
	}

	private void createPeer(InetAddress host, int port) throws IOException {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
		} catch (IOException e) {
			logger.error("Could not connect to host " + host.getHostName()
					+ " and port " + port, e);
			throw e;
		}
		createPeerFromSocket(socket);
	}

	private void createPeers(int count, List<InetAddress> remoteHosts,
			List<Integer> remotePorts) {
		logger.info("Creating peers.");
		for (int i = 0; i < count; i++) {
			InetAddress host = remoteHosts.get(i);
			Integer port = remotePorts.get(i);
			try {
				createPeer(host, port);
			} catch (Exception e) {
				logger.info("Will skip this connection and continue with others.");
			}
		}
	}

	private void listen() {
		ServerSocket serverSocket = null;
		try {
			try {
				serverSocket = new ServerSocket(arguments.getServerPort());
				serverSocket.setSoTimeout(100);
				logger.info("Created server socket for local port "
						+ arguments.getServerPort());
			} catch (IOException e) {
				logger.error("Could not create socket for local port "
						+ arguments.getServerPort(), e);
				logger.info("Killing the application.");
				throw new RuntimeException(e);
			}
			while (!endListening) {
				try {
					Socket connectionSocket = serverSocket.accept();
					logger.info("Accepted a connection from "
							+ connectionSocket.getInetAddress().getHostName()
							+ ":" + connectionSocket.getPort());
					createPeerFromSocket(connectionSocket);
				} catch (SocketTimeoutException e) {
					// do nothing
				} catch (IOException e) {
					logger.error("Failed to accept a connection.", e);
					logger.info("Listening again.");
				}
			}
			logger.info("Application has been closed. Ending listening.");
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					logger.error("Failed to close socket.", e);
					// there is nothing to do
				}
			}
		}
	}

	public static void main(String[] args) {
		setupThreading();
		AppArgs arguments = new AppArgs(args);
		App app = new App(arguments);
		app.listen();
	}
}
