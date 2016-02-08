package uk.org.hrbc.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.BadArgumentResponse;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class AlertCommand extends CommandImpl {

	private final static String ARG_ALERT_ID = "alertid";
	private final static String ARG_DESCRIPTION = "description";
	private final static String ARG_GROUP = "group";
	private final static String ARG_SOURCE = "source";
	private final static String ARG_CONDITION = "condition";
	private final static String ARG_ALERTCOMMAND = "alert";
	private final static String ARG_RECOVERCOMMAND = "recover";
	private final static String ARG_RECOVERDELAY = "rdelay";

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {

		String sql = "";
		String xml = "";

		try {
			int id;

			String ids = getArgument(ARG_ALERT_ID);
			String desc = getArgument(ARG_DESCRIPTION);
			String commandgroup = getArgument(ARG_GROUP);
			String condition = getArgument(ARG_CONDITION);
			String alert = getArgument(ARG_ALERTCOMMAND);
			String source = getArgument(ARG_SOURCE);
			Vector<String> argsVal = getArguments("arg");
			Vector<String> vals = getArguments("val");
			String args = "<args>";
			for (int count = 0; count < argsVal.size(); count++) {
				if (!argsVal.get(count).isEmpty()) {
					args += "<arg id=\"" + argsVal.get(count) + "\">" + vals.get(count) + "</arg>";
				}
			}
			args += "</args>";

			String recover = getArgument(ARG_RECOVERCOMMAND);
			String recoverDelay = getArgument(ARG_RECOVERDELAY);
			String recoverArgs = "";
			if (recover == null || recover.isEmpty()) {
				recover = "0";
				recoverDelay = "0";
			} else {
				recoverArgs = "<args>";
				Vector<String> recoverArgsVal = getArguments("rarg");
				Vector<String> recoverVals = getArguments("rval");
				for (int count = 0; count < recoverArgsVal.size(); count++) {
					if (!recoverArgsVal.get(count).isEmpty()) {
						recoverArgs += "<arg id=\"" + recoverArgsVal.get(count) + "\">" + recoverVals.get(count)
								+ "</arg>";
					}
				}
				recoverArgs += "</args>";
			}

			if (ids == null) {
				if (desc == null) {
					id = -1;
				} else {
					sql = "INSERT INTO tblalert(GroupId,ConditionGroupId,AlertCommandGroupId,InXML,Source,Description,LastExec,RecoverGroupId,RecoverInXML,RecoverDelay,RecoverPoll) VALUES ("
							+ commandgroup + "," + condition + "," + alert + ",'" + args + "'," + source + ",'" + desc
							+ "',0," + recover + ",'" + recoverArgs + "'," + recoverDelay + ",0)";
					ResultSet rs = system.executeQuery(sql);
					if (rs.next())
						id = rs.getInt(1);
					else
						id = -1;
				}
			} else {
				id = Integer.parseInt(ids);
				if (desc != null) {
					sql = "UPDATE tblalert SET GroupId=" + commandgroup + ", ConditionGroupId=" + condition
							+ ", AlertCommandGroupId=" + alert + ", InXML='" + args + "', Source=" + source
							+ ", Description='" + desc + "',RecoverGroupId=" + recover + ",RecoverInXML='" + recoverArgs
							+ "',RecoverDelay=" + recoverDelay + " WHERE AlertId=" + id;
					system.executeQuery(sql);
				}
			}

			xml = "<alert>";
			ResultSet rsg = system.executeQuery("SELECT GroupId, Description FROM tblcommandgroup");
			xml += "<commands>";
			while (rsg.next()) {
				xml += "<command>";
				xml += "<id>" + rsg.getInt(1) + "</id>";
				xml += "<description>" + rsg.getString(2) + "</description>";
				xml += "</command>";
			}
			xml += "</commands>";
			ResultSet rsc = system.executeQuery("SELECT ConditionGroupId, Description FROM tblconditiongroup");
			xml += "<conditions>";
			while (rsc.next()) {
				xml += "<condition>";
				xml += "<id>" + rsc.getInt(1) + "</id>";
				xml += "<description>" + rsc.getString(2) + "</description>";
				xml += "</condition>";
			}
			xml += "</conditions>";
			xml += system.getSourcesXML();
			if (id != -1) {
				sql = "SELECT GroupId,ConditionGroupId,AlertCommandGroupId,InXML,Source,Description,RecoverGroupId,RecoverInXML,RecoverDelay FROM tblalert WHERE AlertId="
						+ id;
				ResultSet rs = system.executeQuery(sql);
				if (rs.next()) {
					xml += "<alertid>" + id + "</alertid>";
					xml += "<description>" + rs.getString(6) + "</description>";
					xml += "<group>" + rs.getInt(1) + "</group>";
					xml += "<condition>" + rs.getInt(2) + "</condition>";
					xml += "<source>" + rs.getInt(5) + "</source>";
					xml += "<alert>" + rs.getInt(3) + "</alert>";
					xml += "<condargs>" + rs.getString(4) + "</condargs>";
					xml += "<recover>" + rs.getInt(7) + "</recover>";
					xml += "<recargs>" + rs.getString(8) + "</recargs>";
					xml += "<delay>" + rs.getInt(9) + "</delay>";
				} else
					return new BadArgumentResponse(ARG_ALERT_ID, "No such alert:" + id);
			} else {
				xml += "<description>New Alert</description>";
				xml += "<source>" + HeatingSystem.SOURCE_POLL + "</source>";
			}
			xml += "</alert>";
			return new SuccessResponse(xml, getArgumentsXML());
		} catch (NumberFormatException e) {
			return new BadArgumentResponse(ARG_ALERT_ID, e.getMessage());
		} catch (SQLException e) {
			return new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
		}
	}

	@Override
	public String getDescription(String mode) {
		return "Create/Update alert";
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_EDIT));
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
