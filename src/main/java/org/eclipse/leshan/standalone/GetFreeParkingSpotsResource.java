package org.eclipse.leshan.standalone;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.google.gson.Gson;

public class GetFreeParkingSpotsResource extends CoapResource {
	
	private BrokerState brokerState = BrokerState.getInstance();
	
	public GetFreeParkingSpotsResource(String name) {
		super(name);
	}
	
	@Override
	public void handleGET(CoapExchange exchange){
		
		Gson gson = new Gson();
		
		System.out.println(gson.toJson(brokerState.getFreeParkingSpots()));
		
		
		exchange.respond(gson.toJson(brokerState.getFreeParkingSpots()));
	}
		
}
