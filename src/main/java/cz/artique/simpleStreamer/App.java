package cz.artique.simpleStreamer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImagePrioviderState;
import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.ImageProviderListener;
import cz.artique.simpleStreamer.backend.Peer;
import cz.artique.simpleStreamer.backend.WebCamReader;
import cz.artique.simpleStreamer.frontend.CloseListener;
import cz.artique.simpleStreamer.frontend.Displayer;
import cz.artique.simpleStreamer.interconnect.CleverList;

public class App {
	static final Logger logger = LogManager.getLogger(App.class.getName());

	private CleverList<ImageProvider> imageProviders;

	private AppArgs arguments;

	private Thread mainThread;

	/**
	 * If any thread throws an exception, whole application should crash.
	 */
	private static void setupThreading() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.exit(1);
			}
		});
	}

	public App(AppArgs arguments) {
		this.arguments = arguments;
		mainThread = Thread.currentThread();
		List<InetAddress> remoteHosts = arguments.getRemoteHosts();
		List<Integer> remotePorts = arguments.getRemotePorts();

		int count = remoteHosts.size() < remotePorts.size() ? remoteHosts
				.size() : remotePorts.size();
		if (remoteHosts.size() != remotePorts.size()) {
			logger.warn("The number of remotes does not equal to number of ports given.");
		}
		imageProviders = new CleverList<ImageProvider>();
		createWebReader();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Displayer(imageProviders)
						.addCloseListener(new CloseListener() {
							@Override
							public void applicationClosing() {
								mainThread.setDaemon(true);
							}
						});
			}
		});

		createPeers(count, arguments.getRemoteHosts(),
				arguments.getRemotePorts());
	}

	private WebCamReader createWebReader() {
		// FIXME what to do with fps
		int fps = 300;

		WebCamReader webCamReader = new WebCamReader(arguments.getWidth(),
				arguments.getHeight(), fps);
		imageProviders.add(webCamReader);
		watchState(webCamReader);

		Thread reader = new Thread(webCamReader);
		reader.start();

		return webCamReader;
	}

	private Peer createPeer(Socket socket) throws IOException {
		Peer peer = new Peer(socket);
		watchState(peer);
		return peer;
	}

	private void watchState(ImageProvider provider) {
		provider.addImageProviderListener(new ImageProviderListener() {
			@Override
			public void stateChanged(ImageProvider provider) {
				// this makes sure that as soon as the peer becomes
				// obsolete, it will be removed from the lists.
				if (ImagePrioviderState.OBSOLETE.equals(provider.getState())) {
					imageProviders.remove(provider);
				}
			}

			@Override
			public void imageAvailable(ImageProvider provider) {
				// ignore
			}
		});
	}

	private void createPeers(int count, List<InetAddress> remoteHosts,
			List<Integer> remotePorts) {
		for (int i = 0; i < count; i++) {
			InetAddress host = remoteHosts.get(i);
			Integer port = remotePorts.get(i);

			Socket socket = null;
			try {
				socket = new Socket(host, port);
			} catch (IOException e) {
				logger.error("Could not connect to host " + host.getHostName()
						+ " and port " + port, e);
				logger.info("Will skip this connection and continue with others.");
				continue;
			}
			try {
				Peer p = createPeer(socket);
				imageProviders.add(p);
			} catch (Exception e) {
				logger.error("Could not create peer for " + host.getHostName()
						+ " and port " + port, e);
				logger.info("Will skip this peer and continue with others.");
			}
		}
	}

	private void listen() {
		ServerSocket serverSocket = null;
		try {
			try {
				serverSocket = new ServerSocket(arguments.getServerPort());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (true) {
				try {
					Socket connectionSocket = serverSocket.accept();
					Peer p = createPeer(connectionSocket);
					imageProviders.add(p);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
