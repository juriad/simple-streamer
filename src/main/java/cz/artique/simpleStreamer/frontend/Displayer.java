package cz.artique.simpleStreamer.frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.ImageProviderListener;
import cz.artique.simpleStreamer.interconnect.CleverList;
import cz.artique.simpleStreamer.interconnect.ListChangeListener;

public class Displayer {
	Map<ImageProvider, CamViewer> viewers;
	private JPanel main;
	private DefaultListModel<ImageProvider> listModel;
	private List<CloseListener> listeners;

	public Displayer(final CleverList<ImageProvider> providers) {
		viewers = new HashMap<ImageProvider, CamViewer>();
		listeners = new ArrayList<CloseListener>();

		final JFrame frame = new JFrame("Webcam Conference");
		JPanel content = new JPanel(new BorderLayout(10, 10));
		frame.getContentPane().add(content);

		JPanel left = new JPanel(new BorderLayout(10, 10));

		listModel = new DefaultListModel<ImageProvider>();
		final JList<ImageProvider> list = new JList<ImageProvider>(listModel);
		left.add(new JScrollPane(list), BorderLayout.CENTER);

		final JButton remove = new JButton("Remove");
		remove.setEnabled(false);
		left.add(remove, BorderLayout.SOUTH);

		content.add(left, BorderLayout.WEST);

		main = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		content.add(new JScrollPane(main), BorderLayout.CENTER);
		main.setPreferredSize(new Dimension(400, 300));

		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				remove.setEnabled(list.getSelectedValue() != null);
			}
		});
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImageProvider provider = list.getSelectedValue();
				provider.terminate();
			}
		});

		providers
				.addListChangeListener(new ListChangeListener<ImageProvider>() {
					@Override
					public void elementRemoved(int index,
							final ImageProvider value) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								listModel.removeElement(value);
								CamViewer camViewer = viewers.remove(value);
								if (camViewer != null) {
									main.remove(camViewer);
								}
							}
						});
					}

					@Override
					public void elementAdded(int index,
							final ImageProvider value) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								registerProvider(value);
							}
						});
					}
				});

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				for (ImageProvider p : providers) {
					p.terminate();
				}

				frame.dispose();

				for (CloseListener l : listeners) {
					l.applicationClosing();
				}
			}
		});

		for (ImageProvider p : providers) {
			registerProvider(p);
		}

		frame.pack();
		frame.setVisible(true);
	}

	private void registerProvider(ImageProvider p) {
		switch (p.getState()) {
		case INITIALIZED:
		case RUNNING:
			addProvider(p);
			// fall through
		case UNINITIALIZED:
			p.addImageProviderListener(new ImageProviderListener() {
				@Override
				public void stateChanged(final ImageProvider provider) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							switch (provider.getState()) {
							case INITIALIZED:
							case RUNNING:
								addProvider(provider);
								break;
							case OBSOLETE:
								// ignore, it will be removed from the list
								// and then removed from here
								break;
							case UNINITIALIZED:
								// we want to wait for initialized or
								// running
								break;
							default:
								throw new IllegalStateException(
										"No such provider state");
							}
						}
					});
				}

				@Override
				public void imageAvailable(ImageProvider provider) {
					// this is handled by the CanViewer
				}
			});
			break;
		case OBSOLETE:
			// do nothing
			break;
		default:
			throw new IllegalStateException("No such provider state");
		}
	}

	private void addProvider(ImageProvider provider) {
		if (viewers.containsKey(provider)) {
			return;
		}
		CamViewer viewer = new CamViewer(provider);
		main.add(viewer);
		viewers.put(provider, viewer);
		listModel.addElement(provider);
	}

	public void addCloseListener(CloseListener listener) {
		listeners.add(listener);
	}
}
