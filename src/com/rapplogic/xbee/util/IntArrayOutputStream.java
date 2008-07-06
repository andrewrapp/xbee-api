package com.rapplogic.xbee.util;

import java.util.ArrayList;
import java.util.List;

// TODO replace with nio.IntBuffer
public class IntArrayOutputStream {

	private List<Integer> intList = new ArrayList<Integer>();
	
	public IntArrayOutputStream() {

	}
	
	public void write (int val) {
		intList.add(val);
	}
	
	public void write(int[] val) {
		for (int i = 0; i < val.length; i++) {
			this.write(val[i]);
		}
	}
	
	public int[] getIntArray() {
		//int[] integer = (int[]) intList.toArray(new int[0]);
		// TODO there has got to be a better way -- how to convert list to int[] array?
		int[] intArr = new int[intList.size()];
		
		int i = 0;
		
		for (Integer integer : intList) {
			intArr[i++] = integer.intValue();
		}
		
		return intArr;
	}
}
