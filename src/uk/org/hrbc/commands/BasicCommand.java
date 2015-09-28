package uk.org.hrbc.commands;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map.Entry;

import uk.org.hrbc.HeatingSystem;

import uk.org.hrbc.comms.exceptions.CommsResendException;
import uk.org.hrbc.comms.exceptions.CommsTimeoutException;

public abstract class BasicCommand extends RecyclableCommand {

	final protected String get(HeatingSystem system, String command, String obj)
			throws IOException, CommsTimeoutException, CommsResendException {

		int retry = 3;
		try {
			retry = Integer.parseInt(system
					.getParam(HeatingSystem.PARAM_RETRIES));
		} catch (NumberFormatException ex) {
		}

		while (retry > 0) {
			try {
				String mess = "[" + obj + "]" + command + "(V)";
				String key = system.sendMessage(mess, true);
				String resp = system.receiveMessage(key);
				String val = getValidXML(resp.substring(5 + command.length()));
				return "<value>" + val + "</value>";
			} catch (CommsResendException e) {
				retry--;
			}
		}

		throw new CommsResendException();
	}

	final protected void set(HeatingSystem system, String command, String zone,
			String value) throws IOException, CommsTimeoutException {
		system.sendMessage("[" + zone + "]" + command + "(V=" + value + ")",
				true);
	}

	@Override
	public boolean isPollable() {
		return true;
	}

	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		Hashtable<String, String> conds = new Hashtable<String, String>();
		for (Entry<String, String> zone : system.getZonesMap().entrySet())
			conds.put("Zone is " + zone.getValue(),
					"/*/*/zone = '" + zone.getKey() + "'");
		return conds;
	}
}
