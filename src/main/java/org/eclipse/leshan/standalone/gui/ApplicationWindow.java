package org.eclipse.leshan.standalone.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;

public class ApplicationWindow {

	private JFrame frame;
	private JLabel freeSpotsValueLabel;
	private JLabel reservedSpotsValueLabel;
	private JLabel occupiedSpotsValueLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ApplicationWindow window = new ApplicationWindow();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ApplicationWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel freeSpotsLabel = new JLabel("Free:");
		freeSpotsLabel.setBounds(12, 12, 48, 15);
		frame.getContentPane().add(freeSpotsLabel);
		
		JLabel reservedSpotsLabel = new JLabel("Res:");
		reservedSpotsLabel.setBounds(156, 12, 38, 15);
		frame.getContentPane().add(reservedSpotsLabel);
		
		JLabel occupiedSpotsLabel = new JLabel("Occ:");
		occupiedSpotsLabel.setBounds(324, 12, 38, 15);
		frame.getContentPane().add(occupiedSpotsLabel);
		
		freeSpotsValueLabel = new JLabel("0");
		freeSpotsValueLabel.setBounds(58, 12, 70, 15);
		frame.getContentPane().add(freeSpotsValueLabel);
		
		reservedSpotsValueLabel = new JLabel("0");
		reservedSpotsValueLabel.setBounds(193, 12, 70, 15);
		frame.getContentPane().add(reservedSpotsValueLabel);
		
		occupiedSpotsValueLabel = new JLabel("0");
		occupiedSpotsValueLabel.setBounds(368, 12, 70, 15);
		frame.getContentPane().add(occupiedSpotsValueLabel);
		
		frame.setVisible(true);
	}
	public JLabel getFreeSpotsValueLabel() {
		return freeSpotsValueLabel;
	}
	public JLabel getOccupiedSpotsValueLabel() {
		return occupiedSpotsValueLabel;
	}
	public JLabel getReservedSpotsValueLabel() {
		return reservedSpotsValueLabel;
	}
}
