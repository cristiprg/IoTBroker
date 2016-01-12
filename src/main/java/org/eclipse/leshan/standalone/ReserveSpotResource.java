package org.eclipse.leshan.standalone;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.omg.PortableInterceptor.SUCCESSFUL;


/**
 * 	vehicle -> broker
 *	POST /ReserveSpot?SpotID=ID&DriverID=ID
 *
 */
public class ReserveSpotResource extends CoapResource{

	private BrokerState brokerState = BrokerState.getInstance();
	
	private void log(String message){
		System.out.println("ReserveSpotResource : "+ message);
	}
	
	
	public ReserveSpotResource(String name) {
		super(name);
	}
	
	@Override
	public void handlePOST(CoapExchange exchange) {
		OptionSet optionSet = exchange.getRequestOptions();
		String queryString = optionSet.getUriQueryString();
		String licensePlate = "";
		String parkingSpotID = "";
		
		// split on &
		String[] queryVariables = queryString.split("&");
		
		// for each variable, split on = and extract the values
		for (String queryVariable : queryVariables){			
			if (queryVariable.startsWith("DriverID=")){
				licensePlate = queryVariable.split("=")[1];			
			}else if (queryVariable.startsWith("SpotID=")){
				parkingSpotID = queryVariable.split("=")[1];
			}
		}
		
		// sanity check: in case either car or parking spot is not registered, return failure
		if (!brokerState.isVehicleRegistered(licensePlate) || !brokerState.isParkingSpotRegistered(parkingSpotID)){
			log("Error making reservation - car: " + licensePlate + " or parking spot: " + parkingSpotID + " not registered.");
			exchange.respond(ResponseCode.BAD_OPTION);
		}

		// now, we have "licensePlate reserves parkingSpotID"
		
		// 1. Leshan Server writes licenseplate to parkingspot
		// 2. Leshan Server writes color to parking spot
		// 3. Leshan Server turns OBS on to parking spot
		
		// 4. Change the internal state of parking spot to "reserved"
		brokerState.changeParkingSpotState(parkingSpotID, "reserved");	
		log("Vehicle " + licensePlate + " successfully reserved " + parkingSpotID);
		
		exchange.respond(ResponseCode.CREATED);
	}

}
