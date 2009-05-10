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

import com.rapplogic.xbee.util.ByteUtils;

/**
 * Provides access to XBee's 8 Digital (0-7) and 6 Analog (0-5) IO pins
 * 
 * @author andrew
 *
 */
public class IoSample {
	
	private RxResponseIoSample parent;
	
	private Integer dioMsb;
	private Integer dioLsb;
	
	private Integer analog0;
	private Integer analog1;
	private Integer analog2;
	private Integer analog3;
	private Integer analog4;
	private Integer analog5;

	public IoSample(RxResponseIoSample parent) {
		this.parent = parent;
	}

	public void setDioMsb(Integer dioMsb) {
		this.dioMsb = dioMsb;
	}

	public void setDioLsb(Integer dioLsb) {
		this.dioLsb = dioLsb;
	}
	
	public Integer getDioMsb() {
		return dioMsb;
	}

	public Integer getDioLsb() {
		return dioLsb;
	}

	public Integer getAnalog0() {
		return analog0;
	}

	public void setAnalog0(Integer analog0) {
		this.analog0 = analog0;
	}

	public Integer getAnalog1() {
		return analog1;
	}

	public void setAnalog1(Integer analog1) {
		this.analog1 = analog1;
	}

	public Integer getAnalog2() {
		return analog2;
	}

	public void setAnalog2(Integer analog2) {
		this.analog2 = analog2;
	}

	public Integer getAnalog3() {
		return analog3;
	}

	public void setAnalog3(Integer analog3) {
		this.analog3 = analog3;
	}

	public Integer getAnalog4() {
		return analog4;
	}

	public void setAnalog4(Integer analog4) {
		this.analog4 = analog4;
	}

	public Integer getAnalog5() {
		return analog5;
	}

	public void setAnalog5(Integer analog5) {
		this.analog5 = analog5;
	}

	/**
	 * Returns boolean value of DIO 0 pin
	 * Returns null if DIO 0 is not enabled 
	 * 
	 * @return
	 */
	public Boolean isD0On() {
		if (this.parent.isD0Enabled()) {
			return ByteUtils.getBit(dioLsb, 1);	
		}
		
		return null;
	}

	public Boolean isD1On() {
		if (this.parent.isD1Enabled()) {
			return ByteUtils.getBit(dioLsb, 2);	
		}
		
		return null;
	}
	
	public Boolean isD2On() {
		if (this.parent.isD2Enabled()) {
			return ByteUtils.getBit(dioLsb, 3);
		}
		
		return null;
	}	

	public Boolean isD3On() {
		if (this.parent.isD3Enabled()) {
			return ByteUtils.getBit(dioLsb, 4);	
		}
		
		return null;
	}
	
	public Boolean isD4On() {
		if (this.parent.isD4Enabled()) {
			return ByteUtils.getBit(dioLsb, 5);	
		}
		
		return null;
	}
	
	public Boolean isD5On() {
		if (this.parent.isD5Enabled()) {
			return ByteUtils.getBit(dioLsb, 6);	
		}
		
		return null;
	}
	
	public Boolean isD6On() {
		if (this.parent.isD6Enabled()) {
			return ByteUtils.getBit(dioLsb, 7);	
		}
		
		return null;
	}
	
	public Boolean isD7On() {
		if (this.parent.isD7Enabled()) {
			return ByteUtils.getBit(dioLsb, 8);	
		}
		
		return null;
	}
	
	public Boolean isD8On() {
		if (this.parent.isD8Enabled()) {
			return ByteUtils.getBit(dioMsb, 1);	
		}
		
		return null;	
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		// TODO only prefix with comma if not first entry written.  Use reflection
		if (this.getDioMsb() != null) {
			builder.append("dioMsb=" + ByteUtils.toBase2(this.getDioMsb()));	
		}
		
		if (this.getDioLsb() != null) {
			builder.append(",dioLsb=" + ByteUtils.toBase2(this.getDioLsb()));	
		}
		
		if (this.getAnalog0() != null) {
			builder.append(",analog0=" + this.getAnalog0());
		}

		if (this.getAnalog1() != null) {
			builder.append(",analog1=" + this.getAnalog1());
		}

		if (this.getAnalog2() != null) {
			builder.append(",analog2=" + this.getAnalog2());
		}

		if (this.getAnalog3() != null) {
			builder.append(",analog3=" + this.getAnalog3());
		}

		if (this.getAnalog4() != null) {
			builder.append(",analog4=" + this.getAnalog4());
		}

		if (this.getAnalog5() != null) {
			builder.append(",analog5=" + this.getAnalog5());
		}
		
		if (this.isD0On() != null) {
			builder.append(",digital0=" + (this.isD0On() ? "high" : "low"));
		}

		if (this.isD1On() != null) {
			builder.append(",digital1=" + (this.isD1On() ? "high" : "low"));
		}

		if (this.isD2On() != null) {
			builder.append(",digital2=" + (this.isD2On() ? "high" : "low"));
		}

		if (this.isD3On() != null) {
			builder.append(",digital3=" + (this.isD3On() ? "high" : "low"));
		}

		if (this.isD4On() != null) {
			builder.append(",digital4=" + (this.isD4On() ? "high" : "low"));
		}

		if (this.isD5On() != null) {
			builder.append(",digital5=" + (this.isD5On() ? "high" : "low"));
		}

		if (this.isD6On() != null) {
			builder.append(",digital6=" + (this.isD6On() ? "high" : "low"));
		}
		
		if (this.isD7On() != null) {
			builder.append(",digital7=" + (this.isD7On() ? "high" : "low"));
		}

		if (this.isD8On() != null) {
			builder.append(",digital8=" + (this.isD8On() ? "high" : "low"));
		}
		
		return builder.toString();
	}
}