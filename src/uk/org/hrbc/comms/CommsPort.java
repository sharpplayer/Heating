package uk.org.hrbc.comms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.ActualRequiredTemperatureCommand;
import uk.org.hrbc.commands.FlowTemperatureCommand;
import uk.org.hrbc.commands.BoilerStateCommand;
import uk.org.hrbc.commands.OccupiedCommand;
import uk.org.hrbc.commands.OptimumStartCommand;
import uk.org.hrbc.commands.OptimumStopCommand;
import uk.org.hrbc.commands.RequiredTemperatureCommand;
import uk.org.hrbc.commands.SetPointNotOccupiedCommand;
import uk.org.hrbc.commands.TemperatureCommand;
import uk.org.hrbc.commands.ValveCommand;
import uk.org.hrbc.comms.exceptions.CommsResendException;
import uk.org.hrbc.comms.exceptions.CommsTimeoutException;
import uk.org.hrbc.comms.testdata.BoilerStatusTestData;
import uk.org.hrbc.comms.testdata.OccupancyTestData;
import uk.org.hrbc.comms.testdata.StatusTestData;
import uk.org.hrbc.comms.testdata.TemperatureTestData;
import uk.org.hrbc.comms.testdata.TestData;
import uk.org.hrbc.comms.testdata.TimeTestData;
import uk.org.hrbc.comms.testdata.ValveTestData;
import uk.org.hrbc.debug.DebugItem;
import uk.org.hrbc.debug.DebugItems;

public class CommsPort implements SerialPortEventListener {

	private final static int BUFFER_SIZE = 256;
	private final static byte END_BUFFER = (byte) 0x03;
	private final static byte ESCAPE_CODE = (byte) 0x1b;
	private final static byte IGNORE_READ = (byte) 0x1c;
	private final static int COMM_PAUSE = 100;
	private final static String ALARM_MESSAGE = "ALARM CONNECTION";

	private InputStream inputStream;
	private OutputStream outputStream = null;
	private SerialPort serialPort;
	private Socket ipSocket = null;
	private ConcurrentHashMap<String, String> inData = new ConcurrentHashMap<String, String>();
	private byte[] receiveBuffer = new byte[BUFFER_SIZE];
	private int bufferIndex = 0;
	private boolean exit = false;
	private char sendKey = '4';
	private boolean[] oddParity = new boolean[128];
	private boolean resend = false;
	private int port = 0;
	private String interf;
	// type index:send buffer index,0:1 1:2 2:7 3:8 4:6

	// private final static byte[] INIT = new byte[] { (byte) 0x8A, (byte) 0x01,
	// '0', '0', 0x05 };
	// private final static byte[] INIT2 = new byte[] { (byte) 0xFE, (byte)
	// 0x01,
	// (byte) 0xFE, (byte) 0xFE, 0x05 };
	private final static byte[] SEND = new byte[] { (byte) 0x94, (byte) 0x8A, '0', '0', 0x05 };
	private final static byte[] SEND_EX = new byte[] { (byte) 0x94, (byte) 0x8A, '0', '0', 0x04 };
	private final static byte[] SEND_ALM = new byte[] { (byte) 0x01, (byte) 0x94, '0', '0', 0x04 };
	private HashMap<String, TestData> testData = new HashMap<String, TestData>();
	private int timeout = 10;
	private DebugItems debug;
	private Thread thread = null;

