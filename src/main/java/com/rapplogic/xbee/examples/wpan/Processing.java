package com.rapplogic.xbee.examples.wpan;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;

public class Processing {
	XBee xbee;
	Queue<XBeeResponse> queue = new ConcurrentLinkedQueue<XBeeResponse>();
	boolean message;
	XBeeResponse response;
	  
	void setup() {
	  try { 
	    //optional.  set up logging
	    PropertyConfigurator.configure(dataPath("")+"log4j.properties");

	    xbee = new XBee();
	    // replace with your COM port
	    xbee.open("/dev/tty.usbserial-A6005v5M", 9600);

	    xbee.addPacketListener(new PacketListener() {
	      public void processResponse(XBeeResponse response) {
	        queue.offer(response);
	      }
	    }
	    );
	  } 
	  catch (Exception e) {
	    System.out.println("XBee failed to initialize");
	    e.printStackTrace();
	    System.exit(1);
	  }
	}

	void draw() {
	  try {
	    readPackets();
	  } 
	  catch (Exception e) {
	    e.printStackTrace();
	  }
	}

	void readPackets() throws Exception {

	  while ((response = queue.poll()) != null) {
	    // we got something!
	    try {
	      RxResponseIoSample ioSample = (RxResponseIoSample) response;

	      println("We received a sample from " + ioSample.getSourceAddress());

	      if (ioSample.containsAnalog()) {
	        println("10-bit temp reading (pin 19) is " +
	          ioSample.getSamples()[0].getAnalog1());
	      }
	    } 
	    catch (ClassCastException e) {
	      // not an IO Sample
	    }
	  }
	}
	
	void println(String s) {};
	String dataPath(String s) {return null;};
}
