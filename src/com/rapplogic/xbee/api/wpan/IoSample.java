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

public class IoSample {
		
	private int dioMsb;
	private int dioLsb;
	
	private int analog0;
	private int analog1;
	private int analog2;
	private int analog3;
	private int analog4;
	private int analog5;

	public IoSample() {

	}

	public void setDioMsb(int dioMsb) {
		this.dioMsb = dioMsb;
	}

	public void setDioLsb(int dioLsb) {
		this.dioLsb = dioLsb;
	}
	
	public int getDioMsb() {
		return dioMsb;
	}

	public int getDioLsb() {
		return dioLsb;
	}

	public int getAnalog0() {
		return analog0;
	}

	public void setAnalog0(int analog0) {
		this.analog0 = analog0;
	}

	public int getAnalog1() {
		return analog1;
	}

	public void setAnalog1(int analog1) {
		this.analog1 = analog1;
	}

	public int getAnalog2() {
		return analog2;
	}

	public void setAnalog2(int analog2) {
		this.analog2 = analog2;
	}

	public int getAnalog3() {
		return analog3;
	}

	public void setAnalog3(int analog3) {
		this.analog3 = analog3;
	}

	public int getAnalog4() {
		return analog4;
	}

	public void setAnalog4(int analog4) {
		this.analog4 = analog4;
	}

	public int getAnalog5() {
		return analog5;
	}

	public void setAnalog5(int analog5) {
		this.analog5 = analog5;
	}

	public boolean isD0On() {
		return ByteUtils.getBit(dioLsb, 1);
	}

	public boolean isD1On() {
		return ByteUtils.getBit(dioLsb, 2);
	}
	
	public boolean isD2On() {
		return ByteUtils.getBit(dioLsb, 3);
	}	

	public boolean isD3On() {
		return ByteUtils.getBit(dioLsb, 4);
	}
	
	public boolean isD4On() {
		return ByteUtils.getBit(dioLsb, 5);
	}
	
	public boolean isD5On() {
		return ByteUtils.getBit(dioLsb, 6);
	}
	
	public boolean isD6On() {
		return ByteUtils.getBit(dioLsb, 7);
	}
	
	public boolean isD7On() {
		return ByteUtils.getBit(dioLsb, 8);
	}
	
	public boolean isD8On() {
		return ByteUtils.getBit(dioMsb, 1);
	}
	
	// TODO remaining bits
	public String toString() {
		return "dioMsb=" + ByteUtils.toBase2(dioMsb) + ",dioLsb=" + ByteUtils.toBase2(this.dioLsb); 
	}
}