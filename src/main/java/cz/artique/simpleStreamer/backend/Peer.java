package cz.artique.simpleStreamer.backend;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.compression.Compressor;
import cz.artique.simpleStreamer.compression.Compressors;
import cz.artique.simpleStreamer.compression.ImageFormat;
import cz.artique.simpleStreamer.interconnect.CrateImage;
import cz.artique.simpleStreamer.network.ImageMessage;
import cz.artique.simpleStreamer.network.MalformedMessageException;
import cz.artique.simpleStreamer.network.Message;
import cz.artique.simpleStreamer.network.MessageHandler;
import cz.artique.simpleStreamer.network.StartStreamMessage;
import cz.artique.simpleStreamer.network.StopStreamMessage;

public class Peer extends AbstractImageProvider {
	static final Logger logger = LogManager.getLogger(Peer.class.getName());

	private int width;
	private int height;

	private MessageHandler messageHandler;

	private Thread receivingThread;
	private Thread sendingThread;

	private InetAddress hostname;
	private int port;

	public Peer(Socket socket, ImageProvider sendingProvider,
			ImageFormat sendingFormat, int rate) throws IOException {
		super(socket.getInetAddress().getHostName() + ":" + socket.getPort());
		hostname = socket.getInetAddress();
		port = socket.getPort();
		messageHandler = new MessageHandler(socket);

		PeerReceiver peerReceiver = new PeerReceiver();
		receivingThread = new Thread(peerReceiver);
		receivingThread.setDaemon(true);
		receivingThread.start();

		PeerSender peerSender = new PeerSender(sendingFormat, sendingProvider,
				rate);
		sendingThread = new Thread(peerSender);
		sendingThread.start();

		logger.info(this + " Peer started.");
	}

	@Override
	public int getWidth() {
		if (ImageProviderState.UNINITIALIZED.equals(getState())) {
			throw new IllegalStateException("Not yet initialized");
		}
		return width;
	}

	@Override
	public int getHeight() {
		if (ImageProviderState.UNINITIALIZED.equals(getState())) {
			throw new IllegalStateException("Not yet initialized");
		}
		return height;
	}

	public InetAddress getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	private class PeerReceiver implements Runnable {
		private ImageFormat format;
		private Compressor compressor;

		@Override
		public void run() {
			while (!isEnd()) {
				try {
					Message message = messageHandler.receiveMessage();
					if (message instanceof StartStreamMessage) {
						if (!ImageProviderState.UNINITIALIZED
								.equals(getState())) {
							logger.error(this
									+ " Got start stream message when the stream has already been initialized.");
							logger.info(this + " Skipping this message.");
							continue;
						}
						width = ((StartStreamMessage) message).getWidth();
						height = ((StartStreamMessage) message).getHeight();
						format = ((StartStreamMessage) message).getFormat();
						compressor = Compressors.COMPRESSORS
								.getCompressor(format);
						logger.info(this + " Initialized with format " + format
								+ " width " + width + " height " + height + ".");
						if (compressor == null) {
							logger.error(this
									+ " There is no compressor for format "
									+ format + ".");
							break;
						}
						setState(ImageProviderState.INITIALIZED);
					} else if (message instanceof StopStreamMessage) {
						if (ImageProviderState.UNINITIALIZED.equals(getState())) {
							logger.error(this
									+ " Got stop stream message when the stream has not been started yet.");
						}
						break;
					} else if (message instanceof ImageMessage) {
						if (ImageProviderState.UNINITIALIZED.equals(getState())) {
							logger.error(this
									+ " Got image when the streaming has not been started yet.");
							logger.info(this + " Dropping the image message.");
							continue;
						}
						byte[] data = ((ImageMessage) message).getData();
						byte[] image = compressor.uncompress(data);
						logger.info(this + " Successfully got image.");
						getCrate().setImage(image, getWidth(), getHeight());
						fireImageAvailable();
					}
				} catch (MalformedMessageException e) {
					logger.error(this + " Got a malformed message.", e);
					logger.info(this
							+ " We will skip this one and wait for the next one.");
				} catch (IOException e) {
					logger.error(this
							+ " Failed to read from the socket. Suppose it is broken.");
					break;
				}
			}

			setState(ImageProviderState.OBSOLETE);
			if (!isEnd()) {
				logger.info(this + "Terminating peer from receiver's side.");
				terminate();
			}
		}

		@Override
		public String toString() {
			return Peer.this.toString() + " receiver";
		}
	}

	private class PeerSender implements Runnable {
		private ImageFormat format;
		private Compressor compressor;
		private ImageProvider sendingProvider;
		ImageProviderState sendingState = ImageProviderState.UNINITIALIZED;
		private int rate;

		public PeerSender(ImageFormat format, ImageProvider sendingProvider,
				int rate) {
			this.format = format;
			this.sendingProvider = sendingProvider;
			this.rate = rate;
			this.compressor = Compressors.COMPRESSORS.getCompressor(format);
		}

		@Override
		public void run() {
			while (!isEnd()) {
				if (ImageProviderState.UNINITIALIZED.equals(sendingState)) {
					StartStreamMessage message = new StartStreamMessage(format,
							sendingProvider.getWidth(),
							sendingProvider.getHeight());
					logger.info(this
							+ " Sending start stream message width format "
							+ format + ", width " + sendingProvider.getWidth()
							+ ", height" + sendingProvider.getHeight() + ".");
					try {
						messageHandler.sendMessage(message);
					} catch (IOException e) {
						logger.error(this
								+ " Failed to send the stream start message.");
						break;
					}
					sendingState = ImageProviderState.INITIALIZED;
				} else {
					CrateImage crateImage = getImage();
					byte[] data = compressor.compress(crateImage.getRawImage());
					ImageMessage message = new ImageMessage(data);
					logger.info(this + " Sending image number "
							+ crateImage.getNumber());
					try {
						messageHandler.sendMessage(message);
						try {
							Thread.sleep(rate);
						} catch (InterruptedException e) {
							// do nothing
						}
					} catch (IOException e) {
						logger.error(this + " Failed to send an image message.");
						break;
					}
				}
			}

			if (!ImageProviderState.UNINITIALIZED.equals(sendingState)) {
				StopStreamMessage message = new StopStreamMessage();
				logger.info(this + " Sending stop stream message.");
				try {
					messageHandler.sendMessage(message);
				} catch (IOException e) {
					logger.error(this
							+ " Failed to send the stop stream message.");
				}
				messageHandler.close();
			}

			if (!isEnd()) {
				logger.info(this + "Terminating peer from sender's side.");
				terminate();
			}
		}

		private CrateImage getImage() {
			return sendingProvider.getCrate().getImage(-1);
		}

		@Override
		public String toString() {
			return Peer.this.toString() + " sender";
		}
	}

}
