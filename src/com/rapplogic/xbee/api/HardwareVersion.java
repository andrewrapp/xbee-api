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

package com.rapplogic.xbee.api;


/**
 * Represents a XBee Address.
 * <p/>
 * @author andrew
 *
 */
public class HardwareVersion {

	public enum RadioType {
		SERIES1("Series 1"),
		SERIES1_PRO("Series 1 Pro"),
		SERIES2("Series 2"),
		SERIES2_PRO("Series 2 Pro"),
		SERIES2B_PRO("Series 2B Pro"),
		UNKNOWN("Unknown");
		
		private String name;
		
		RadioType(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	public static RadioType parse(AtCommandResponse response) throws XBeeException {
		
		if (!response.getCommand().equals("HV")) {
			throw new IllegalArgumentException("This is only applicable to the HV command");
		}
		
		if (!response.isOk()) {
			throw new XBeeException("Attempt to query HV parameter failed");
		}
		
		switch (response.getValue()[0]) {
		case 0x17:
			return RadioType.SERIES1;
		case 0x18:
			return RadioType.SERIES1_PRO;
		case 0x19:
			return RadioType.SERIES2;
		case 0x1a:
			return RadioType.SERIES2_PRO;
		case 0x1e:
			return RadioType.SERIES2B_PRO;
		}

		return RadioType.UNKNOWN;
	}
}
