package cz.artique.simpleStreamer.interconnect;

public interface ListChangeListener<E> {
	void elementAdded(int index, E value);

	void elementRemoved(int index, E value);
}
