package uk.org.hrbc.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.DateErrorResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class ZimbraAnalysisCommand extends CommandImpl {

	@Override
	public boolean isPollable() {
		return false;
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
	}

	@Override
	public String getDescription(String mode) {
		return "Zimbra analysis command";
	}

	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		return null;
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_ADMIN;
	}

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {

		String sql = "";
		String ret = "<zanalysis>";
		String zone = getArgument("zone");
		if (zone == null) {
			zone = system.getParam(HeatingSystem.PARAM_DEFAULTZONE);
		}
		try {
			if (getArgument("fromday") != null) {
				Date date = getDate(system, "from");

				sql = "SELECT InXML, OutXML, Success FROM tblcompleted LEFT JOIN tblcommand ON tblcompleted.CommandId=tblcommand.CommandId WHERE Class='"
						+ ZimbraCommand.class.getCanonicalName() + "' AND Data=0 AND ExecTimestamp >= '"
						+ HeatingSystem.SQL_DATE.format(date) + "'" + " ORDER BY ExecTimestamp";

				ResultSet rs = system.executeQuery(sql);
				String response = "";
				while (rs.next()) {
					if (!rs.getString(1).equals("<args/>")) {
						response += rs.getString(2);
						if (rs.getInt(3) == CommandResponse.SUCCESS) {
							int index = response.indexOf("<occupancy><zone>" + zone + "</zone>");
							if (index != -1) {
								response = response.substring(index);
								response = response.substring(0, response.indexOf("</occupancy>") + 12);
								ret += "<zimbra>" + response + "</zimbra>";
							}
							response = "";
						}
					}
				}

				sql = "SELECT OutXml, ExecTimestamp FROM tblcompleted LEFT JOIN tblcommand ON tblcompleted.CommandId=tblcommand.CommandId WHERE Class='"
						+ TemperatureCommand.class.getCanonicalName() + "' AND Data=1 AND ExecTimestamp >= '"
						+ HeatingSystem.SQL_DATE.format(date) + "' " + " ORDER BY ExecTimestamp";
				rs = system.executeQuery(sql);
				ret += "<oss>";
				while (rs.next()) {
					ret += "<os>";
					ret += "<t>";
					ret += rs.getString(2);
					ret += "</t>";
					ret += "<v>";
					ret += rs.getString(1);
					ret += "</v>";
					ret += "</os>";
				}
				ret += "</oss>";
			}
		} catch (SQLException e) {
			return new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
		} catch (ParseException e) {
			return new DateErrorResponse(getArgumentsXML());
		}
		ret += "</zanalysis>";

		return new SuccessResponse(ret, getArgumentsXML());
	}

}
