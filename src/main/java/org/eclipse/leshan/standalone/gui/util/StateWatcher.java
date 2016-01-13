package org.eclipse.leshan.standalone.gui.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.leshan.standalone.BrokerState;
import org.eclipse.leshan.standalone.gui.ApplicationWindow;
import org.eclipse.leshan.standalone.gui.ParkingSpotLabel;
import org.eclipse.leshan.standalone.model.ParkingSpot;
import org.eclipse.leshan.standalone.billInfo;


/**
 * Thread that updates the GUI according to the broker state.
 */
public class StateWatcher implements Runnable{

	private BrokerState brokerState = BrokerState.getInstance();
	private ApplicationWindow applicationWindow;
	private ArrayList<ParkingSpot> registeredParkingSpots;
	
	int nrFreeSpots = 0;
	int nrReservedSpots = 0;
	int nrOccupiedSpots = 0;


	public StateWatcher(ApplicationWindow applicationWindow) {
		this.applicationWindow = applicationWindow;
	}
	
	@Override
	public void run() {
					
		while(true){
			
			registeredParkingSpots = brokerState.getRegisteredParkingSpots();
			
			// count number of free, reserved and occupied spots
			updateNumberSpots();
			
			// update the fields related to the number of spots 
			applicationWindow.getFreeSpotsValueLabel().setText(String.valueOf(nrFreeSpots));
			applicationWindow.getReservedSpotsValueLabel().setText(String.valueOf(nrReservedSpots));
			applicationWindow.getOccupiedSpotsValueLabel().setText(String.valueOf(nrOccupiedSpots));
			
			// update the parking spot labels
			updateParkingSpotLabels();
			
			// update bills
			updateBills();
			
			// sleep for 900 ms, and if interrupted, break and exit
			try {
				Thread.sleep(900);
			} catch (InterruptedException e) {
				break;
			}
		}
		
	}

	private void updateBills() {
		for(ParkingSpot spot : registeredParkingSpots){
			
			spot.setBillingAmount( billInfo.getBill(spot.getpID()));
			
		}
	}

	private void updateParkingSpotLabels() {
		//applicationWindow.getPanel().removeAll();
		for (ParkingSpotLabel label : applicationWindow.getParkingSpotLabels()){
			label.updateLabel();
		}
	}

	private void updateNumberSpots() {
		
		nrFreeSpots = 0;
		nrReservedSpots = 0;
		nrOccupiedSpots = 0;	
		
		for (ParkingSpot spot : registeredParkingSpots){
			
			switch (spot.getState()) {
			case "free":
				++nrFreeSpots;
				break;
			case "occupied":
				++nrOccupiedSpots;
				break;
			case "reserved":
				++nrReservedSpots;
				break;
			default:
				break;
			}
		}			
	}

	private void printNumbers(){
		System.out.println("nrFreeSpots = " + nrFreeSpots);
		System.out.println("nrOccupiedSpots = " + nrOccupiedSpots);
		System.out.println("nrReservedSpots = " + nrReservedSpots);
	}

	public void parkingSpotRegistered(ParkingSpot parkingSpot) {
		ParkingSpotLabel label = new ParkingSpotLabel(parkingSpot);
		applicationWindow.getParkingSpotLabels().add(label);
		applicationWindow.getPanel().add(label);
	}
}
