package com.rapplogic.xbee;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.log4j.Logger;

/**
 * This is a bit of a misnomer as we are not connected to anything.  This class serves as a buffer
 * and provides protocol independent input/output streams for xbee-api 
 * 
 * @author andrew
 *
 */
public abstract class AbstractXBeeConnection implements XBeeConnection {

	private final static Logger log = Logger.getLogger(XBeeConnection.class);
	
	// we write to this to provide xbee-api with RX packets 
	private PipedOutputStream pipeToInputStream = new PipedOutputStream();
	// xbee api reads from this to parse packets
	private PipedInputStream xbeeInputStream = new PipedInputStream();
	
	public void init() {
		
	}
	
	public AbstractXBeeConnection() {
		try {
			xbeeInputStream.connect(pipeToInputStream);	
		} catch (IOException e) {
			// won't happen
		}
	}

	//Writes the byte to the input stream to be parsed
	public void write(int b) throws IOException {
		pipeToInputStream.write(b);	
		
		// TODO ideally we don't notify until end of packet but this should be fine, it will just block on read
		
		// critical: notify XBee that data is available
		synchronized(this) {
			log.debug("Notifying the XBee input stream thread that new data is available");
			this.notify();
		}
	}

	public abstract OutputStream getOutputStream();
	
	public InputStream getInputStream() {
		return this.xbeeInputStream;
	}
}
