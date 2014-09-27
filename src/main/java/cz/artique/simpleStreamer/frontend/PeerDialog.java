package cz.artique.simpleStreamer.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class PeerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private InetAddress hostname;

	private int port;

	private boolean filled;

	public PeerDialog(JFrame owner) {
		super(owner, "New peer connection", true);

		final JTextField hostnameField = new JTextField("localhost", 20);
		final JTextField portField = new JTextField("6262", 6);
		JButton button = new JButton("Add peer");

		JPanel panel = new JPanel(new MigLayout("gap 10px 10px"));

		panel.add(new JLabel("Hostname"));
		panel.add(hostnameField, "wrap");
		panel.add(new JLabel("Port"));
		panel.add(portField, "wrap");
		panel.add(button, "span,align right");

		getContentPane().add(panel);

		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String hostnameString = hostnameField.getText();
				if (hostnameString == null) {
					JOptionPane.showMessageDialog(PeerDialog.this,
							"Specify the hostname.", "Hostaname error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					hostname = InetAddress.getByName(hostnameString);
				} catch (UnknownHostException e1) {
					JOptionPane.showMessageDialog(PeerDialog.this,
							"The specified hostname is not valid.",
							"Hostaname error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				String portString = portField.getText();
				if (portString == null) {
					JOptionPane.showMessageDialog(PeerDialog.this,
							"Specify the port.", "Port error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					port = Short.parseShort(portString);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(PeerDialog.this,
							"The specified port is not valid.", "Port error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				filled = true;
				dispose();
			}
		});

		pack();
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public InetAddress getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public boolean isFilled() {
		return filled;
	}

}
