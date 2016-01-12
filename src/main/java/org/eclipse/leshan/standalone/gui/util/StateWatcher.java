package org.eclipse.leshan.standalone.gui.util;

import java.util.HashMap;

import org.eclipse.leshan.standalone.BrokerState;
import org.eclipse.leshan.standalone.gui.ApplicationWindow;

/**
 * Thread that updates the GUI according to the broker state.
 */
public class StateWatcher implements Runnable{

	private BrokerState brokerState = BrokerState.getInstance();
	private ApplicationWindow applicationWindow;
	private HashMap<String, String> registeredParkingSpots;
	
	
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
			
			// sleep for 900 ms, and if interrupted, break and exit
			try {
				Thread.sleep(900);
			} catch (InterruptedException e) {
				break;
			}
		}
		
	}

	private void updateNumberSpots() {
		
		nrFreeSpots = 0;
		nrReservedSpots = 0;
		nrOccupiedSpots = 0;	
		
		for (String state : registeredParkingSpots.values()){
			switch (state) {
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
}
