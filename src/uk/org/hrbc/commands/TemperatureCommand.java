package uk.org.hrbc.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.comms.exceptions.CommsResendException;
import uk.org.hrbc.comms.exceptions.CommsTimeoutException;

public class TemperatureCommand extends BasicAreaCommand {

	public final static String TAG_TEMPERATURE = "temperature";

	private final static String CMD_INSIDE_TEMP = "S1";
	private final static String CMD_OUTSIDE_TEMP = "S4";

	public TemperatureCommand() {
		super(TAG_TEMPERATURE);
	}

	@Override
	public String getDescription(String mode) {
		return "Get current temperature";
	}

	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		Hashtable<String, String> conds = super.getConditions(system);
		int[] temps = { 5, 10, 15, 18, 20, 21, 22, 23, 24, 25 };
		for (int temp : temps)
			conds.put("Temperature is less than " + temp + " degrees", "number(/*/temperature/value) &lt; " + temp);
		for (int temp : temps)
			conds.put("Temperature is greater than " + temp + " degrees", "number(/*/temperature/value) &gt; " + temp);
		return conds;
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_NORMAL;
	}

	@Override
	public CommandResponse execute(HeatingSystem system) {
		return super.executeGet(system, CMD_INSIDE_TEMP, CMD_OUTSIDE_TEMP, false);
	}

	@Override
	protected String getXMLResponse(HeatingSystem system, String zone, String obj, String command)
			throws IOException, CommsTimeoutException, CommsResendException {
		String ret = super.getXMLResponse(system, zone, obj, command);
		double temp = 0;
		// Dirty way to get value, but easier than XPath
		int ind = ret.indexOf("<value>");
		if (ind != -1) {
			int endInd = ret.indexOf("</value>");
			try {
				temp = Double.parseDouble(ret.substring(ind + 7, endInd));
				if (command.equals(CMD_OUTSIDE_TEMP)) {
					system.setParam(HeatingSystem.PARAM_OUTSIDE_TEMP, Double.toString(temp));
				} else {
					system.setParam(HeatingSystem.PARAM_INSIDE_TEMP + zone.toUpperCase(), Double.toString(temp));
				}
			} catch (Exception e) {
				// Do nothing if problems
			}
		}
		return ret;
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
	}
}
