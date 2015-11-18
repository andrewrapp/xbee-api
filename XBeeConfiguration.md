# XBee Configuration #

This guide describes how to configure two XBee radios (Coordinator and End Device) to form a network.  Digi provides the [X-CTU](http://www.digi.com/support/productdetl.jsp?pid=3352&osvid=57&tp=5&s=316) application for firmware upgrades and XBee configuration.  It's also possible to programmatically configure XBees with this software (all platforms).  See [ApiAtExample.java](http://code.google.com/p/xbee-api/source/browse/trunk/src/com/rapplogic/xbee/examples/ApiAtExample.java) for an example.

When using X-CTU, remember that configuration values are in Hexadecimal (base 16), so for example, IR=9000, sets the sample rate to every 36864 milliseconds (in base 10) or every 36.8 seconds.  There are plenty of sites online to help converting between base 10 and 16.

<a href='Hidden comment: 
Note: The AtCommand class requires that the radio be in API mode (AP=2).  Also, remember that parameters are in hex format, without the 0x prefix, so if you specify "12", you need to use 0x12 in Java.  For this reason you may want to use hex digits (A-F), where possible  to eliminate any confusion.

For series 1 radios that are not in API mode yet, there is another option.. if your series 1 XBee is in transparent (AT) mode (AP=0), you can use the XBeeSerialProgrammer to put in into API mode.
Remember to check "api mode" and escape chars when using X-CTU.
'></a>

## 802.15.4/Series 1 ##

Prerequisites:

  * Two 802.15.4 XBee radios with 10A5 or later firmware

### Coordinator ###

```
Restore to Factory Settings *
RE

Put XBee in API mode (escape control bytes)
AP=2 

Make this radio the Coordinator
CE=1

Set the address of this radio to any arbitrary two byte value
MY=1234

Set the PAN ID to a two byte arbitrary value.  Each XBee in the network must have this same value.  
ID=1111

Both radios must have the same Channel and PAN ID to communicate
CH=0C

Save to non-volitile memory to survive power on/off
WR

Reboot Radio
FR
```
### End Device ###

The End Device configuration is identical except for:

```
Make this an End Device
CE=0

Set the 16-bit address to a unique value
MY=5678

```

`*` Running the Restore command is generally a good idea to reset existing configurations that might otherwise cause problems.  Keep in mind that if you are currently in API mode and you perform a restore with X-CTU, the radio will switch to transparent mode (AP=0).  If this occurs you will need to go to the "PC Settings" tab and uncheck "Enable API" in order to make the configuration changes.  After making the configuration changes, both the "Enable API" and "Use escape characters" checkboxes should be checked.

## ZNet/ZB Pro/Series 2 ##

Unlike Series 1 radios, Series 2 radios require a firmware upload to support API mode, as they ship with AT firmware.  Moreover, firmware is specific to the mode (Coordinator, Router, or End Device). The X-CTU application is required to upload firmware.

Prerequisites:

  * One ZNet or ZB Pro XBee with Coordinator API firmware
  * One ZNet or ZB Pro XBee with End Device (or Router) API firmware

### Coordinator ###

```
First restore to factory settings to eliminate the chance of lingering settings *
RE

Set PAN ID to an arbitrary value.  The end device must also use this exact value
ID=1AAA

Both radios must have the same Channel and PAN ID to communicate
CH=13

Set the node identifier to an arbitrary string.  This is optional and only serves as a convenient way to identify your devices
NI=COORDINATOR

Set API mode to 2 (escape control bytes)
AP=2

Save to settings to survive power cycle
WR

Reboot the radio.  apply changes "AC" should also suffice
FR
```

### Router (recommended)/End Device ###

The configuration of the Router/End Device is the same, except choose a different value for the Node Identifier, ex:
```
NI=END_DEVICE_1
```

`*` If you are using X-CTU, and currently in API mode (AP=2), a Restore will change the API mode to AP=1 (no escape bytes).   If you click the "Read" button after a restore and get an error, it is likely due to the the API mode.  If this occurs, go to the PC Settings" tab and uncheck "Use escape characters" checkbox, then make the configuration changes.  After after changing AP=2, you will need to check the "Use escape characters" checkbox.

Note: End Devices are intended to sleep periodically to conserve power (default sleep mode is Cyclic Sleep). If you want your radio always powered on choose Router firmware. If you do install End Device firmware and want to prevent the radio from sleeping (for example to to read/write configuration, since it will likely go to sleep during a configuration change), set the sleep mode to "Pin Hibernate" and ground pin 9. See http://rapplogic.blogspot.com/2009/02/xbee-zb-pro-upgrade-pin-sleep-trick.html for more information.

# Only for End Device firmware to prevent sleeping. Then ground pin 9
```
SM=1
```