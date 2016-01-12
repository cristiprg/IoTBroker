package org.eclipse.leshan.standalone.model;

public class ParkingSpot {

	private final String pID; 
	private String state;
	private String licensePlate;
	private final String endpoint;
	private float billingAmount;
	
	public ParkingSpot(String endpoint, String pID) {
		this.pID = pID;
		this.state = "free";
		this.licensePlate = "";
		this.endpoint = endpoint;
		billingAmount = 0.01f;
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
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
}
