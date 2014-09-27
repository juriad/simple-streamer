package cz.artique.simpleStreamer.backend.cam;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImageProviderState;

public class DummyWebCamReader extends AbstractWebcamReader {
	static final Logger logger = LogManager.getLogger(DummyWebCamReader.class
			.getName());

	private static final int[] BAND_OFFSETS = new int[] { 0, 1, 2 };
	private static final int[] BITS = { 8, 8, 8 };
	// private static final int[] OFSET = new int[] { 0 };
	private static final int DATA_TYPE = DataBuffer.TYPE_BYTE;
	private static final ColorSpace COLOR_SPACE = ColorSpace
			.getInstance(ColorSpace.CS_sRGB);

	private ComponentSampleModel smodel;
	private ComponentColorModel cmodel;

	private int number = 0;

	public DummyWebCamReader(int width, int height, int rate) {
		super("Dummy webcam", width, height, rate);

		smodel = new ComponentSampleModel(DATA_TYPE, width, height, 3,
				width * 3, BAND_OFFSETS);

		cmodel = new ComponentColorModel(COLOR_SPACE, BITS, false, false,
				Transparency.OPAQUE, DATA_TYPE);

		Thread reader = new Thread(this);
		reader.start();
		logger.info("Dummy webcam reader's thread started.");
	}

	@Override
	public void run() {
		while (!isEnd()) {
			BufferedImage image = paintImage();
			crate.setImage(image);
			fireImageAvailable();
			setState(ImageProviderState.RUNNING);
			try {
				Thread.sleep(getRate());
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		setState(ImageProviderState.OBSOLETE);
	}

	private BufferedImage paintImage() {
		WritableRaster raster = Raster.createWritableRaster(smodel, null);
		BufferedImage bi = new BufferedImage(cmodel, raster, false, null);

		Graphics g = bi.getGraphics();
		g.setFont(g.getFont().deriveFont(30.0f));
		g.drawString("Dummy", getWidth() / 4, getHeight() / 4);
		g.setFont(g.getFont().deriveFont(30.0f));
		g.drawString("" + number, getWidth() / 2, getHeight() / 2);
		g.setColor(Color.RED);
		g.drawRect(getWidth() * 3 / 4, getHeight() / 4, 20, 20);
		g.setColor(Color.GREEN);
		g.drawRect(getWidth() * 3 / 4, getHeight() * 3 / 4, 20, 20);
		g.setColor(Color.BLUE);
		g.drawRect(getWidth() / 4, getHeight() * 3 / 4, 20, 20);

		number++;
		return bi;
	}
}
