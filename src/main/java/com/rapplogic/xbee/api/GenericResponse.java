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

import java.io.IOException;

/**
 * Container for unknown response
 * <p/>
 * @author andrew
 *
 */
public class GenericResponse extends XBeeResponse {
		
	private int genericApiId;

	public GenericResponse() {

	}
	
	public int getGenericApiId() {
		return genericApiId;
	}

	public void setGenericApiId(int genericApiId) {
		this.genericApiId = genericApiId;
	}
	
	public void parse(IPacketParser parser) throws IOException {
		//eat packet bytes -- they will be save to bytearray and stored in response
		parser.readRemainingBytes();
		// TODO gotta save it because it isn't know to the enum apiId won't
		this.setGenericApiId(parser.getIntApiId());		
	}
}