package uk.org.hrbc.commands.responses;

import java.text.SimpleDateFormat;

import uk.org.hrbc.debug.DebugItems;

public class CommandResponse {

	public final static int SUCCESS = 0;
	public final static int ERROR_IN_CLASS = 1;
	public final static int ERROR_NULL_RESPONSE = 2;
	public final static int ERROR_BAD_ADDRESS = 3;
	public final static int ERROR_BAD_SQL = 4;
	public final static int LONG_RESPONSE = 5;
	public final static int ERROR_BAD_ARGUMENT = 6;
	public final static int ERROR_ARGUMENT_PARSE = 8;
	public final static int ERROR_IN_MESSAGING = 9;
	public final static int ERROR_COMMS_WRITE = 10;
	public final static int ERROR_MISSING_ARGUMENT = 11;
	public final static int ERROR_COMMS_TIMEOUT = 12;
	public final static int ERROR_BAD_DATE = 13;
	public final static int ERROR_SYSTEM_COMMAND = 14;
	public final static int ERROR_GENERAL_EXCEPTION = 15;
	public final static int ERROR_COMMS_RETRY_EXCEED = 16;
	public final static int ERROR_XML = 17;
	public final static int ERROR_URL = 18;
	public final static int ERROR_ICS = 19;

	public final static SimpleDateFormat XML_DATE = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private String message = "";
	private int status = SUCCESS;
	private boolean data = false;
	private DebugItems debug = null;
	private String args = "";
	private boolean autoPollStop = false;

	protected CommandResponse(String xmlMessage, int code, boolean isData,
			DebugItems dis, String arguments) {
		message = xmlMessage;
		status = code;
		data = isData;
		debug = dis;
		args = arguments;
	}

	protected CommandResponse(String xmlMessage, int code, boolean isData,
			String arguments) {
		this(xmlMessage, code, isData, null, arguments);
	}

	protected CommandResponse(String xmlMessage, int code, DebugItems dis,
			String arguments) {
		this(xmlMessage, code, false, dis, arguments);
	}

	protected CommandResponse(String xmlMessage, int code, String arguments) {
		this(xmlMessage, code, false, null, arguments);
	}

	public String getMessage() {
		return message;
	}

	public int getStatus() {
		return status;
	}

	public boolean isData() {
		return data;
	}

	public String getArgs() {
		return args;
	}

	public DebugItems getDebug() {
		return debug;
	}

	public boolean isAutoPollStop() {
		return autoPollStop;
	}

	public void setAutoPollStop(boolean aps) {
		autoPollStop = aps;
	}

}
