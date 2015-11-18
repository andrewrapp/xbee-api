The following API types are supported:

XBee (802.15.4 and ZNet/ZB Pro):

  * AT Command (0x08)
  * AT Command Response (0x88)
  * AT Command - Queue Parameter Value (0x09)
  * Modem Status (0x8A)

XBee series 1 (802.15.4):

  * 16/64-bit RX I/O Sample (0x83, 0x82)
  * TX (Transmit) Request: 64-bit address (0x00)
  * TX (Transmit) Request: 16-bit address (0x01)
  * TX (Transmit) Status (0x89)
  * RX (Receive) Packet: 64-bit Address (0x80)
  * RX (Receive) Packet: 16-bit Address (0x81)

XBee series 2 (ZNet/ZB Pro):
  * ZigBee Transmit Request (0x10)
  * ZigBee Transmit Status (0x8B)
  * ZigBee Receive Packet (AO=0) (0x90)
  * ZigBee IO Data Sample Rx Indicator (0x92)
  * Remote Command Request (0x17)
  * Remote Command Response (0x97)
  * Explicit Addressing ZigBee Command Frame (0x11)
  * ZigBee Explicit Rx Indicator (0x91)
  * Node Identification Indicator (AO=0) (0x95)

Not supported at this time: XBee Sensor Read Indicator (AO=0)