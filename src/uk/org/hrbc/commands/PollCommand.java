package uk.org.hrbc.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.BadArgumentResponse;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class PollCommand extends CommandImpl {

  private final static String ARG_POLL_ID = "pollid";
  private final static String ARG_GROUP = "group";
  private final static String ARG_FREQ = "interval";
  private final static String ARG_ACTIVE = "active";
  private final static String ARG_OFFSET = "offset";

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {

    String sql = "";
    String xml = "";
    int FIFTEEN_MINS = 15 * 60 * 1000;

    try {
      int id;

      String ids = getArgument(ARG_POLL_ID);
      String command = getArgument(ARG_GROUP);
      String active = getArgument(ARG_ACTIVE);
      String offset = getArgument(ARG_OFFSET);

      Vector<String> argsVal = getArguments("arg");
      Vector<String> vals = getArguments("val");
      String args = "<args>";
      for (int count = 0; count < argsVal.size(); count++) {
        args += "<arg id=\"" + argsVal.get(count) + "\">" + vals.get(count) + "</arg>";
      }
      args += "</args>";

      if (active == null)
        active = "0";
      else if (active.equalsIgnoreCase("on"))
        active = "1";
      if (ids == null) {
        if (command == null) {
          id = -1;
        } else {

          sql = "INSERT INTO tblpoll(GroupId, Frequency, NextPoll, StartTime, Active, InXML) VALUES(" + command + ","
              + getArgument(ARG_FREQ) + "," + offset + ",'" + HeatingSystem.SQL_DATE.format(new Date()) + "'," + active
              + ",'" + args + "')";
          ResultSet rs = system.executeQuery(sql);
          if (rs.next())
            id = rs.getInt(1);
          else
            id = -1;
        }
      } else {
        id = Integer.parseInt(ids);
        if (command != null) {
          sql = "UPDATE tblpoll SET GroupId=" + getArgument(ARG_GROUP) + ", Frequency=" + getArgument(ARG_FREQ)
              + ", NextPoll=" + offset + ", InXML='" + args + "', Active=" + active + " WHERE PollId=" + id;
          system.executeQuery(sql);
        }
      }

      xml = "<poll>";
      ResultSet rsg = system.executeQuery("SELECT GroupId, Description FROM tblcommandgroup WHERE Pollable=1");
      xml += "<commands>";
      while (rsg.next()) {
        xml += "<command>";
        xml += "<id>" + rsg.getInt(1) + "</id>";
        xml += "<description>" + rsg.getString(2) + "</description>";
        xml += "</command>";
      }
      xml += "</commands>";
      if (id != -1) {
        sql = "SELECT StartTime, NextPoll, EndTime, Frequency, Active, GroupId, InXML FROM tblpoll WHERE PollId=" + id;
        ResultSet rs = system.executeQuery(sql);
        if (rs.next()) {
          xml += "<pollid>" + id + "</pollid>";
          xml += system.getTimeXML(rs.getTimestamp(1), "start");
          xml += system.getTimeXML(new Date(rs.getLong(2)), "next");
          xml += system.getTimeXML(rs.getTimestamp(3), "end");
          xml += "<interval>" + rs.getInt(4) + "</interval>";
          xml += "<active>" + (rs.getInt(5) == 1 ? "1" : "0") + "</active>";
          xml += "<group>" + rs.getInt(6) + "</group>";
          xml += "<offset>" + (rs.getLong(2) % rs.getInt(4)) + "</offset>";
          xml += rs.getString(7);
        } else
          return new BadArgumentResponse(ARG_POLL_ID, "No such poll:" + id);
      } else {
        xml += "<interval>" + FIFTEEN_MINS + "</interval>";
        xml += "<active>1</active>";
      }
      xml += "</poll>";
      return new SuccessResponse(xml, getArgumentsXML());
    } catch (NumberFormatException e) {
      return new BadArgumentResponse(ARG_POLL_ID, e.getMessage());
    } catch (SQLException e) {
      return new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
    }
  }

  @Override
  public String getDescription(String mode) {
    return "Create/Update poll";
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
