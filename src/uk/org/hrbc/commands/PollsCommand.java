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

public class PollsCommand extends CommandImpl {

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {
		String sXml = "<polls>";
		String sSql = "SELECT PollId, Description, Active FROM tblpoll LEFT JOIN tblcommandgroup ON tblpoll.GroupId = tblcommandgroup.GroupId WHERE Source="
				+ HeatingSystem.SOURCE_POLL;
		ResultSet rs;
		try {
			rs = system.executeQuery(sSql);
			while (rs.next()) {
				sXml += "<poll><id>" + rs.getInt(1) + "</id>";
				sXml += "<description>" + rs.getString(2) + "</description>";
				sXml += "<active>" + rs.getInt(3) + "</active>";
				sXml += "</poll>";
			}
			sXml += "</polls>";
			return new SuccessResponse(sXml, getArgumentsXML());
		} catch (SQLException e) {
			return new SqlErrorResponse(sSql, e.getMessage(), getArgumentsXML());
		}
	}

	@Override
	public String getDescription(String mode) {
		return "List polls";
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
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		return null;
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_ADMIN;
	}
}