	public void commonConstructor(String interf, int port, int to) {
		debug = new DebugItems(this);
		timeout = to / COMM_PAUSE;
		this.interf = interf;
		this.port = port;

		for (int loop = 0; loop < 128; loop++) {
			int data = loop;
			while (data > 0) {
				if ((data & 1) == 1)
					oddParity[loop] = !oddParity[loop];
				data >>= 1;
			}
		}
		if (interf.equalsIgnoreCase("test")) {
			testData.put("010", new OccupancyTestData("010", "    10  1320  1320  1320  1440  1440"));
			testData.put("020", new OccupancyTestData("020", "    10    10    10  1260  1440  1440"));
			testData.put("030", new OccupancyTestData("030", "    10    10    10    10  1440  1440"));
			testData.put("040", new OccupancyTestData("040", "    10   690   690   690  1440  1440"));
			testData.put("050", new OccupancyTestData("050", "    10    10   840   840  1440  1440"));
			testData.put("060", new OccupancyTestData("060", "    10    10   390   390  1440  1440"));
			testData.put("070", new OccupancyTestData("070", "    10  1290  1440  1440  1440  1440"));
			testData.put("011", new OccupancyTestData("011", "    10  1320  1320  1320  1440  1440"));
			testData.put("021", new OccupancyTestData("021", "    10    10    10  1260  1440  1440"));
			testData.put("031", new OccupancyTestData("031", "    10    10    10    10  1440  1440"));
			testData.put("041", new OccupancyTestData("041", "    10   690   690   690  1440  1440"));
			testData.put("051", new OccupancyTestData("051", "    10    10   840   840  1440  1440"));
			testData.put("061", new OccupancyTestData("061", "    10    10   390   390  1440  1440"));
			testData.put("071", new OccupancyTestData("071", "    10  1290  1440  1440  1440  1440"));
			testData.put("012", new OccupancyTestData("011", "    10  1320  1320  1320  1440  1440"));
			testData.put("022", new OccupancyTestData("021", "    10    10    10  1260  1440  1440"));
			testData.put("032", new OccupancyTestData("031", "    10    10    10    10  1440  1440"));
			testData.put("042", new OccupancyTestData("041", "    10   690   690   690  1440  1440"));
			testData.put("052", new OccupancyTestData("051", "    10    10   840   840  1440  1440"));
			testData.put("062", new OccupancyTestData("061", "    10    10   390   390  1440  1440"));
			testData.put("072", new OccupancyTestData("071", "    10  1290  1440  1440  1440  1440"));
			testData.put(ValveCommand.class.getCanonicalName(), new ValveTestData());
			testData.put(TemperatureCommand.class.getCanonicalName(), new TemperatureTestData());
			testData.put(FlowTemperatureCommand.class.getCanonicalName(), new TemperatureTestData());
			testData.put(RequiredTemperatureCommand.class.getCanonicalName(), new TemperatureTestData());
			testData.put(ActualRequiredTemperatureCommand.class.getCanonicalName(), new TemperatureTestData());
			testData.put(OccupiedCommand.class.getCanonicalName(), new StatusTestData());
			testData.put(OptimumStartCommand.class.getCanonicalName(), new StatusTestData());
			testData.put(OptimumStopCommand.class.getCanonicalName(), new StatusTestData());
			testData.put(BoilerStateCommand.class.getCanonicalName(), new BoilerStatusTestData());
			testData.put(SetPointNotOccupiedCommand.class.getCanonicalName(), new TemperatureTestData());
			TimeTestData td = new TimeTestData();
			testData.put("uAT", td);
			testData.put("u00", td);

			thread = new Thread() {
				@Override
				public void run() {
					byte[] data = new byte[512];
					int len;
					while (!exit) {
						if (Math.random() * 1000 > 995) {
							int duffData = (int) (Math.random() * 100 + 50);
							for (len = 0; len < duffData; len++) {
								do {
									data[len] = (byte) (Math.random() * 128);
								} while ((data[len] == END_BUFFER) || (data[len] == 0x0D));
							}
							String alarmHeader = "uX1ALM000";
							System.arraycopy(alarmHeader.getBytes(), 0, data, len, alarmHeader.length());
							len += alarmHeader.length();
							System.arraycopy(ALARM_MESSAGE.getBytes(), 0, data, len, ALARM_MESSAGE.length());
							len += ALARM_MESSAGE.length();
							data[len] = END_BUFFER;
							len++;
							byte[] send = new byte[len];
							System.arraycopy(data, 0, send, 0, len);
							for (int loop = 0; loop < len; loop++) {
								send[loop] = toByte(send[loop]);
								processNewData(send[loop]);
							}
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
				}
			};
			thread.start();
		} else if (port == 0) {
			try {
				CommPortIdentifier portId = null;
				CommPortIdentifier portTest = null;
				Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
				while (portList.hasMoreElements()) {
					portTest = (CommPortIdentifier) portList.nextElement();
					if (portTest.getPortType() == CommPortIdentifier.PORT_SERIAL) {
						if (portTest.getName().equals(interf)) {
							portId = portTest;
							break;
						}
					}
				}
				if (portId != null) {
					serialPort = (SerialPort) portId.open("Heating", 2000);
					inputStream = serialPort.getInputStream();
					outputStream = serialPort.getOutputStream();
					serialPort.addEventListener(this);
					serialPort.notifyOnDataAvailable(true);
					serialPort.notifyOnBreakInterrupt(true);
					serialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					debug.add(new DebugItem(DebugItem.INFO, "Connecting via serial port " + interf));
				}
				init();
			} catch (PortInUseException e) {
				debug.add(new DebugItem(DebugItem.WARNING, e.getMessage()));
			} catch (IOException e) {
				debug.add(new DebugItem(DebugItem.WARNING, e.getMessage()));
			} catch (TooManyListenersException e) {
				debug.add(new DebugItem(DebugItem.WARNING, e.getMessage()));
			} catch (UnsupportedCommOperationException e) {
				debug.add(new DebugItem(DebugItem.WARNING, e.getMessage()));
			}

		} else {
			try {
				ipSocket = new Socket(interf, port);
				inputStream = new DataInputStream(ipSocket.getInputStream());
				outputStream = new DataOutputStream(ipSocket.getOutputStream());
				// Ethernet setup

				thread = new Thread() {
					@Override
					public void run() {
						byte[] data = new byte[512];
						int len;
						while (!exit) {
							try {
								while ((len = inputStream.read(data)) != -1) {
									for (int loop = 0; loop < len; loop++)
										processNewData(data[loop]);
								}
							} catch (IOException e) {
								System.out.println("Exiting thread exception");
								exit = true;
								resend = true;
							}
						}
					}
				};
				thread.start();

				debug.add(new DebugItem(DebugItem.INFO, "Connecting via ethernet port " + port + " to " + interf));

				init();
			} catch (IOException e) {
				debug.add(new DebugItem(DebugItem.WARNING, e.getMessage()));
			}
		}
	}

	public CommsPort(HeatingSystem system) {

		String port = system.getParam(HeatingSystem.PARAM_PORT);
		if (port.equalsIgnoreCase("test")) {
			commonConstructor("test", 0, 1000);
			debug.add(new DebugItem(DebugItem.INFO, "Using test comm port data"));
		} else {
			int to = 1000;
			try {
				to = Integer.parseInt(system.getParam(HeatingSystem.PARAM_COMM_TIMEOUT));
			} catch (NumberFormatException ex) {
			}

			try {
				int p = Integer.parseInt(port);
				commonConstructor(system.getParam(HeatingSystem.PARAM_IP), p, to);
			} catch (NumberFormatException ex) {
				commonConstructor(port, 0, to);
			}
		}
	}

	private void init() throws IOException {
		// sendMessage(INIT, "wMV", "", false);
		// sendMessage(INIT2, "LAN", "AS", false);
	}

	@Override
	public synchronized void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;

		case SerialPortEvent.DATA_AVAILABLE:
			int newData = 0;
			while (newData != -1) {
				try {
					newData = inputStream.read();
					if (newData != -1)
						processNewData((byte) newData);
				} catch (IOException e) {
					debug.add(new DebugItem(DebugItem.WARNING, e.getMessage()));
				}
			}
			break;
		}
	}

