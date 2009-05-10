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

package com.rapplogic.xbee.api.zigbee;

import java.io.IOException;

import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.util.IntArrayInputStream;

//TODO Now supported by series 1 XBee. parseIoSample now needs to handle series 1 and 2

public class ZNetRemoteAtResponse extends AtCommandResponse {
		
	private XBeeAddress64 remoteAddress64;
	private XBeeAddress16 remoteAddress16;
	
	public ZNetRemoteAtResponse() {

	}

	public boolean isSixteenBitAddressUnknown() {
		return remoteAddress16.getMsb() == 0xff && remoteAddress16.getLsb() == 0xfe;
	}

	public XBeeAddress64 getRemoteAddress64() {
		return remoteAddress64;
	}

	public void setRemoteAddress64(
			XBeeAddress64 sixtyFourBitResponderAddress) {
		this.remoteAddress64 = sixtyFourBitResponderAddress;
	}

	public XBeeAddress16 getRemoteAddress16() {
		return remoteAddress16;
	}

	public void setRemoteAddress16(
			XBeeAddress16 sixteenBitResponderAddress) {
		this.remoteAddress16 = sixteenBitResponderAddress;
	}

	/**
	 * @deprecated use getCommand instead
	 * @return
	 * Mar 4, 2009
	 */
	public String getCommandName() {
		return super.getCommand();
	}
	
	/**
	 * @deprecated use getValue instead
	 * @return
	 * Mar 4, 2009
	 */
	public int[] getCommandData() {
		return super.getValue();
	}
	
	/**
	 * Returns the sample portion of a ZNetRxIoSampleResponse, based on a IS (force sample) response.
	 * Only the sample data is populated; other fields are null or default.
	 * 
	 * @return
	 * @throws IOException
	 */
	public ZNetRxIoSampleResponse parseIsSample() throws IOException {
		
		if (!this.getCommand().equals("IS")) {
			throw new RuntimeException("This method is only applicable to the IS command");
		}
		
		IntArrayInputStream in = new IntArrayInputStream(this.getCommandData());
		ZNetRxIoSampleResponse sample = new ZNetRxIoSampleResponse();
		sample.parse(in);
		
		return sample;
	}
	
	public String toString() {
		return super.toString() +
			",remoteAddress64=" + this.remoteAddress64 +
			",remoteAddress16=" + this.remoteAddress16;
	}
}