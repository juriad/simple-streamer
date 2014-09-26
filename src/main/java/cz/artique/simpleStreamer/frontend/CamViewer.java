package cz.artique.simpleStreamer.frontend;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.ImageProviderListener;

public class CamViewer extends JPanel {
	static final Logger logger = LogManager
			.getLogger(CamViewer.class.getName());

	private static final long serialVersionUID = 1L;

	private ImageProvider provider;

	private BufferedImage image;

	int imageShown = -1;

	boolean refreshRequest = false;

	public CamViewer(ImageProvider provider) {
		this.provider = provider;

		image = new BufferedImage(provider.getWidth(), provider.getHeight(),
				BufferedImage.TYPE_3BYTE_BGR);

		this.setPreferredSize(new Dimension(provider.getWidth(), provider
				.getHeight()));
		this.setToolTipText(provider.getName());

		provider.addImageProviderListener(new ImageProviderListener() {
			@Override
			public void stateChanged(ImageProvider provider) {
				// ignore, this is handled by Displayer
			}

			@Override
			public void imageAvailable(ImageProvider provider) {
				if (refreshRequest) {
					return;
				}
				refreshRequest = true;
				logger.info(CamViewer.this
						+ " Got a new image; scheduling a refresh request.");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						refreshImage();
					}
				});
			}
		});
		logger.info(this + " Created.");
	}

	private int[] toIntArray(byte[] barr) {
		int[] result = new int[barr.length];
		for (int i = 0; i < barr.length; i++)
			result[i] = barr[i];
		return result;
	}

	private void refreshImage() {
		byte[] imageData = provider.getCrate().getImage(imageShown);
		imageShown = provider.getCrate().getImageNumber();
		logger.info(this + " Showning image number " + imageShown);
		WritableRaster raster = image.getRaster();
		raster.setPixels(0, 0, getWidth(), getHeight(), toIntArray(imageData));
		this.repaint();
		refreshRequest = false;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
	}

	@Override
	public String toString() {
		return "CamViewer of " + provider.toString();
	}
}
