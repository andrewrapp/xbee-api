package com.rapplogic.xbee.examples.wpan;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.transparent.XBeeFileProgrammer;

/**
 * This requires transparent mode and as such does not work for ZNet radios flashed in API mode
 * 
 * TODO file programmer that supports API mode
 * TODO allow serial number to be specified so that radio will only be programmed if serial matches e.g. SL=4008B48F, SH=13A200
 *  
 * @author andrew
 * 
 */
public class TestFileProgrammer {

		
	private TestFileProgrammer() throws Exception {

		File f = new File("xbee-base-thermistor.profile.txt");
		//File f = new File("xbee-remote-thermistor.profile.txt");
		//File f = new File("xbee-query.profile.txt");
		//File f = new File("xbee-base.profile.txt");
		//File f = new File("xbee-remote.profile.txt");
		
		
		XBeeFileProgrammer prog = new XBeeFileProgrammer("COM5", f);
		prog.execute();
	}

	public static void main(String[] args) throws Exception {
		// init log4j
		Properties props = new Properties();
		props.load(new FileInputStream("log4j.properties"));
		PropertyConfigurator.configure(props);
		
		new TestFileProgrammer();
	}
}