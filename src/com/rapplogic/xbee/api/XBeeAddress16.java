package com.rapplogic.xbee.api;

import com.rapplogic.xbee.util.DoubleByte;


/**
 * Represents a double byte XBeeApi Address.
 * 
 * @author andrew
 *
 */
public class XBeeAddress16 extends XBeeAddress {

	public static final XBeeAddress16 BROADCAST = new XBeeAddress16(0xFF, 0xFF);
	public static final XBeeAddress16 ZNET_BROADCAST = new XBeeAddress16(0xFF, 0xFE);

	private DoubleByte doubleByte = new DoubleByte();
	
	/**
	 * Provide address as msb byte and lsb byte
	 * 
	 * @param msb
	 * @param lsb
	 */
	public XBeeAddress16(int msb, int lsb) {
		this.doubleByte.setMsb(msb);
		this.doubleByte.setLsb(lsb);
	}

	public XBeeAddress16(int[] arr) {
		this.doubleByte.setMsb(arr[0]);
		this.doubleByte.setLsb(arr[1]);
	}
	
	public XBeeAddress16() {
		
	}
	
	public int get16BitValue() {
		return this.doubleByte.get16BitValue();
	}
	
	public int getMsb() {
		return this.doubleByte.getMsb();
	}

	public void setMsb(int msb) {
		this.doubleByte.setMsb(msb);
	}

	public int getLsb() {
		return this.doubleByte.getLsb();
	}

	public void setLsb(int lsb) {
		this.doubleByte.setLsb(lsb);
	}

	public boolean equals(Object o) {
		
		if (this == o) {
			return true;
		} else {
			try {
				XBeeAddress16 addr = (XBeeAddress16) o;
				
				if (this.getLsb() == addr.getLsb() && this.getMsb() == addr.getMsb()) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				return false;
			}			
		}
	}

	@Override
	public int[] getAddress() {
		return new int[] { this.doubleByte.getMsb(), this.doubleByte.getLsb() };
	}
}
