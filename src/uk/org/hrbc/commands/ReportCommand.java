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

public class ReportCommand extends CommandImpl {

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {

		String sql = "SELECT ExecTimestamp, OutXML FROM tblcompleted WHERE ";

		try {
			StringBuilder xml = new StringBuilder("<report>");
			xml.append("<interval>");
			xml.append(getInterval(system, "start", new Date()));
			xml.append(getInterval(system, "end",
					new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)));
			xml.append("</interval>");
			if (getArgument("startday") != null) {
				Date date = getDate(system, "start");
				sql += "Data=1";
				if (date != null)
					sql += " AND ExecTimestamp >= \""
							+ HeatingSystem.SQL_DATE.format(date) + "\"";
				date = getDate(system, "end");
				if (date != null)
					sql += " AND ExecTimestamp <= \""
							+ HeatingSystem.SQL_DATE.format(date) + "\"";
				String nopoll = getArgument("nopoll");
				if (nopoll == null)
					sql += " AND Source=" + HeatingSystem.SOURCE_POLL;

				ResultSet rs = system.executeQuery(sql);
				Date time = null;
				boolean newRec = false;
				boolean closeRec = false;

				while (rs.next()) {
					newRec = false;
					closeRec = true;
					if (time == null) {
						time = rs.getTimestamp(1);
						newRec = true;
					} else if (time.compareTo(rs.getTimestamp(1)) != 0) {
						xml.append("</data>");
						newRec = true;
						time = rs.getTimestamp(1);
					}
					if (newRec) {
						xml.append("<data>");
						xml.append(system.getTimeXML(time, "polltime"));
					}
					xml.append(getValidXML(rs.getString(2)));
				}
				if (closeRec) {
					xml.append("</data>");
				}
			}
			xml.append("</report>");
			return new SuccessResponse(xml.toString(), getArgumentsXML());
		} catch (ParseException e1) {
			return new DateErrorResponse(getArgumentsXML());
		} catch (SQLException e) {
			return new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
		}
	}

	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		return null;
	}

	@Override
	public String getDescription(String mode) {
		return "Displays a report";
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
	}

	@Override
	public boolean isPollable() {
		return false;
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_ADMIN;
	}

}
