package org.eclipse.leshan.standalone;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;

/**
 * State of the broker - contains:
 * 		*) 	list of registered vehicles
 * 		*)	list of parking spots and their state - dictionary
 * 		*)	list of events
 * 
 * This implemented as a singleton class to enforce one instance per server.
 */
public class BrokerState {

	/**
	 * List of registered vehicles. Each entry represents the license plate number. 
	 */
	private ArrayList<String> registeredVehicles = new ArrayList<>();
	
	/**
	 * List of registered parking spots. Each entry is a <parking spot ID, state> pair.
	 * The state is one of the following: "free", "occupied", "reserved".
	 */
	private HashMap<String, String> registeredParkingSpots = new HashMap<>();
	
	/**
	 * List of events related to state changes of parking spots 
	 */
	private ArrayList<String> eventsLog = new ArrayList<>();
	
	private static BrokerState instance = null;
	
	public static BrokerState getInstance(){
		if(instance == null){
			instance = new BrokerState();
		}
		
		return instance;
	}
	
	private BrokerState(){
		
	}
	
	private void logParkingSpotEvent(String message){
		eventsLog.add(new Date().toString() + ", " + message);
		System.out.println(new Date().toString() + ", " + message);
	}
	
	/**
	 * Registers a vehicle to the broker with the license plate number.
	 * @param licensePlate
	 * @return true if vehicle has successfully been registered, false otherwise.
	 */
	public boolean registerVehicle(String licensePlate){
		if (registeredVehicles.contains(licensePlate)){		
			return false;
		}
		else{
			registeredVehicles.add(licensePlate);
			return true;
		}
	}	
	
	/**
	 * Registers a parking spot with its ID and logs a "parking spot registration" event.
	 * @param pID
	 * @return true
	 */
	public boolean registerParkingSpot(String pID){
		registeredParkingSpots.put(pID, "free");
		logParkingSpotEvent(pID + ", registered");
		return true;
	}
	
	/**
	 * Changes the state of the specified parking spot ID to the newState.
	 * @param pID
	 * @param newState
	 * @return true
	 */
	public boolean changeParkingSpotState(String pID, String newState){
		if (!registeredParkingSpots.containsKey(pID))
				System.err.println("Warning: changing state to unregistered parking spot! We register it for you because we are nice people ...");
				
		registeredParkingSpots.put(pID, newState);
		logParkingSpotEvent(pID + ", changed state to " + newState);
		return true;
	}
	
	/**
	 * Iterates over the list of parking spots and adds just the free ones.   
	 * @return list of free parking spots
	 */
	public ArrayList<String> getFreeParkingSpots(){
		ArrayList<String> freeSpots = new ArrayList<>();
		
		for(Map.Entry<String, String> entry : registeredParkingSpots.entrySet()){
			String pID = entry.getKey();
			String state = entry.getValue();
			
			if (state.equals("free"))
				freeSpots.add(pID);
		}
		
		return freeSpots;
	}
	
	
	/**
	 * Checks whether vehicle with ID licensePlate is registered or not
	 * @param licensePlate
	 * @return true is vehicle is registered, false otherwise
	 */
	public boolean isVehicleRegistered(String licensePlate){
		return registeredVehicles.contains(licensePlate);
	}
	
	/**
	 * Checks whether parking spot with ID pID is registered or not
	 * @param pID
	 * @return true is parking spot is registered, false otherwise
	 */
	public boolean isParkingSpotRegistered(String pID){
		return registeredParkingSpots.containsKey(pID);
	}
}
