package cz.artique.simpleStreamer.interconnect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * This list is thread-safe. It can only add a single element and remove single
 * element.
 * 
 * @author adam
 *
 * @param <E>
 */
public class CleverList<E> implements List<E> {
	static final Logger logger = LogManager.getLogger(CleverList.class
			.getName());
	private CopyOnWriteArrayList<E> list;

	private List<ListChangeListener<E>> listeners = new ArrayList<ListChangeListener<E>>();

	public CleverList() {
		list = new CopyOnWriteArrayList<E>();
	}

	public synchronized void addListChangeListener(ListChangeListener<E> l) {
		listeners.add(l);
		logger.debug("Added list change listener: " + l);
	}

	protected void fireElementAdded(int index, E value) {
		logger.debug("An element " + value + " has been added to this list.");
		for (ListChangeListener<E> l : listeners) {
			l.elementAdded(index, value);
		}
	}

	protected void fireElementRemoved(int index, E value) {
		logger.debug("An element " + value
				+ " has been removed from this list.");
		for (ListChangeListener<E> l : listeners) {
			l.elementRemoved(index, value);
		}
	}

	@Override
	public E get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean add(E e) {
		list.add(e);
		fireElementAdded(list.size() - 1, e);
		return true;
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		int i = list.indexOf(o);
		boolean removed = list.remove(o);
		if (removed) {
			fireElementRemoved(i, (E) o);
		}
		return removed;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		E e = list.get(index);
		remove(e);
		return e;
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

}
