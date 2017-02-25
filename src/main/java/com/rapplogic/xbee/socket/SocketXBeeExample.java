package com.rapplogic.xbee.socket;

import com.rapplogic.xbee.XBeeConnection;
import com.rapplogic.xbee.api.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SocketXBeeExample {

	private final static Logger log = Logger.getLogger(SocketXBeeExample.class);
	
	public static void main(String[] args) throws UnknownHostException, XBeeException, IOException {
		PropertyConfigurator.configure("log4j.properties");
  
		try {
			// must disable start checks until bug fixed
			XBee xbee = new XBee(new XBeeConfiguration().withStartupChecks(false));
			xbee.initProviderConnection((XBeeConnection)new SocketXBeeConnection("pi", 9000));

			
			for (int i = 0; i < 3; i++) {
				log.debug("Sending AI command");
				
				try {
					AtCommandResponse response = (AtCommandResponse) xbee.sendSynchronous(new AtCommand("AI"));
					log.debug("Received AI response " + response);
				} catch (XBeeException e) {
					if (e.getCause() != null && e.getCause() instanceof SocketException) {
						// socket disconnected.. java.net.SocketException: Broken pipe
						// reconnect
						throw e;
					}
				}

				Thread.sleep(1000);
			}
			
			log.debug("Done");
			
		} catch (Throwable t) {
			log.error("xbee socket client failed", t);
		}

	}
}
