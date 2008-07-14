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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.rapplogic.xbee.api.XBeeException;

/**
 * TODO need to support API mode for radios ZNet radios that do not support transparent when flashed with API firmware
 * TODO add serial number support so radio will only be programmed if serial number in file matches the radio's serial 
 * 
 */
public class XBeeFileProgrammer extends SerialAsciiComm {
	
	private File profile;
	private String port;
	
	public XBeeFileProgrammer(String port, File profile) {
		this.profile = profile;
		this.port = port;
	}
	
	public void execute() throws XBeeException, IOException, InterruptedException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(profile)));
		
		XBeeSerialProgrammer prog = new XBeeSerialProgrammer();
		
		boolean atleastone = false;
		String line = null;
		
		while ((line = reader.readLine()) != null) {
			
			line = line.trim();
			
			if (line.startsWith("#")) {
				// this is a comment
				continue;
			} else if (line.length() == 0) {
				// blank line
				continue;
			}
			
			prog.addCommand(line.trim());
			atleastone = true;
		}
		
		if (!atleastone) {
			throw new RuntimeException("File: " + profile.getAbsolutePath() + ", does not contain any commands");
		}
		
		try {
			prog.open(port, "XBeeApi", 0, 9600);
			prog.execute();			
		} finally {
			prog.close();			
		}
	}
	
	protected Object getLock() {
		return this;
	}
}