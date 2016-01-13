package org.eclipse.leshan.standalone.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.awt.FlowLayout;
import javax.swing.JTextField;

import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.request.WriteRequest.Mode;
import org.eclipse.leshan.standalone.LeshanStandalone;
import org.eclipse.leshan.standalone.model.ParkingSpot;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ApplicationWindow {

	private JFrame frame;
	private JLabel freeSpotsValueLabel;
	private JLabel reservedSpotsValueLabel;
	private JLabel occupiedSpotsValueLabel;
	private ArrayList<ParkingSpotLabel> parkingSpotLabels;
	private JPanel panel;
	private JTextField fwTextField;
	private JLabel lblUseletterOr;
	private LeshanStandalone theeBroker;

	public ArrayList<ParkingSpotLabel> getParkingSpotLabels() {
		return parkingSpotLabels;
	}

	/**
	 * Launch the application.
	 */
	/*public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ApplicationWindow window = new ApplicationWindow();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

	/**
	 * Create the application.
	 */
	public ApplicationWindow(LeshanStandalone broker) {
		this.theeBroker = broker;
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
		
		panel = new JPanel();
		panel.setBounds(12, 39, 426, 200);
		frame.getContentPane().add(panel);
		
		JButton btnUpdateFw = new JButton("Update FW");
		btnUpdateFw.addActionListener(new ActionListener() {
			//TODO: refactor
			public void actionPerformed(ActionEvent e) {
				if (fwTextField.getText().equals("letter") || fwTextField.getText().equals("color")){				
					for(ParkingSpotLabel label : parkingSpotLabels){
						theeBroker.updateFirmware(label.parkingSpot.getpID(), fwTextField.getText());
					}
				}
			}
		});
		btnUpdateFw.setBounds(321, 245, 117, 25);
		frame.getContentPane().add(btnUpdateFw);
		
		fwTextField = new JTextField();
		fwTextField.setText("letter");
		fwTextField.setBounds(190, 248, 114, 19);
		frame.getContentPane().add(fwTextField);
		fwTextField.setColumns(10);
		
		lblUseletterOr = new JLabel("Use \"letter\" or \"color\"");
		lblUseletterOr.setBounds(28, 250, 163, 15);
		frame.getContentPane().add(lblUseletterOr);
		frame.setVisible(true);
		
		parkingSpotLabels = new ArrayList<>();
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

	public JPanel getPanel() {
		return panel;
	}
}
