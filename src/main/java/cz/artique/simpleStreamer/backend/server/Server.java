package cz.artique.simpleStreamer.backend.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server implements Runnable {
	static final Logger logger = LogManager.getLogger(Server.class.getName());

	private final int port;

	private boolean end = false;

	private PeerInitializer peerInitializer;

	public Server(int port, PeerInitializer peerInitializer) {
		this.port = port;
		this.peerInitializer = peerInitializer;

		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {
			try {
				serverSocket = new ServerSocket(getPort());
				serverSocket.setSoTimeout(100);
				logger.info("Created server socket for local port " + getPort());
			} catch (IOException e) {
				logger.error("Could not create socket for local port "
						+ getPort(), e);
				logger.info("Killing the application.");
				throw new RuntimeException(e);
			}
			while (!isEnd()) {
				try {
					Socket connectionSocket = serverSocket.accept();
					logger.info("Accepted a connection from "
							+ connectionSocket.getInetAddress().getHostName()
							+ ":" + connectionSocket.getPort());
					peerInitializer.createPeerFromSocket(connectionSocket);
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

	public synchronized void terminate() {
		logger.info(this + " Terminate called.");
		end = true;
	}

	protected synchronized boolean isEnd() {
		return end;
	}

	public int getPort() {
		return port;
	}

}
