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

package com.rapplogic.xbee.examples.wpan;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.transparent.XBeeFileProgrammer;

/**
 * This requires transparent mode and as such does not work for ZNet radios flashed in API mode
 * 
 * TODO file programmer that supports API mode
 * TODO allow serial number to be specified so that radio will only be programmed if serial matches e.g. SL=4008B48F, SH=13A200
 *  
 * @author andrew
 * 
 */
public class TestFileProgrammer {

		
	private TestFileProgrammer() throws Exception {

		File f = new File("xbee-base-thermistor.profile.txt");
		//File f = new File("xbee-remote-thermistor.profile.txt");
		//File f = new File("xbee-query.profile.txt");
		//File f = new File("xbee-base.profile.txt");
		//File f = new File("xbee-remote.profile.txt");
		
		
		XBeeFileProgrammer prog = new XBeeFileProgrammer("COM5", f);
		prog.execute();
	}

	public static void main(String[] args) throws Exception {
		// init log4j
		PropertyConfigurator.configure("log4j.properties");
		
		new TestFileProgrammer();
	}
}