	public void processNewData(byte newData) {
		byte trueData = (byte) (newData & 0x7f);
		String ddata;
		if (trueData < 32)
			ddata = "[" + trueData + "]";
		else
			ddata = Character.toString((char) trueData);
		debug.add(new DebugItem(DebugItem.INFO, "Received:" + ddata));
		receiveBuffer[bufferIndex] = trueData;
		bufferIndex++;
		if (trueData == END_BUFFER) {
			if ((bufferIndex > 1) && (receiveBuffer[bufferIndex - 2] == ESCAPE_CODE)) {
				// Escaped end buffer so do nothing
			} else if (bufferIndex == 4) {
				// Ack buffer
				bufferIndex = 0;
			} else {
				if (receiveBuffer[0] == IGNORE_READ) {
					bufferIndex = 0;
				} else {
					String key = Character.toString((char) receiveBuffer[3])
							+ Character.toString((char) receiveBuffer[4]) + Character.toString((char) receiveBuffer[5]);
					String data = "";
					for (int loop = 9; (receiveBuffer[loop] != END_BUFFER) && (receiveBuffer[loop] != 0x0D); loop++)
						data += (char) (receiveBuffer[loop]);
					// For some reason alarm connection receives junk sometimes
					// at the
					// start
					int ind = data.indexOf(ALARM_MESSAGE);
					if (ind != -1) {
						ind += 8;
						key = Character.toString((char) receiveBuffer[ind - 5])
								+ Character.toString((char) receiveBuffer[ind - 4])
								+ Character.toString((char) receiveBuffer[ind - 3]);
						try {
							debug.add(new DebugItem(DebugItem.INFO, "Received ALARM CONNECTION"));
							sendMessage(SEND_ALM, key, "", false);
							resend = true;
						} catch (IOException ex) {
							debug.add(
									new DebugItem(DebugItem.INFO, "Failed to Ack ALARM CONNECTION:" + ex.getMessage()));
						}
					} else {
						inData.put(key, data);
					}
					bufferIndex = 0;
					receiveBuffer = new byte[BUFFER_SIZE];
				}
			}
		}
	}

