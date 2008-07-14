/**
 * Copyright (c) 2008 Andrew Rapp. All rights reserved.
 *  
 * This file is part of XBee-API.
 *  
 * XBee-API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * XBee-API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rapplogic.xbee.transparent;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.RxTxSerialComm;

public abstract class SerialAsciiComm extends RxTxSerialComm {

	private final static Logger log = Logger.getLogger(SerialAsciiComm.class);
	
	private String lastResponse = null;	
	private StringBuffer strBuf = new StringBuffer();

	public final int TIMEOUT = 1000;
	
	public final char CR = 13; // /r
	public final char LF = 10; // /n
	
	public SerialAsciiComm() {
		super();
	}
	
	protected abstract Object getLock();
	
	protected void handleSerialData() throws IOException {

		//log.debug("handleSerialData(): thread [" + Thread.currentThread().getName() + "]");
		
		byte[] readBuffer = new byte[4];
		
		// read data
		while (this.getInputStream().available() > 0) {
			int numBytes = this.getInputStream().read(readBuffer);

			//log.debug("read " + numBytes + " bytes");
			
			for (int i = 0; i < numBytes; i++) {

				int ch = (int) readBuffer[i];

				//log.debug("Received: [" + (int)ch + "]" + (char)ch);
				
				// XBeeApi responds with only a CR (13)
				if (!(ch == CR || ch == LF)) {
					// don't add CR/LF
					strBuf.append((char) readBuffer[i]);
				}

				// CR
				if ((int) readBuffer[i] == CR) {
					
					synchronized(this.getLock()) {
						//log.debug(strBuf.toString());
						lastResponse = strBuf.toString();
						
						// wake up wait thread
						//log.debug("waking up wait thread");
						this.getLock().notify();
					}
					
					strBuf = new StringBuffer();
				}
			}
		}
		
		//System.out.print("handleSerialData: Exiting");
	}
	
	/**
	 * Sends Command in ASCII.  
	 * 
	 * @param commandSequence
	 * @param type
	 * @throws IOException
	 */
	protected void sendCommand(String commandSequence, String type) throws IOException {
		String fullCmd = type + commandSequence;
		
		log.debug("Sending " + fullCmd);
		
		this.getOutputStream().write((fullCmd + CR).getBytes());
		this.getOutputStream().flush();		
	}
	
	public String getLastResponse() {
		return lastResponse;
	}

	public StringBuffer getStrBuf() {
		return strBuf;
	}
}