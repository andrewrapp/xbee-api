package com.rapplogic.xbee.transparent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.rapplogic.xbee.api.XBeeException;

/**
 * @author andrew
 * 
 * TODO need to support API mode for radios ZNet radios that do not support transparent when flashed with API firmware
 * TODO add serial number support so radio will only be programmed if serial number in file matches the radio's serial 
 * 
 */
public class XBeeFileProgrammer extends SerialAsciiComm {
	
	private File profile;
	private String port;
	
	public XBeeFileProgrammer(String port, File profile) {
		this.profile = profile;
		this.port = port;
	}
	
	public void execute() throws XBeeException, IOException, InterruptedException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(profile)));
		
		XBeeSerialProgrammer prog = new XBeeSerialProgrammer();
		
		boolean atleastone = false;
		String line = null;
		
		while ((line = reader.readLine()) != null) {
			
			line = line.trim();
			
			if (line.startsWith("#")) {
				// this is a comment
				continue;
			} else if (line.length() == 0) {
				// blank line
				continue;
			}
			
			prog.addCommand(line.trim());
			atleastone = true;
		}
		
		if (!atleastone) {
			throw new RuntimeException("File: " + profile.getAbsolutePath() + ", does not contain any commands");
		}
		
		try {
			prog.open(port, "XBeeApi", 0, 9600);
			prog.execute();			
		} finally {
			prog.close();			
		}
	}
	
	protected Object getLock() {
		return this;
	}
}