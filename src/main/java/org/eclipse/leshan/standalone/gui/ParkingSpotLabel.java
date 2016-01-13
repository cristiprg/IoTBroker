package org.eclipse.leshan.standalone.gui;

import java.text.DecimalFormat;

import javax.swing.JLabel;

import org.eclipse.leshan.standalone.model.ParkingSpot;

public class ParkingSpotLabel extends JLabel {

	private ParkingSpot parkingSpot;
	private final static DecimalFormat df = new DecimalFormat("0.00");
	
	public ParkingSpotLabel(ParkingSpot parkingSpot){
		this.parkingSpot = parkingSpot;
	}
	
	public void updateLabel(){
		
		// set color
		String color = "";
		switch(parkingSpot.getState()){
			case "free":
				color = "green";
			break;
			case "occupied":
				color = "red";
				break;
			case "reserved":
				color = "orange";				
				break;
			default:
				break;
		}

		setText("<html> <body style=\"background-color:" + color + ";\"> " + 
				parkingSpot.getpID() + " <br> " + 
				parkingSpot.getLicensePlate() + " <br> " + 
				df.format(parkingSpot.getBillingAmount()) + " </body> </html>");
		
		setToolTipText(parkingSpot.getReport());
	}
	
	
}
