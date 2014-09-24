package cz.artique.simpleStreamer.backend;

import java.io.IOException;
import java.net.Socket;

import cz.artique.simpleStreamer.Crate;
import cz.artique.simpleStreamer.network.MessageHandler;

public class Peer extends AbstractImageProvider implements Runnable {

	private int width;
	private int height;
	private MessageHandler messageHandler;

	public Peer(Socket socket, Crate myCrate, Crate toSend) throws IOException {
		super(myCrate);
		messageHandler = new MessageHandler(socket);
	}

	@Override
	public int getWidth() {
		if (ImagePrioviderState.UNINITIALIZED.equals(getState())) {
			throw new IllegalStateException("Not yet negotiated");
		}
		return width;
	}

	@Override
	public int getHeight() {
		if (ImagePrioviderState.UNINITIALIZED.equals(getState())) {
			throw new IllegalStateException("Not yet negotiated");
		}
		return height;
	}

	@Override
	public void run() {
		// TODO read messages
	}
}
