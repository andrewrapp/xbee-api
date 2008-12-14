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

import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.transparent.XBeeSerialProgrammer;

/**
 * Requires transparent mode
 * 
 * If you are using Arduino as a PC->XBee gatway, remove atmega prior to connecting XBeeApi shield.  
 * Note: The dot on the chip is should be closest to the end of the board. Also, place the jumpers in the USB position.
 * 
 * @author andrew
 * 
 */
public class TestSerialProgrammer {

		
	private TestSerialProgrammer(String[] args) throws Exception {
		
		XBeeSerialProgrammer prog = new XBeeSerialProgrammer();
		
		try {
			prog.open("COM5", "XBeeApi", 0, 9600);
			
			// get the address of the XBee
			prog.addCommand("ATMY");
			prog.addCommand("ATSL");
			prog.addCommand("ATSH");
			
			// set destination address
			//prog.addCommand("ATDL 5678");
			// set XBeeApi group address
			//prog.addCommand("ATID 1111");
			// enable api mode
			//prog.addCommand("ATAP 2");
			
			prog.execute();			
		} finally {
			prog.close();			
		}
	}

	public static void main(String[] args) throws Exception {
		// init log4j
		PropertyConfigurator.configure("log4j.properties");	
		new TestSerialProgrammer(args);
	}
}