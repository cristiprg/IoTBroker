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
package org.eclipse.leshan.standalone;

import java.awt.EventQueue;
import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.core.server.resources.ResourceAttributes;
import org.eclipse.californium.core.server.resources.ResourceObserver;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.impl.SecurityRegistryImpl;
import org.eclipse.leshan.standalone.gui.ApplicationWindow;
import org.eclipse.leshan.standalone.gui.util.StateWatcher;
import org.eclipse.leshan.standalone.servlet.ClientServlet;
import org.eclipse.leshan.standalone.servlet.EventServlet;
import org.eclipse.leshan.standalone.servlet.ObjectSpecServlet;
import org.eclipse.leshan.standalone.servlet.SecurityServlet;
import org.eclipse.leshan.util.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeshanStandalone {

    private static final Logger LOG = LoggerFactory.getLogger(LeshanStandalone.class);

    private Server server;
    private LeshanServer lwServer;
    private BrokerState brokerState = BrokerState.getInstance();
    private ApplicationWindow applicationWindow;

    public void start() {
        // Use those ENV variables for specifying the interface to be bound for coap and coaps
        String iface = System.getenv("COAPIFACE");
        String ifaces = System.getenv("COAPSIFACE");

        // Build LWM2M server
        LeshanServerBuilder builder = new LeshanServerBuilder();
        if (iface != null && !iface.isEmpty()) {
            builder.setLocalAddress(iface.substring(0, iface.lastIndexOf(':')),
                Integer.parseInt(iface.substring(iface.lastIndexOf(':') + 1, iface.length())));
        }
        if (ifaces != null && !ifaces.isEmpty()) {
            builder.setLocalAddressSecure(ifaces.substring(0, ifaces.lastIndexOf(':')),
                Integer.parseInt(ifaces.substring(ifaces.lastIndexOf(':') + 1, ifaces.length())));
        }

        // Get public and private server key
        PrivateKey privateKey = null;
        PublicKey publicKey = null;
        try {
            // Get point values
            byte[] publicX = Hex
                    .decodeHex("fcc28728c123b155be410fc1c0651da374fc6ebe7f96606e90d927d188894a73".toCharArray());
            byte[] publicY = Hex
                    .decodeHex("d2ffaa73957d76984633fc1cc54d0b763ca0559a9dff9706e9f4557dacc3f52a".toCharArray());
            byte[] privateS = Hex
                    .decodeHex("1dae121ba406802ef07c193c1ee4df91115aabd79c1ed7f4c0ef7ef6a5449400".toCharArray());

            // Get Elliptic Curve Parameter spec for secp256r1
            AlgorithmParameters algoParameters = AlgorithmParameters.getInstance("EC");
            algoParameters.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec parameterSpec = algoParameters.getParameterSpec(ECParameterSpec.class);

            // Create key specs
            KeySpec publicKeySpec = new ECPublicKeySpec(new ECPoint(new BigInteger(publicX), new BigInteger(publicY)),
                    parameterSpec);
            KeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(privateS), parameterSpec);

            // Get keys
            publicKey = KeyFactory.getInstance("EC").generatePublic(publicKeySpec);
            privateKey = KeyFactory.getInstance("EC").generatePrivate(privateKeySpec);

            builder.setSecurityRegistry(new SecurityRegistryImpl(privateKey, publicKey));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidParameterSpecException e) {
            LOG.warn("Unable to load RPK.", e);
        }

        lwServer = builder.build();
        lwServer.start();

        // Now prepare and start jetty
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = System.getProperty("PORT");
        }
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }
        server = new Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext();
        root.setContextPath("/");
        root.setResourceBase(this.getClass().getClassLoader().getResource("webapp").toExternalForm());
        root.setParentLoaderPriority(true);
        server.setHandler(root);

        // Create Servlet
        EventServlet eventServlet = new EventServlet(lwServer, lwServer.getSecureAddress().getPort());
        ServletHolder eventServletHolder = new ServletHolder(eventServlet);
        root.addServlet(eventServletHolder, "/event/*");

        ServletHolder clientServletHolder = new ServletHolder(new ClientServlet(lwServer, lwServer.getSecureAddress()
                .getPort()));
        root.addServlet(clientServletHolder, "/api/clients/*");

        ServletHolder securityServletHolder = new ServletHolder(new SecurityServlet(lwServer.getSecurityRegistry()));
        root.addServlet(securityServletHolder, "/api/security/*");

        ServletHolder objectSpecServletHolder = new ServletHolder(new ObjectSpecServlet(lwServer.getModelProvider()));
        root.addServlet(objectSpecServletHolder, "/api/objectspecs/*");

        // Start jetty
        try {
            server.start();
        } catch (Exception e) {
            LOG.error("jetty error", e);
        }
        
        try {
        	// highly inspired from http://stackoverflow.com/questions/12726801/avahi-not-able-to-find-service-creted-by-jmdns
        	// because there is a bug with the other ways to publish the service
        	
        	String service_type = "_coap._udp.local.";
        	String service_name = "BrokerService";
        	int service_port = 5683;
        	String service_key = "description";
        	String service_text = "IoTBroker";
        	HashMap<String, byte[]> properties = new HashMap<String, byte[]>();
        	properties.put(service_key, service_text.getBytes());
        	ServiceInfo service_info = ServiceInfo.create(service_type, service_name, service_port, 0, 0, true, properties);
        	JmDNS jmdns = JmDNS.create("localhost"); 
        	jmdns.registerService(service_info);
        	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        CoapServer coapServer = lwServer.getCoapServer();
        
        // Here we have all the resources listed
        coapServer.add(new VehicleRegisterResource("Register"));   
        coapServer.add(new GetFreeParkingSpotsResource("FreeParkingSpots"));
        coapServer.add(new ReserveSpotResource("ReserveSpot", lwServer, eventServlet));
    }   
    
    public void stop() {
        try {
            lwServer.destroy();
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public LeshanStandalone() {
		// start gui
    	System.out.println("Starting GUI ...");
    	applicationWindow = new ApplicationWindow();		
    	System.out.println("OK!");
    	    	
    	// start watcher
    	System.out.println("Starting watcher ...");
    	StateWatcher stateWatcher = new StateWatcher(applicationWindow);
    	brokerState.setStateWatcher(stateWatcher);
		new Thread(stateWatcher).start();
		
    	System.out.println("OK!");
}

    public static void main(String[] args) {
        new LeshanStandalone().start();        
    }
}
