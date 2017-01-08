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

package com.rapplogic.xbee.api.wpan;

import java.io.IOException;

import com.rapplogic.xbee.api.IPacketParser;
import com.rapplogic.xbee.util.ByteUtils;

public class RxResponse extends RxBaseResponse {
	
	private int[] data;
	
	public RxResponse() {

	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}

	public void parse(IPacketParser parser) throws IOException {		
		int[] payload = new int[parser.getLength().getLength() - parser.getFrameDataBytesRead()];
		
		int bytesRead = parser.getFrameDataBytesRead();
		
		for (int i = 0; i < parser.getLength().getLength() - bytesRead; i++) {
			payload[i] = parser.read("Payload byte " + i);
			//log.debug("rx data payload [" + i + "] " + payload[i]);
		}				
		
		this.setData(payload);	
	}
	
	public String toString() {
		return super.toString() +
			",data=" + ByteUtils.toBase16(this.data);
	}	
}