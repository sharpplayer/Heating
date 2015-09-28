package uk.org.hrbc.commands;

import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.BadArgumentResponse;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class HouseKeepingCommand extends CommandImpl {

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {
    Vector<String> delete = getArguments("delete");
    if (delete.contains("all"))
      delete.add("pending");
    String sql;
    boolean deleteDone = false;
    for (String del : delete) {
      long time = Long.parseLong(system.getParam(HeatingSystem.PARAM_HOUSEKEEPING_NON_DATA));
      String col = "ExecTimestamp";
      if (del.equalsIgnoreCase("web"))
        sql = "DELETE FROM tblcompleted WHERE Source=" + HeatingSystem.SOURCE_WEB;
      else if (del.equalsIgnoreCase("nondata"))
        sql = "DELETE FROM tblcompleted WHERE Data=0";
      else if (del.equalsIgnoreCase("all"))
        sql = "DELETE FROM tblcompleted";
      else if (del.equalsIgnoreCase("cmd"))
        sql = "DELETE FROM tblcompleted WHERE Source=" + HeatingSystem.SOURCE_COMMAND_LINE;
      else if (del.equalsIgnoreCase("alert"))
        sql = "DELETE FROM tblcompleted WHERE Source=" + HeatingSystem.SOURCE_ALERT;
      else if (del.equalsIgnoreCase("poll"))
        sql = "DELETE FROM tblcompleted WHERE Source=" + HeatingSystem.SOURCE_POLL + " AND Data=0";
      else if (del.equalsIgnoreCase("data")) {
        sql = "DELETE FROM tblcompleted WHERE Source=" + HeatingSystem.SOURCE_POLL + " AND Data=1";
        time = Long.parseLong(system.getParam(HeatingSystem.PARAM_HOUSEKEEPING_DATA));
      } else if (del.equalsIgnoreCase("pending")) {
        sql = "DELETE FROM tblpending WHERE Completed=1";
        col = "InTimestamp";
      } else
        return new BadArgumentResponse("delete", del);
      if (!delete.contains("all")) {
        Date before = new Date(System.currentTimeMillis() - time);
        sql += " AND " + col + "<='" + HeatingSystem.SQL_DATE.format(before) + "'";
      }
      try {
        system.executeQuery(sql);
        deleteDone = true;
      } catch (SQLException e) {
        return new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
      }
    }
    return new SuccessResponse("<housekeeping><status>" + (deleteDone ? "Data deleted" : "No data deleted")
        + "</status></housekeeping>", getArgumentsXML());
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    return null;
  }

  @Override
  public String getDescription(String mode) {
    return "Tidies up the database";
  }

  @Override
  public Vector<String> getModes() {
    Vector<String> modes = new Vector<String>();
    modes.add(HeatingSystem.MODE_DEFAULT);
    return modes;
  }

  @Override
  public boolean isPollable() {
    return true;
  }

  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_ADMIN;
  }
}
