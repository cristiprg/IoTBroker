/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.standalone.servlet;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.request.WriteRequest.Mode;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.client.ClientRegistryListener;
import org.eclipse.leshan.server.observation.ObservationRegistryListener;
import org.eclipse.leshan.standalone.BrokerState;
import org.eclipse.leshan.standalone.servlet.json.ClientSerializer;
import org.eclipse.leshan.standalone.servlet.json.LwM2mNodeSerializer;
import org.eclipse.leshan.standalone.servlet.log.CoapMessage;
import org.eclipse.leshan.standalone.servlet.log.CoapMessageListener;
import org.eclipse.leshan.standalone.servlet.log.CoapMessageTracer;
import org.eclipse.leshan.standalone.utils.EventSource;
import org.eclipse.leshan.standalone.utils.EventSourceServlet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EventServlet extends EventSourceServlet {

    private static final String EVENT_DEREGISTRATION = "DEREGISTRATION";

    private static final String EVENT_UPDATED = "UPDATED";

    private static final String EVENT_REGISTRATION = "REGISTRATION";

    private static final String EVENT_NOTIFICATION = "NOTIFICATION";

    private static final String EVENT_COAP_LOG = "COAPLOG";

    private static final String QUERY_PARAM_ENDPOINT = "ep";

    private static final long serialVersionUID = 1L;
    
    private static final String TEXT_COLOR_TARGET = "/3341/0/5527";
    private static final String PARKING_SPOT_ID_TARGET = "/32700/0/32800";
    private static final long TIMEOUT = 5000; // ms

    private static final Logger LOG = LoggerFactory.getLogger(EventServlet.class);

    private final Gson gson;

    private final CoapMessageTracer coapMessageTracer;

    private final LeshanServer server;

    private Set<LeshanEventSource> eventSources = new ConcurrentHashSet<>();
    
    private BrokerState brokerState = BrokerState.getInstance();

    private final ClientRegistryListener clientRegistryListener = new ClientRegistryListener() {

        @Override
        public void registered(Client client) {
            String jClient = EventServlet.this.gson.toJson(client);
            sendEvent(EVENT_REGISTRATION, jClient, client.getEndpoint());
            
            // Get the parking spot ID, which is different from client endpoint.
            // For this, a request is sent to the client for the resource  PARKING_SPOT_ID_TARGET
            ReadRequest request = new ReadRequest(PARKING_SPOT_ID_TARGET);
            ReadResponse cResponse = server.send(client, request, TIMEOUT);
            
            JSONObject obj = new JSONObject(cResponse);
            String parkingSpotID = obj.getJSONObject("content").getString("value");
            
            brokerState.registerParkingSpot(client.getEndpoint(), parkingSpotID);                       
        }

        @Override
        public void updated(Client clientUpdated) {
            String jClient = EventServlet.this.gson.toJson(clientUpdated);
            sendEvent(EVENT_UPDATED, jClient, clientUpdated.getEndpoint());            
        };

        @Override
        public void unregistered(Client client) {
            String jClient = EventServlet.this.gson.toJson(client);
            sendEvent(EVENT_DEREGISTRATION, jClient, client.getEndpoint());                       
        }
    };

    public ObservationRegistryListener getObservationRegistryListener(){
    	return this.observationRegistryListener;
    }
    
    private final ObservationRegistryListener observationRegistryListener = new ObservationRegistryListener() {

        @Override
        public void cancelled(Observation observation) {
        }

		@Override
		public void newValue(Observation observation, LwM2mNode value) {
			if (LOG.isDebugEnabled()){
				LOG.debug("Received notification from [{}] containing value [{}]", observation.getPath(),
						value.toString());
			}
			Client client = server.getClientRegistry().findByRegistrationId(observation.getRegistrationId());

			if (client != null) {
				String data = new StringBuffer("{\"ep\":\"").append(client.getEndpoint()).append("\",\"res\":\"")
						.append(observation.getPath().toString()).append("\",\"val\":").append(gson.toJson(value))
						.append("}").toString();

				sendEvent(EVENT_NOTIFICATION, data, client.getEndpoint());

				if (value instanceof LwM2mSingleResource) {
					LwM2mSingleResource value2 = (LwM2mSingleResource) value;

					String parkingSpotID = brokerState.getParkingSpotByEndpoint(client.getEndpoint());
					int resourceID = value2.getId();
					
					// type of newState is subject to change, for now, it is Float
					//String newState = (String) value2.getValue();
					int newState = 0;
					
					if (value2.getType() == Type.FLOAT){ 					
						newState =  (int)( (Double) value2.getValue() ).intValue();
					}
					else{
						System.out.println("not float ... NOOOO, it is " + String.valueOf(value2.getType()));
					}

					// if the resource is the "Parking Spot State", as indicated in the protocol proposal Table4, then we check the new state
					LwM2mSingleResource node = null;
					if (resourceID == 5703) {
						switch (newState) {
						case -100:
							// change to free
							brokerState.changeParkingSpotState(parkingSpotID, "free");
							
							node = LwM2mSingleResource.newResource(5527, "green", Type.STRING);
							server.send(client, new WriteRequest(Mode.REPLACE, null, TEXT_COLOR_TARGET, node));
							break;
						case 100:
							// change to occupied
							brokerState.changeParkingSpotState(parkingSpotID, "occupied");	
							
							node = LwM2mSingleResource.newResource(5527, "red", Type.STRING);
							server.send(client, new WriteRequest(Mode.REPLACE, null, TEXT_COLOR_TARGET, node));
							break;
						default:
							System.out.println("ERROR: Unkown state change " + newState);
							break;
						}

					}
				}
			}
		}

        @Override
        public void newObservation(Observation observation) {
        }
    };

    public EventServlet(LeshanServer server, int securePort) {
        this.server = server;
        server.getClientRegistry().addListener(this.clientRegistryListener);
        server.getObservationRegistry().addListener(this.observationRegistryListener);

        // add an interceptor to each endpoint to trace all CoAP messages
        coapMessageTracer = new CoapMessageTracer(server.getClientRegistry());
        for (Endpoint endpoint : server.getCoapServer().getEndpoints()) {
            endpoint.addInterceptor(coapMessageTracer);
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Client.class, new ClientSerializer(securePort));
        gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeSerializer());
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        this.gson = gsonBuilder.create();
    }

    private synchronized void sendEvent(String event, String data, String endpoint) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching {} event from endpoint {}", event, endpoint);
        }

        for (LeshanEventSource eventSource : eventSources) {
            if (eventSource.getEndpoint() == null || eventSource.getEndpoint().equals(endpoint)) {
                eventSource.sentEvent(event, data);
            }
        }
    }

    class ClientCoapListener implements CoapMessageListener {

        private final String endpoint;

        ClientCoapListener(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void trace(CoapMessage message) {
            String coapLog = EventServlet.this.gson.toJson(message);
            sendEvent(EVENT_COAP_LOG, coapLog, endpoint);
        }

    }

    private void cleanCoapListener(String endpoint) {
        // remove the listener if there is no more eventSources for this endpoint
        for (LeshanEventSource eventSource : eventSources) {
            if (eventSource.getEndpoint() == null || eventSource.getEndpoint().equals(endpoint)) {
                return;
            }
        }
        coapMessageTracer.removeListener(endpoint);
    }

    @Override
    protected EventSource newEventSource(HttpServletRequest req) {
        String endpoint = req.getParameter(QUERY_PARAM_ENDPOINT);
        return new LeshanEventSource(endpoint);
    }

    private class LeshanEventSource implements EventSource {

        private String endpoint;
        private Emitter emitter;

        public LeshanEventSource(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void onOpen(Emitter emitter) throws IOException {
            this.emitter = emitter;
            eventSources.add(this);
            if (endpoint != null) {
                coapMessageTracer.addListener(endpoint, new ClientCoapListener(endpoint));
            }
        }

        @Override
        public void onClose() {
            cleanCoapListener(endpoint);
            eventSources.remove(this);
        }

        public void sentEvent(String event, String data) {
            try {
                emitter.event(event, data);
            } catch (IOException e) {
                onClose();
            }
        }

        public String getEndpoint() {
            return endpoint;
        }
    }
}
