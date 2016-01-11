package org.eclipse.leshan.standalone;

import java.net.ResponseCache;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class VehicleRegisterResource extends CoapResource {
	
	List<String> registeredVehicles = new ArrayList<>();
	
	private void log(String message){
		System.out.println("VehicleRegisterResource : "+ message);
	}
	
	public VehicleRegisterResource(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void handleGET(CoapExchange exchange){
		exchange.respond("vehicle salutes you");
	}
	
	@Override
	public void handlePOST(CoapExchange exchange){
		exchange.accept();
		
		OptionSet optionSet = exchange.getRequestOptions();
				
		String queryString = optionSet.getUriQueryString();
		if (queryString.startsWith("DriverID=")){
			String licensePlate = queryString.split("=")[1];
			
			if (registeredVehicles.contains(licensePlate)){							
				exchange.respond(ResponseCode.BAD_OPTION);
				
				log("Vehicle " + licensePlate + " already registered!");
			}
			else{
				registeredVehicles.add(licensePlate);
				exchange.respond(ResponseCode.CREATED);
				
				log("Vehicle " + licensePlate + " successfully registered!");
			}
			
		}
	}
}
