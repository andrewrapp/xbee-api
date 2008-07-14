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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.XBeeException;

/**
 * @author Andrew Rapp
 *
 */
public class XBeeSerialProgrammer extends SerialAsciiComm {

	private final static Logger log = Logger.getLogger(XBeeSerialProgrammer.class);
	
	final long timeout = 10000;
	private List<String> commandList = new ArrayList<String>();

	public void open(String port, String appName, int timeout, int baudRate) throws XBeeException {
		try {
			this.openSerialPort(port, appName, timeout, baudRate);	
		} catch (Exception e) {
			throw new XBeeException(e);
		}
		
	}
	
	/**
	 * Add command (e.g. ATMY A1F0) Don't add +++ command or exit command or add line breaks.
	 * Don't forget ATWR if you want to save your changes
	 *
	 * @param command
	 */
	public void addCommand(String command) {
		commandList.add(command);
	}

	public void execute() throws IOException, InterruptedException {

		// enter command mode
		this.sendCommand("+++");

		for (String cmd : commandList) {
			this.sendCommand(cmd + "\r\n");
		}

		// quit
		this.sendCommand("ATCN\r\n");
	}

	protected Object getLock() {
		return this;
	}

	/**
	 * Sends command to XBeeApi and waits for response.  If timeout occurs before response, a
	 * runtime exception is thrown.
	 *
	 * TODO handle commands that return multiple lines (ATVL)
	 * 
	 * @param cmd
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void sendCommand(String cmd) throws IOException, InterruptedException {

		synchronized(this.getLock()) {
			log.debug("Sending command: " + cmd);
			this.getOutputStream().write(cmd.getBytes());
			this.getOutputStream().flush();

			long start = System.currentTimeMillis();
			this.getLock().wait(timeout);

			if (System.currentTimeMillis() - start >= timeout) {
				throw new RuntimeException("Timeout exceed with no response");
			}

			if ("ERROR".equals(this.getLastResponse())) {
				throw new RuntimeException("Command failed with ERROR response");
			} else {
				log.debug("Command response: " + this.getLastResponse());
			}
		}
	}
}