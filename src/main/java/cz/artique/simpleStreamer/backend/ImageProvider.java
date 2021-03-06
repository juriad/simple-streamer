package cz.artique.simpleStreamer.backend;

import cz.artique.simpleStreamer.interconnect.Crate;


public interface ImageProvider {

	int getWidth();

	int getHeight();

	Crate getCrate();

	ImageProviderState getState();

	void terminate();

	void addImageProviderListener(ImageProviderListener l);

	String getName();

}
