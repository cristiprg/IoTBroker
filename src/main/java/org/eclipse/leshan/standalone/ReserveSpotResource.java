package org.eclipse.leshan.standalone;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.request.WriteRequest.Mode;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.server.LwM2mServer;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.standalone.servlet.EventServlet;
import org.omg.PortableInterceptor.SUCCESSFUL;


/**
 * 	vehicle -> broker
 *	POST /ReserveSpot?SpotID=ID&DriverID=ID
 *
 */
public class ReserveSpotResource extends CoapResource{

	private BrokerState brokerState = BrokerState.getInstance();
	private LeshanServer leshanServer = null;
	private EventServlet eventServlet = null;
	
	private static final String TEXT_COLOR_TARGET = "/3341/0/5527";
	private static final String VEHICLE_ID_TARGET = "/32700/0/32802";
	private static final String JOYSTICK_TARGET = "/3345/0/5703";
	
	private static final long TIMEOUT = 5000; // ms
	
	private void log(String message){
		System.out.println("ReserveSpotResource : "+ message);
	}
	
	
	public ReserveSpotResource(String name, LwM2mServer leshanServer, EventServlet eventServlet) {
		super(name);
		this.leshanServer = (LeshanServer)leshanServer;
		this.eventServlet = eventServlet;
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
		Client client = leshanServer.getClientRegistry().get(brokerState.getEndpointByParkingSpotID(parkingSpotID));
		
		// 1. Leshan Server writes licenseplate to parkingspot
		LwM2mSingleResource node = LwM2mSingleResource.newResource(32802, licensePlate, Type.STRING);			
		leshanServer.send(client, new WriteRequest(Mode.REPLACE, null, VEHICLE_ID_TARGET, node));		
		
		// 2. Leshan Server writes color to parking spot
		node = LwM2mSingleResource.newResource(5527, "orange", Type.STRING);
		leshanServer.send(client, new WriteRequest(Mode.REPLACE, null, TEXT_COLOR_TARGET, node));
		
		// 3. Leshan Server turns OBS on to parking spot joystick Y
		ObserveRequest request = new ObserveRequest(JOYSTICK_TARGET);
        ObserveResponse cResponse = leshanServer.send(client, request, TIMEOUT);
        cResponse.getObservation().addListener(eventServlet.getObservationRegistryListener());
		
		// 4. Change the internal state of parking spot to "reserved"
		//brokerState.changeParkingSpotState(parkingSpotID, "reserved");
        brokerState.reserverParkingSpotForVehicle(parkingSpotID, licensePlate);
		log("Vehicle " + licensePlate + " successfully reserved " + parkingSpotID);
		
		// 5. Start billing process
		billInfo.startBilling(licensePlate, parkingSpotID);
		
		exchange.respond(ResponseCode.CREATED);
	}

}
