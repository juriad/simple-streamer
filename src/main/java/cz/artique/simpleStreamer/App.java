package cz.artique.simpleStreamer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.Peer;
import cz.artique.simpleStreamer.backend.cam.DummyWebCamReader;
import cz.artique.simpleStreamer.backend.cam.RealWebCamReader;
import cz.artique.simpleStreamer.backend.cam.WebCamReader;
import cz.artique.simpleStreamer.backend.server.PeerInitializer;
import cz.artique.simpleStreamer.backend.server.PeerInitializerCallback;
import cz.artique.simpleStreamer.backend.server.Server;
import cz.artique.simpleStreamer.compression.Compressors;
import cz.artique.simpleStreamer.compression.RawCompressor;
import cz.artique.simpleStreamer.frontend.Displayer;
import cz.artique.simpleStreamer.frontend.DisplayerListener;
import cz.artique.simpleStreamer.interconnect.CleverList;

public class App {
	static final Logger logger = LogManager.getLogger(App.class.getName());

	private final CleverList<ImageProvider> imageProviders;

	private final WebCamReader webCamReader;

	private final Server server;

	private Displayer displayer;

	private PeerInitializer peerInitializer;

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

	private static void setupCompressors() {
		Compressors.COMPRESSORS.registerCompressor(new RawCompressor());
	}

	public App(final AppArgs arguments) {
		imageProviders = new CleverList<ImageProvider>();
		webCamReader = createWebCamReader(arguments.getWidth(),
				arguments.getHeight(), arguments.isDummy());
		imageProviders.add(webCamReader);

		peerInitializer = new PeerInitializer(imageProviders,
				arguments.getRate(), webCamReader);
		peerInitializer.createPeers(arguments.getRemoteHosts(),
				arguments.getRemotePorts(), null);

		server = new Server(arguments.getServerPort(), peerInitializer);

		logger.info("Scheduling GUI creation.");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setDisplayer(new Displayer(imageProviders));
				getDisplayer().addDisplayerListener(new DisplayerListener() {
					@Override
					public void applicationClosing() {
						closeApplication();
					}

					@Override
					public void newPeer(final InetAddress hostname,
							final int port) {
						createNewPeer(hostname, port);
					}
				});
			}
		});
	}

	private void waitForReturn() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("Waiting for return key to be pressed.");
				try {
					System.in.read();
				} catch (IOException e) {
					logger.error("Failed to read from standard input; closing application");
				}
				closeApplication();
			}
		});

		thread.setDaemon(true);
		thread.start();
	}

	private static WebCamReader createWebCamReader(int width, int height,
			boolean dummy) {
		int rate = 20; // wait between images (50FPS)

		WebCamReader webCamReader = null;

		if (!dummy) {
			try {
				webCamReader = new RealWebCamReader(width, height, rate);
				logger.info("Created a real webcam reader.");
			} catch (FileNotFoundException e) {
			}
		}

		if (webCamReader == null) {
			webCamReader = new DummyWebCamReader(width, height, rate);
			logger.info("Created a dummy webcam reader.");
		}

		return webCamReader;
	}

	private void createNewPeer(InetAddress hostname, int port) {
		if (hostname.isLoopbackAddress() && port == server.getPort()) {
			showMessage("Connection to yourself is prohibited.", "Peer Error",
					JOptionPane.ERROR_MESSAGE);
			logger.error("Connection to yourself is prohibited.");
			return;
		}

		for (ImageProvider provider : imageProviders) {
			if (provider instanceof Peer) {
				Peer p = (Peer) provider;
				if (p.getHostname().equals(hostname) && p.getPort() == port) {
					String msg = "You are already connected to "
							+ hostname.getHostName() + ":" + port + ".";
					showMessage(msg, "Peer Error", JOptionPane.ERROR_MESSAGE);
					logger.error(msg);
					return;
				}
			}
		}

		logger.info("Adding new peer; this may take a while, processing in a new thread.");
		peerInitializer.createPeer(hostname, port,
				new PeerInitializerCallback() {
					@Override
					public void onSuccess(InetAddress hostname, int port) {
					}

					@Override
					public void onError(InetAddress hostname, int port,
							Throwable t) {
						showMessage(
								"Failed to connect to peer "
										+ hostname.getHostName() + ":" + port
										+ ".", "Peer Error",
								JOptionPane.ERROR_MESSAGE);
						logger.info("Ignoring this failed peer.");
					}
				});
	}

	private synchronized void closeApplication() {
		logger.info("Application should be closed, terminating all Providers.");
		for (ImageProvider p : imageProviders) {
			p.terminate();
		}
		logger.info("Terminating Sever and closing Displayer");
		server.terminate();
		getDisplayer().close();
	}

	private void showMessage(String message, String title, int type) {
		if (getDisplayer() != null) {
			getDisplayer().showMessage(message, title, type);
		}
	}

	public static void main(String[] args) {
		setupThreading();
		setupCompressors();
		AppArgs arguments = new AppArgs(args);
		App app = new App(arguments);
		app.waitForReturn();
	}

	private synchronized Displayer getDisplayer() {
		return displayer;
	}

	private synchronized void setDisplayer(Displayer displayer) {
		this.displayer = displayer;
	}
}