	public String sendMessage(String message, boolean lf, String clazz) throws IOException {
		String key;
		if (outputStream == null)
			key = clazz;
		else
			key = "uX" + sendKey;
		sendKey++;
		if (sendKey == 'P')
			sendKey = '4';
		return sendMessage(SEND, key, message, lf);
	}

	public String sendMessage(String key, String message, boolean lf) throws IOException {
		return sendMessage(SEND, key, message, lf);
	}

	public String sendMessageEx(String key, String message, boolean lf) throws IOException {
		return sendMessage(SEND_EX, key, message, lf);
	}

	private String sendMessage(byte[] type, String key, String message, boolean lf) throws IOException {

		if (thread != null) {
			if (!thread.isAlive()) {
				closeConnection();
				commonConstructor(interf, port, timeout * COMM_PAUSE);
				exit = false;
				thread.start();
			}
		}

		debug.add(new DebugItem(DebugItem.INFO, "Sending message " + message + " with key " + key));
		if (outputStream == null) {
			if (testData.containsKey(key)) {
				TestData td = testData.get(key);
				if (td != null)
					td.setData(message);
			}
		} else {
			int size = message.length();
			if (lf)
				size++;
			size += 10;
			byte[] data = new byte[size];
			data[0] = 2;
			data[1] = toByte(type[0]);
			data[2] = toByte(type[1]);
			data[3] = toByte((byte) key.charAt(0));
			data[4] = toByte((byte) key.charAt(1));
			data[5] = toByte((byte) key.charAt(2));
			data[6] = toByte(type[4]);
			data[7] = toByte(type[2]);
			data[8] = toByte(type[3]);
			for (int loop = 0; loop < message.length(); loop++)
				data[9 + loop] = toByte((byte) message.charAt(loop));
			if (lf)
				data[size - 2] = toByte((byte) 0x0D);
			data[size - 1] = toByte(END_BUFFER);
			if (outputStream != null) {
				String msg = "";
				for (byte b : data) {
					if ((b & 0x7F) < 32)
						msg += "[" + Byte.toString(b) + "]";
					else
						msg += (char) (b & 0x7F);
				}
				debug.add(new DebugItem(DebugItem.INFO, "Sending message " + msg));
				outputStream.write(data);
				outputStream.flush();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}

		return key;
	}

	public String receiveMessage(String key) throws CommsTimeoutException, CommsResendException {

		if (outputStream == null) {
			if (testData.containsKey(key))
				return testData.get(key).getData();
			else
				return "No test data";
		} else {
			int wait = 0;
			while (!inData.containsKey(key)) {
				wait++;
				if (resend) {
					resend = false;
					throw new CommsResendException();
				}
				if (wait >= timeout)
					break;
				try {
					Thread.sleep(COMM_PAUSE);
				} catch (InterruptedException e) {
				}
				debug.add(new DebugItem(DebugItem.INFO, "Waited:" + (wait * COMM_PAUSE)));
			}
		}

		if (inData.containsKey(key)) {
			String ret = inData.get(key);
			inData.remove(key);
			return ret;
		} else
			throw new CommsTimeoutException();
	}

	public void closeConnection() {

		if (serialPort != null) {
			try {
				if (outputStream != null)
					outputStream.close();
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
				debug.add(new DebugItem(DebugItem.WARNING, e.getMessage()));
			}
			serialPort.close();
		}

		if (ipSocket != null) {
			try {
				if (thread != null)
					if (thread.isAlive())
						thread.interrupt();
				if (outputStream != null)
					outputStream.close();
				exit = true;
				if (inputStream != null)
					inputStream.close();
				ipSocket.close();
			} catch (IOException e) {
				debug.add(new DebugItem(DebugItem.WARNING, e.getMessage()));
			}
		}
	}

	private byte toByte(byte ch) {
		if ((ch & 0x80) != 0)
			return ch;
		else if (oddParity[ch])
			return (byte) ch;
		else
			return (byte) (ch | 0x80);
	}

	public DebugItems getDebug() {
		DebugItems dis = debug;
		debug = new DebugItems(this);
		return dis;
	}
}
