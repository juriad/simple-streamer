package cz.artique.simpleStreamer.frontend;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.ImageProviderListener;

public class CamViewer extends JPanel {
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
				if (imageShown >= provider.getCrate().getImageNumber()
						|| refreshRequest) {
					return;
				}
				refreshRequest = true;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						refreshImage();
					}
				});
			}
		});
	}

	private int[] toIntArray(byte[] barr) {
		int[] result = new int[barr.length];
		for (int i = 0; i < barr.length; i++)
			result[i] = barr[i];
		return result;
	}

	private void refreshImage() {
		byte[] imageData = provider.getCrate().getImage();
		WritableRaster raster = image.getRaster();
		raster.setPixels(0, 0, getWidth(), getHeight(), toIntArray(imageData));
		this.repaint();
		imageShown = provider.getCrate().getImageNumber();
		refreshRequest = false;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
	}
}
