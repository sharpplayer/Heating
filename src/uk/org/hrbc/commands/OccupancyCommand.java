package uk.org.hrbc.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.RetryFailedResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;
import uk.org.hrbc.commands.responses.TimeoutResponse;
import uk.org.hrbc.commands.responses.WriteErrorResponse;
import uk.org.hrbc.comms.exceptions.CommsResendException;
import uk.org.hrbc.comms.exceptions.CommsTimeoutException;

public class OccupancyCommand extends RecyclableCommand {

	private String spaces = "      ";

	public OccupancyCommand() {
		setTag("occupancy");
	}

	@Override
	public CommandResponse execute(HeatingSystem system) {
		String occsin = getArgument("occin10");
		String occsout;
		String key;
		String resp;
		String xml = "";
		String tag;
		int day;
		int index;
		int zoneId;
		String zone = getArgument("zone");
		if (zone == null) {
			zone = system.getParam(HeatingSystem.PARAM_DEFAULTZONE);
		}
		zoneId = Integer.parseInt(zone.substring(1));

		int retry = 3;
		try {
			retry = Integer.parseInt(system
					.getParam(HeatingSystem.PARAM_RETRIES));
		} catch (NumberFormatException ex) {
		}

		if (occsin != null) {
			while (retry != 0) {
				try {
					system.sendMessage("vZ$", zone.toUpperCase() + ",1($),O"
							+ zoneId + "(W,D,N,F)", true);
					for (day = 1; day <= 7; day++) {
						resp = "";
						for (index = 0; index < 3; index++) {
							occsin = getArgument("occin" + day + index);
							occsout = getArgument("occout" + day + index);
							resp += rightJust(occsin);
							resp += rightJust(occsout);
						}
						key = "0" + day;
						key += (zoneId - 1);
						system.sendMessageEx(key, resp, false);
						system.receiveMessage(key);
					}
					break;
				} catch (IOException e) {
					return new WriteErrorResponse(e.getMessage(),
							getArgumentsXML());
				} catch (CommsTimeoutException e) {
					return new TimeoutResponse(getArgumentsXML());
				} catch (CommsResendException e) {
					retry--;
				}
			}
		}

		if (retry == 0)
			return new RetryFailedResponse(getArgumentsXML());

		xml = "<" + getTag() + ">";
		while (retry != 0) {
			try {
				xml += "<zone>" + zone + "</zone>";
				system.sendMessage("vZ$", zone.toUpperCase() + ",1($),O"
						+ zoneId + "(W,D,N,F)", true);
				for (day = 1; day <= 7; day++) {
					key = "0" + Integer.toString(day) + (zoneId - 1);
					system.sendMessage(key, "", false);
					resp = system.receiveMessage(key);
					xml += "<day>";
					xml += "<weekday>" + ((day % 7) + 1) + "</weekday>";
					xml += "<times>";
					for (index = 0; index < 6; index++) {
						if ((index % 2) == 0)
							tag = "in";
						else
							tag = "out";
						xml += "<" + tag;
						xml += " id=\"occ" + tag + day + (int) (index / 2)
								+ "\">";
						if (index == 5)
							xml += resp.substring(index * 6).trim();
						else
							xml += resp.substring(index * 6, index * 6 + 6)
									.trim();
						xml += "</" + tag + ">";
					}
					xml += "</times>";
					xml += "</day>";
				}
				break;
			} catch (IOException e) {
				return new WriteErrorResponse(e.getMessage(), getArgumentsXML());
			} catch (CommsTimeoutException e) {
				return new TimeoutResponse(getArgumentsXML());
			} catch (CommsResendException e) {
				retry--;
			}
		}

		if (retry == 0)
			return new RetryFailedResponse(getArgumentsXML());

		xml += "</" + getTag() + ">";
		return new SuccessResponse(xml, getArgumentsXML());
	}

	private String rightJust(String data) {
		return spaces.substring(0, spaces.length() - data.length()) + data;
	}

	@Override
	public String getDescription(String mode) {
		if (mode.equalsIgnoreCase(HeatingSystem.MODE_EDIT))
			return "Set occupancy times";
		else
			return "Get occupancy times";
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT,
				HeatingSystem.MODE_EDIT));
	}

	@Override
	public boolean isPollable() {
		return false;
	}

	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		Hashtable<String, String> conds = new Hashtable<String, String>();
		conds.put("Occupancy viewed or modified", "/*/" + getTag() + "");
		return conds;
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_ADMIN;
	}

	@Override
	public String getDefaultArgXML(HeatingSystem system) {
		return "<arg id=\"zone\">"
				+ system.getParam(HeatingSystem.PARAM_DEFAULTZONE) + "</arg>";
	}

	@Override
	public boolean isSetCommand() {
		return hasMultipleArgs();
	}

	@Override
	public String getImmediateResponse() {
		String xml = "<" + getTag() + ">";
		String zone = getArgument("zone");
		int day;
		int index;
		String occsin;
		String occsout;
		xml += "<zone>" + zone + "</zone>";
		for (day = 1; day <= 7; day++) {
			xml += "<day>";
			xml += "<weekday>" + ((day % 7) + 1) + "</weekday>";
			xml += "<times>";
			for (index = 0; index < 3; index++) {
				occsin = getArgument("occin" + day + index);
				occsout = getArgument("occout" + day + index);
				xml += "<in id=\"occin" + day + index + "\">" + occsin
						+ "</in>";
				xml += "<out id=\"occout" + day + index + "\">" + occsout
						+ "</out>";
			}
			xml += "</times>";
			xml += "</day>";
		}
		xml += "</" + getTag() + ">";
		return xml;
	}
}
