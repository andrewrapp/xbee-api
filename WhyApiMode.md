# Why API mode? #

XBees support two modes of operation: API and AT.  In API mode, you communicate with the radio by sending and receiving packets.  In AT (transparent) mode, the XBee radio simply relays serial data to the receiving XBee, as identified by the DH+DL address.

This software is designed solely for API mode.  API mode is enabled by setting the AP parameter (Series 1), or uploading API firmware (Series 2).  This software requires the AP mode set to 2 (escape bytes), as this setting offers the best reliability.

Here's a brief overview of the main advantages of API Mode vs. AT (Transparent Mode):

API (Packet) Mode

  * I/O Samples.  This feature allows an XBee to receive I/O data from 1 or more remote XBees
  * Acknowledgement (ACK) and Retries.  When sending a packet, the transmitting radio receives an ACK, indicating the packet was successfully delivered.  The transmitting radio will resend the packet if it does not receive an ACK.
  * Receive packets (RX), contain the source address of transmitting radio
  * Configure a remote radio with the Remote AT feature
  * Easily address multiple radios and send broadcast TX packets
  * Obtain RSSI (signal strength) of an RX packet
  * Packets include a checksum for data integrity
  * ZigBee endpoints, cluster IDs and profile IDs (Series 2 XBee)

AT (Transparent) Mode

  * Simple
  * Compatible with any device that speaks serial
  * Primarily for point to point communication between two XBees.  It's possible to communicate with multiple XBees but this requires entering command mode each time to change the destination address.

Series 1 radios support both AT and API modes with a single firmware version, allowing you switch between the modes with X-CTU. However, Series 2 requires a specific firmware for API mode.  As of now there are two firmware versions for Series 2 API mode: ZNet and ZB Pro.  ZNet is recommended as it is the easiest to work with.