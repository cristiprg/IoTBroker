package org.eclipse.leshan.standalone;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

class billingLog {

	private static long reservedTime;
	private static long parkedTime;
	private static long retrievedTime;
    private static double reserveBillRate;
    private static double parkBillRate;
    private static String parkingSpot;
    private static String vehicleId;
    
    private static double finalBill;
    
    public static void startBill(String vehicle,String ParkingSpot, long time, double reserveRate, double parkRate){
    	parkingSpot=vehicle;
    	vehicleId =ParkingSpot;
    	reservedTime =time; 
    	reserveBillRate = reserveRate;
    	parkBillRate = parkRate;
    	finalBill = 0;
    	parkedTime = 0;
    	retrievedTime = 0;
    }
    public static long getReservedTime(){
    	return reservedTime;
    }
    public static long getparkedTime(){
    	return parkedTime;
    }
    public static double getReserveBillRate(){
    	return reserveBillRate;
    }
    public static double getParkBillRate(){
    	return parkBillRate;
    }
    public static void setParkedTime(long val){
    	parkedTime = val;
    }
    public static void setBill(double b){
    	finalBill = b;
    }
    public static double getBill(){
    	return finalBill;
    }
    public static long getRetrievedTime(){
    	return retrievedTime;
    }
}

public class billInfo {
		/*
		 * Billing
		 */
		private static HashMap<String, billingLog> billLog = new HashMap<>();
	
		private static double PARK_BILL_RATE = 0.05;
		private static double RESERVE_BILL_RATE = 0.01;
		
		public static void startBilling(String vehicle, String ParkingSpot){
			billingLog bill = new billingLog();
			//Date now = new Date();  	
			//Long longTime = new Long(now.getTime()/1000);
			Long longTime = System.currentTimeMillis();
			System.out.println("Bill : parkTime : " + longTime);
			
			billingLog.startBill(vehicle,ParkingSpot,longTime,RESERVE_BILL_RATE,PARK_BILL_RATE);
			billLog.put(ParkingSpot, bill);
		}
		
		public static void updateBillToPark(String ParkingSpot){
			billingLog bill = billLog.get(ParkingSpot);
			
			//Date now = new Date();  	
			//Long parkTime = new Long(now.getTime()/1000);
			long parkTime = System.currentTimeMillis();
			System.out.println("Bill : parkTime : " + parkTime);
			bill.setParkedTime(parkTime);
			billLog.put(ParkingSpot, bill);
		}
		
		/**
		 * For the final bill and ending the bill calculation.
		 * @param ParkingSpot
		 */
		public static void calculateBill(String ParkingSpot){
			billingLog bill = billLog.get(ParkingSpot);
			
			//Date now = new Date();  	
			//Long endTime = new Long(now.getTime()/1000);
			long endTime = System.currentTimeMillis();
			System.out.println("Bill : retrivalTime : " + endTime);
			
			long startTime = bill.getReservedTime();
			long parkedTime = bill.getparkedTime();
			
			double rate1 = bill.getReserveBillRate();
			double rate2 = bill.getParkBillRate();
			
			double charge = (((parkedTime - startTime)* rate1) + ((endTime - parkedTime)* rate2))/1000;
			bill.setBill(charge);
			billLog.put(ParkingSpot, bill);
			billLog.remove(ParkingSpot);
		}
		
		/**
		 * For current bill.
		 * @param ParkingSpot
		 * @return
		 */
		public static double getBill(String ParkingSpot){
			billingLog bill = billLog.get(ParkingSpot);
			if(bill != null){
				long currentTime = System.currentTimeMillis();
				
				long startTime = bill.getReservedTime();
				long parkedTime = bill.getparkedTime();
				long retrievedTime = bill.getRetrievedTime();
				
				double rate1 = bill.getReserveBillRate();
				double rate2 = bill.getParkBillRate();
				double charge = 0;
				
				if(parkedTime != 0){
					if(retrievedTime != 0)
						charge = (retrievedTime - parkedTime)* rate2 + (parkedTime - startTime)* rate1;
					else
						charge = (currentTime - parkedTime)* rate2 + (parkedTime - startTime)* rate1;
				}
				else
					charge = (currentTime - startTime)* rate1;
				
				charge = charge/1000;
				return charge;
			}
			else
				return 0;
		}
		
}
