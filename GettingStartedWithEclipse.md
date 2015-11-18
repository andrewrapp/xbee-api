This entry is intended to help you get started using Eclipse with XBee API.  This entry assumes you have at least one XBee connected to your computer and your computer recognizes the device as a serial port.  If this is not the case you probably need to download the FTDI USB Serial drivers (See GettingStarted).

First you'll need to download the [Eclipse IDE](http://www.eclipse.org/downloads/).  There are several versions to choose from and any of the Java version will work fine.  I recommend "Eclipse IDE for Java Developers".  Unzip the file and run the executable (e.g. Eclipse.exe on Windows).

Download the latest version of the XBee API, if you haven't done so already and unzip the file to where ever you keep your projects.

Now it's time to create an Eclipse project.  From the Eclipse file menu, select File->Import, then, General->Existing Projects into Workspace.  Click "Next", choose the "Select root directory" radio button and browse to the xbee-api folder (I leave the "copy projects into workspace" checkbox unchecked).  Click "Finish".

At this point you should have an XBee attached to your serial port and configured in API mode.  See XBeeConfiguration for more details.

In Eclipse, in the left most window pane, expand the project you created and navigate to the "src" folder.  Then expand the "com.rapplogic.xbee.examples" package and open the ApiAtTest Java class file (this example works for both series 1 and 2 -- in API mode).  Edit the line that contains the "xbee.open(..)" method and specify your COM port.  Click save.

To run the example, right-click the file in the left window pane and select "Run As->Java Application".  You should see the output of your code in the Console window.  You can also view the the program output in xbee.log, in the project folder.

If you see an error message such as:

Caused by: java.lang.RuntimeException: Could not find port: COM6
> at com.rapplogic.xbee.RxTxSerialComm.openSerialPort(RxTxSerialComm.java:73)
> at com.rapplogic.xbee.RxTxSerialComm.openSerialPort(RxTxSerialComm.java:40)
> at com.rapplogic.xbee.api.XBee.open(XBee.java:105)

your serial port was not found.  Verify it is plugged in and powered on and your computer can "see" it.

Now run some of the other examples.  The examples are organized by radio type, so "com.rapplogic.xbee.examples.wpan" are for Series 1 and "com.rapplogic.xbee.examples.zigbee" are for Series 2 (ZNet).

You can start creating your own classes too.  From the File menu select File->New->Class
Enter a name, for example: "XBeeTest", and specify a package, like "com.foo.xbee"  Now you can cut and paste pieces from the examples to get going.

For examples that are designed to run "infinitely" (e.g. while (true) collect data), remember to terminate the process before running another example.  If you get an error message stating that RXTX is in use, there is probably another Java example running.  If this occurs, shutdown Eclipse and start it again.