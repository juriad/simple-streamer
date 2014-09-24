package cz.artique.simpleStreamer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImagePrioviderState;
import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.ImageProviderListener;
import cz.artique.simpleStreamer.backend.Peer;
import cz.artique.simpleStreamer.backend.WebCamReader;

public class App {
	static final Logger logger = LogManager.getLogger(App.class.getName());

	private WebCamReader webCamReader;

	private List<Peer> peers;

	private List<Crate> crates;

	private AppArgs arguments;

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
		List<InetAddress> remoteHosts = arguments.getRemoteHosts();
		List<Integer> remotePorts = arguments.getRemotePorts();

		int count = remoteHosts.size() < remotePorts.size() ? remoteHosts
				.size() : remotePorts.size();
		if (remoteHosts.size() != remotePorts.size()) {
			logger.warn("The number of remotes does not equal to number of ports given.");
		}
		crates = new CopyOnWriteArrayList<Crate>();
		Crate crate = new Crate();
		crates.add(crate);

		// FIXME what to do with fps
		int fps = 30;

		webCamReader = new WebCamReader(crate, arguments.getWidth(),
				arguments.getHeight(), fps);

		Thread reader = new Thread(webCamReader);
		reader.start();

		peers = new CopyOnWriteArrayList<Peer>();
		createPeers(count, arguments.getRemoteHosts(),
				arguments.getRemotePorts());
	}

	private Peer createPeer(Socket socket) {
		Crate crate = new Crate();

		Peer p = null;
		try {
			p = new Peer(socket, crate, crates.get(0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		p.addImageProviderListener(new ImageProviderListener() {
			@Override
			public void stateChanged(ImageProvider provider) {
				// this makes sure that as soon as the peer becomes
				// obsolete, it will be removed from the lists.
				if (ImagePrioviderState.OBSOLETE.equals(provider.getState())) {
					peers.remove(provider);
					crates.remove(provider.getCrate());
				}
			}

			@Override
			public void imageAvailable(ImageProvider provider) {
			}
		});

		return p;
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
					peers.add(p);
					crates.add(p.getCrate());
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

	private void createPeers(int count, List<InetAddress> remoteHosts,
			List<Integer> remotePorts) {
		final List<Peer> peers = new CopyOnWriteArrayList<Peer>();

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
			Peer p = createPeer(socket);
			peers.add(p);
			crates.add(p.getCrate());
		}
	}
}
