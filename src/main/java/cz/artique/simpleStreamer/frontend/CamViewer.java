package cz.artique.simpleStreamer.frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.ImageProviderListener;
import cz.artique.simpleStreamer.interconnect.CrateImage;

public class CamViewer extends JPanel {
	static final Logger logger = LogManager
			.getLogger(CamViewer.class.getName());

	private static final long serialVersionUID = 1L;

	private ImageProvider provider;

	private BufferedImage image;

	int imageShown = -1;

	int forcedWidth = -1;

	boolean refreshRequest = false;

	public CamViewer(ImageProvider provider) {
		this.provider = provider;

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
				logger.debug(CamViewer.this
						+ " Got a new image; scheduling a refresh request.");
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						refreshImage();
					}
				});
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				Point point = e.getPoint();
				setForcedWidth((int) point.getX());
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					setForcedWidth(-1);
				}
			}
		});

		logger.info(this + " Created.");
	}

	@Override
	public Dimension getPreferredSize() {
		if (forcedWidth <= 0) {
			return new Dimension(provider.getWidth(), provider.getHeight());
		}
		double scale = forcedWidth / (double) provider.getWidth();
		return new Dimension(forcedWidth, (int) (provider.getHeight() * scale));
	}

	public void setForcedWidth(int forcedWidth) {
		this.forcedWidth = forcedWidth;
		invalidate();
		getParent().doLayout();
	}

	private void refreshImage() {
		CrateImage crateImage = provider.getCrate().getImage(imageShown);
		imageShown = crateImage.getNumber();
		image = crateImage.getBufferedImage();
		logger.debug(this + " Showning image number " + imageShown);
		this.repaint();
		refreshRequest = false;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (image != null) {
			Image scaled = image.getScaledInstance(forcedWidth, -1,
					BufferedImage.SCALE_DEFAULT);
			g.drawImage(scaled, 0, 0, null);
		}
	}

	@Override
	public String toString() {
		return "CamViewer of " + provider.toString();
	}
}
