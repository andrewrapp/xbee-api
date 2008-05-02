package com.rapplogic.xbee.examples.wpan;


import java.io.FileInputStream;
import java.util.Properties;

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
		Properties props = new Properties();
		props.load(new FileInputStream("log4j.properties"));
		PropertyConfigurator.configure(props);		
		
		new TestSerialProgrammer(args);
	}
}