package cz.artique.simpleStreamer;


public class Crate {

	private int imageNumber = -1;
	private byte[] image = null;

	public Crate() {
	}

	public synchronized void setImage(byte[] image) {
		this.image = image;
	}

	public synchronized byte[] getImage() {
		return image;
	}

	public synchronized int getImageNumber() {
		return imageNumber;
	}

}
