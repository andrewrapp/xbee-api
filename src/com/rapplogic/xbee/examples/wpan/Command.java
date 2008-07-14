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

/**
 * This goes along with XBeePing/Pong
 * 
 * @author andrew
 *
 */
public class Command {

	public final static String COMMAND = "C";
	public final static String ACK = "A";
	
	private String type;
	private Integer sequence;
	
	public Command(String type, Integer sequence) {
		this.type = type;
		this.sequence = sequence;
	}

	public String getType() {
		return type;
	}

	public Integer getSequence() {
		return sequence;
	}
	
	public static Command parse(String str) {
		String type = str.substring(0, 1);
		Integer sequence = new Integer(str.substring(1));
		
		return new Command(type, sequence);
	}
	
	public String toString() {
		return "Type=" + type + ",Sequence=" + sequence;
	}
}
