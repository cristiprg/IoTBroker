package org.eclipse.leshan.standalone;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.ParagraphAction;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.leshan.standalone.model.ParkingSpot;

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
	 * List of registered parking spots;
	 */
	ArrayList<ParkingSpot> parkingSpotsArrayList = new ArrayList<>();
	
	/**
	 * List of registered vehicles. Each entry represents the license plate number. 
	 */
	private ArrayList<String> registeredVehicles = new ArrayList<>();
	
	/**
	 * List of registered parking spots. Each entry is a <parking spot ID, state> pair.
	 * The state is one of the following: "free", "occupied", "reserved".
	 */
	@Deprecated
	private HashMap<String, String> registeredParkingSpots = new HashMap<>();
	
	/**
	 * Relation between parking spot id and its endpoint
	 */
	@Deprecated
	private HashMap<String, String> parkingSpotIDEndpointMap = new HashMap<>();
	
	public ArrayList<ParkingSpot> getRegisteredParkingSpots() {
		
		return parkingSpotsArrayList;
	}

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
	 * @param parkingSpotID 
	 * @return true
	 */
	public boolean registerParkingSpot(String endpoint, String pID){
		ParkingSpot parkingSpot = new ParkingSpot(endpoint, pID);
		parkingSpotsArrayList.add(parkingSpot);
		
		//parkingSpotIDEndpointMap.put(pID, endpoint);
		//registeredParkingSpots.put(pID, "free");
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
		for(ParkingSpot spot : parkingSpotsArrayList){
			if (spot.getpID().equals(pID)){
				spot.setState(newState);
				return true;
			}
		}
		
		
		/*
		if (!registeredParkingSpots.containsKey(pID))
				System.err.println("Warning: changing state to unregistered parking spot! We register it for you because we are nice people ...");
				
		registeredParkingSpots.put(pID, newState);
		logParkingSpotEvent(pID + ", changed state to " + newState);*/
		//return true;
		return false;
	}
	
	/**
	 * Iterates over the list of parking spots and adds just the free ones.   
	 * @return list of free parking spots
	 */
	public ArrayList<String> getFreeParkingSpots(){
		ArrayList<String> freeSpots = new ArrayList<>();
		
		for(ParkingSpot spot : parkingSpotsArrayList){
			if (spot.getState().equals("free")){
				freeSpots.add(spot.getpID());
			}
		}
		
		/*for(Map.Entry<String, String> entry : registeredParkingSpots.entrySet()){
			String pID = entry.getKey();
			String state = entry.getValue();
			
			if (state.equals("free"))
				freeSpots.add(pID);
		}*/			
		
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
		for(ParkingSpot spot : parkingSpotsArrayList){
			if (spot.getpID().equals(pID)){				
				return true;
			}
		}
		
		return false;
		//return registeredParkingSpots.containsKey(pID);
	}

	
	/**
	 * 
	 * @param parkingSpotID
	 * @return
	 */
	public String getEndpointByParkingSpotID(String parkingSpotID) {
		for(ParkingSpot spot : parkingSpotsArrayList){
			if (spot.getpID().equals(parkingSpotID)){				
				return spot.getEndpoint();
			}
		}
		
		return null;
		//return parkingSpotIDEndpointMap.get(parkingSpotID);
	}
	
	/**
	 * 
	 * @param endpoint
	 * @return
	 */
	public String getParkingSpotByEndpoint(String endpoint){
		for(ParkingSpot spot : parkingSpotsArrayList){
			if (spot.getEndpoint().equals(endpoint)){				
				return spot.getpID();
			}
		}
		
		/*String __pID = "", __endpoint = "";
		for (Map.Entry<String, String> e : parkingSpotIDEndpointMap.entrySet()) {
		    __pID = e.getKey();
		    __endpoint = e.getValue();
		    
		    if (endpoint.equals(__endpoint))
		    	return __pID;
		}
		*/
		System.err.println("Could not find endpoint " + endpoint + " !!!");
		
		return "";
	}
}
