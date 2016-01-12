package org.eclipse.leshan.standalone.model;

import java.util.ArrayList;

public class ParkingSpot {

	private final String pID; 
	private String state;
	private String licensePlate;
	private final String endpoint;
	private float billingAmount;
	private ArrayList<String> history;
	
	public ParkingSpot(String endpoint, String pID) {
		this.pID = pID;
		this.state = "free";
		this.licensePlate = "";
		this.endpoint = endpoint;
		billingAmount = 0;
		history = new ArrayList<>();
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
		
		if (state.equals("free") && !licensePlate.equals("")){
			history.add(licensePlate + " " + billingAmount + " $$$"); 			
			billingAmount = 0;
		}
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}

	public float getBillingAmount() {
		return billingAmount;
	}

	public void setBillingAmount(float billingAmount) {
		this.billingAmount = billingAmount;
	}

	public String getpID() {
		return pID;
	}

	public String getEndpoint() {
		return endpoint;
	}
	
	public String getReport(){
		StringBuilder s = new StringBuilder("<html>");
		for (String str : history)
			s.append(str + "<br>");
		
		s.append("</html>");
		return s.toString();
	}
}
