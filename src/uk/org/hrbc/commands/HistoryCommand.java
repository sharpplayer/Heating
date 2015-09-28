package uk.org.hrbc.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class HistoryCommand extends CommandImpl {

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {

		String no = getArgument("count");
		int count;
		try {
			count = Integer.parseInt(no);
		} catch (NumberFormatException ex) {
			count = 5;
		}

		int pending = 0;
		String sql = "SELECT CommandId FROM tblcommand WHERE Class='"
				+ this.getClass().getCanonicalName() + "'";
		try {
			ResultSet rs = system.executeQuery(sql);

			sql = "SELECT InXML, OutXML, ExecTimestamp, Source, Success, Mode, PendingId FROM tblcompleted";
			if (rs.next())
				sql += " WHERE CommandId <>" + rs.getInt(1);
			sql += " ORDER BY PendingId DESC";
			ResultSet rs2 = system.executeQuery(sql);
			{
				boolean inLongResponse = false;
				StringBuffer xml = new StringBuffer("<histories>");
				xml.append(system.getSourcesXML());
				while (rs2.next() && (count > 0)) {
					if (!inLongResponse) {
						xml.append("<history>");
						xml.append(rs2.getString(1));
						xml.append("<source>" + rs2.getInt(4) + "</source>");
						xml.append("<mode>" + rs2.getString(6) + "</mode>");
						xml.append(system.getTimeXML(rs2.getTimestamp(3),
								"timestamp"));
						xml.append("<response>");
						String out = rs2.getString(2);
						int endTag = out.indexOf("/>");
						int endTag2 = out.indexOf(">");

						if (endTag != -1 && endTag2 < endTag) {
							out = out
									.replaceFirst("/>", " mode=\"default\" />");
						} else {
							out = out.replaceFirst(">", " mode=\"default\">");
						}
						xml.append(out);
					} else {
						xml.append(rs2.getString(2));
					}

					if (rs2.getInt(5) != CommandResponse.LONG_RESPONSE) {
						if (rs2.getInt(7) != pending) {
							count--;
							pending = rs2.getInt(7);
						}
						inLongResponse = false;
						xml.append("</response></history>");
					} else
						inLongResponse = true;
				}
				xml.append("</histories>");
				return new SuccessResponse(xml.toString(), getArgumentsXML());
			}
		} catch (SQLException ex) {
			return new SqlErrorResponse(sql, ex.getMessage(), getArgumentsXML());
		}
	}

	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		return null;
	}

	@Override
	public String getDescription(String mode) {
		return "List history";
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

	@Override
	public String getDefaultArgXML(HeatingSystem system) {
		return "<arg id=\"count\">5</arg>";
	}
}
