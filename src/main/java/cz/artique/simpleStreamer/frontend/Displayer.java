package cz.artique.simpleStreamer.frontend;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.artique.simpleStreamer.backend.ImageProvider;
import cz.artique.simpleStreamer.backend.ImageProviderListener;
import cz.artique.simpleStreamer.backend.Peer;
import cz.artique.simpleStreamer.backend.cam.AbstractWebcamReader;
import cz.artique.simpleStreamer.interconnect.CleverList;
import cz.artique.simpleStreamer.interconnect.ListChangeListener;

public class Displayer {
	static final Logger logger = LogManager
			.getLogger(AbstractWebcamReader.class.getName());

	private Map<ImageProvider, CamViewer> viewers;
	private JPanel main;
	private DefaultListModel<ImageProvider> listModel;
	private List<DisplayerListener> listeners;
	private CleverList<ImageProvider> providers;
	private JFrame frame;

	public Displayer(final CleverList<ImageProvider> providers) {
		this.providers = providers;
		viewers = new HashMap<ImageProvider, CamViewer>();
		listeners = new ArrayList<DisplayerListener>();

		frame = new JFrame("Webcam Conference");
		JPanel content = new JPanel(new BorderLayout(10, 10));
		frame.getContentPane().add(content);

		JPanel left = new JPanel(new BorderLayout(10, 10));
		left.setPreferredSize(new Dimension(200, 200));

		final JButton addPeer = new JButton("Add peer");
		left.add(addPeer, BorderLayout.NORTH);

		listModel = new DefaultListModel<ImageProvider>();
		final JList<ImageProvider> list = new JList<ImageProvider>(listModel);
		left.add(new JScrollPane(list,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				BorderLayout.CENTER);

		final JButton remove = new JButton("Remove");
		remove.setEnabled(false);
		left.add(remove, BorderLayout.SOUTH);

		content.add(left, BorderLayout.WEST);

		main = new JPanel(new WrapLayout(WrapLayout.LEFT, 10, 10));
		JScrollPane mainScrollPane = new JScrollPane(main,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		content.add(mainScrollPane, BorderLayout.CENTER);
		mainScrollPane.setMinimumSize(new Dimension(400, 300));
		mainScrollPane.setPreferredSize(new Dimension(800, 600));

		addPeer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("Add peer button has been clicked.");
				PeerDialog peerDialog = new PeerDialog(frame);
				peerDialog.setVisible(true);
				if (peerDialog.isFilled()) {
					logger.info("Valid data for new peer has been filled in.");
					InetAddress hostname = peerDialog.getHostname();
					int port = peerDialog.getPort();
					fireNewPeer(hostname, port);
				} else {
					logger.info("The new peer dialog has been closed.");
				}
			}
		});

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

				if (provider instanceof Peer) {
					logger.info("Remove button has been clicked; provider is "
							+ provider);
					provider.terminate();
				} else {
					terminateAllAndExit();
				}
			}
		});

		providers
				.addListChangeListener(new ListChangeListener<ImageProvider>() {
					@Override
					public void elementRemoved(int index,
							final ImageProvider value) {
						logger.info("Provider " + value
								+ " removed; scheduling its removing from GUI.");
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								listModel.removeElement(value);
								CamViewer camViewer = viewers.remove(value);
								if (camViewer != null) {
									main.remove(camViewer);
								}
								updateMainArea();
								logger.info("CamViewer of provider " + value
										+ " has been removed.");
							}
						});
					}

					@Override
					public void elementAdded(int index,
							final ImageProvider value) {
						logger.info("Provider " + value
								+ " added; scheduling its addition to GUI.");
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
				logger.info("Window closed; terminating all providers and firing close event.");
				terminateAllAndExit();
			}

		});

		for (ImageProvider p : providers) {
			registerProvider(p);
		}

		frame.pack();
		frame.setVisible(true);

		logger.info("Displayer has been created.");
	}

	private void updateMainArea() {
		main.doLayout();
		main.repaint();
	}

	private void terminateAllAndExit() {
		for (ImageProvider p : providers) {
			p.terminate();
		}

		frame.dispose();

		fireApplicationClosing();
	}

	private void registerProvider(ImageProvider p) {
		logger.info("Registering provider " + p + ", which is in state "
				+ p.getState());
		switch (p.getState()) {
		case INITIALIZED:
		case RUNNING:
			logger.info("Found a new provider " + p
					+ ", which is already initialized.");
			addProvider(p);
			// fall through
		case UNINITIALIZED:
			logger.info("Provider "
					+ p
					+ " has not been initialzied yet; waiting for its state change.");
			p.addImageProviderListener(new ImageProviderListener() {

				@Override
				public void stateChanged(final ImageProvider provider) {
					logger.info("Provider " + provider
							+ " changed its state to " + provider.getState());
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							switch (provider.getState()) {
							case INITIALIZED:
							case RUNNING:
								logger.info("Provider " + provider
										+ " is now initialized.");
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
		logger.info("Adding a new CamViewer for provider" + provider);
		CamViewer viewer = new CamViewer(provider);
		main.add(viewer);
		updateMainArea();
		viewers.put(provider, viewer);
		listModel.addElement(provider);
	}

	private synchronized void fireApplicationClosing() {
		for (DisplayerListener l : listeners) {
			l.applicationClosing();
		}
	}

	private synchronized void fireNewPeer(InetAddress hostname, int port) {
		for (DisplayerListener l : listeners) {
			l.newPeer(hostname, port);
		}
	}

	public synchronized void addDisplayerListener(DisplayerListener listener) {
		logger.info("Added new close listener " + listener);
		listeners.add(listener);
	}

	public void showErrorMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(frame, message, "Peer error",
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}
}
