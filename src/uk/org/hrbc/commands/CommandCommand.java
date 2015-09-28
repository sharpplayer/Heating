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

public class CommandCommand extends CommandImpl {

  private final static String ARG_COMMAND_GROUP_ID = "commandid";
  private final static String ARG_DESCRIPTION = "description";
  private final static String ARG_COMMAND = "condition";
  private final static String ARG_ACCESS = "access";

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {

    String sql = "";
    StringBuilder xml = new StringBuilder("");

    try {
      int id;
      String ids = getArgument(ARG_COMMAND_GROUP_ID);
      String desc = getArgument(ARG_DESCRIPTION);
      String access = getArgument(ARG_ACCESS);
      Vector<String> cmds = getArguments(ARG_COMMAND);
      boolean accessEditOnly = false;

      if (ids == null) {
        if (desc == null) {
          id = -1;
        } else {
          sql = "INSERT INTO tblcommandgroup(Description,Pollable,Type,Access) VALUES ('" + desc + "',0,"
              + HeatingSystem.TYPE_USER + "," + access + ")";
          ResultSet rs = system.executeQuery(sql);
          if (rs.next())
            id = rs.getInt(1);
          else
            id = -1;
        }
      } else {
        id = Integer.parseInt(ids);
        sql = "SELECT Type FROM tblcommandgroup WHERE GroupId=" + id;
        ResultSet rst = system.executeQuery(sql);
        if (rst.next()) {
          if (rst.getInt(1) == HeatingSystem.TYPE_NO_EDIT)
            accessEditOnly = true;
        }
        if (access != null) {
          if (accessEditOnly) {
            sql = "UPDATE tblcommandgroup SET Access=" + access + " WHERE GroupId=" + id;
            system.executeQuery(sql);
          } else {
            sql = "UPDATE tblcommandgroup SET Description='" + desc + "'" + ", Access=" + access + " WHERE GroupId="
                + id;
            system.executeQuery(sql);
          }
        }
      }

      if ((id != -1) && (desc != null) && !accessEditOnly) {
        sql = "DELETE FROM tblcommandgroupx WHERE GroupId=" + id;
        system.executeQuery(sql);
        int order = 1;
        boolean pollable = true;
        for (String cmdgrp : cmds) {
          if (cmdgrp.length() > 0) {
            sql = "SELECT CommandId, Pollable, Mode FROM tblcommandgroupx LEFT JOIN tblcommandgroup ON tblcommandgroupx.GroupId=tblcommandgroup.GroupId WHERE tblcommandgroupx.GroupId="
                + cmdgrp;
            ResultSet rs = system.executeQuery(sql);
            if (rs.next()) {
              sql = "INSERT INTO tblcommandgroupx(GroupId, CommandId, tblcommandgroupx.Order, Mode) VALUES(" + id + ","
                  + rs.getInt(1) + "," + order + ",'" + rs.getString(3) + "')";
              system.executeQuery(sql);
              order++;
              pollable &= (rs.getInt(2) == 1);
            }
          }
        }
        sql = "UPDATE tblcommandgroup SET Pollable=" + (pollable ? "1" : "0") + " WHERE GroupId=" + id;
        system.executeQuery(sql);
      }

      xml.append("<command>");
      sql = "SELECT tblcommandgroup.GroupId, Description FROM tblcommandgroup LEFT JOIN tblcommandgroupx ON tblcommandgroup.GroupId=tblcommandgroupx.GroupId";
      ResultSet rsc = system.executeQuery(sql);
      xml.append("<commands>");
      while (rsc.next()) {
        xml.append("<command>");
        xml.append("<id>" + rsc.getInt(1) + "</id>");
        xml.append("<description>" + rsc.getString(2) + "</description>");
        xml.append("</command>");
      }
      xml.append("</commands>");

      if (id != -1) {
        xml.append("<commandid>" + id + "</commandid>");
        sql = "SELECT Description, Access, Type FROM tblcommandgroup WHERE GroupId=" + id;
        ResultSet rsd = system.executeQuery(sql);
        if (rsd.next()) {
          xml.append("<description>" + rsd.getString(1) + "</description>");
          xml.append("<access>" + rsd.getString(2) + "</access>");
          xml.append("<modifiable>" + (rsd.getInt(3) == HeatingSystem.TYPE_USER ? 1 : 0) + "</modifiable>");
        }
        sql = "SELECT CommandId, Mode FROM tblcommandgroupx WHERE GroupId=" + id;
        ResultSet rscs = system.executeQuery(sql);
        xml.append("<cmds>");
        while (rscs.next()) {
          sql = "SELECT tblcommandgroupx.GroupId FROM tblcommandgroupx LEFT JOIN tblcommandgroup ON tblcommandgroupx.GroupId=tblcommandgroup.GroupId WHERE CommandId="
              + rscs.getInt(1) + " AND Type=" + HeatingSystem.TYPE_NO_EDIT + " AND Mode='" + rscs.getString(2) + "'";
          ResultSet rs = system.executeQuery(sql);
          if (rs.next())
            xml.append("<command>" + rs.getInt(1) + "</command>");
        }
        xml.append("</cmds>");
      } else {
        xml.append("<description>New Command Group</description>");
        xml.append("<access>" + HeatingSystem.ACCESS_NORMAL + "</access>");
        xml.append("<modifiable>1</modifiable>");
      }
      xml.append("</command>");

      return new SuccessResponse(xml.toString(), getArgumentsXML());
    } catch (NumberFormatException e) {
      return new BadArgumentResponse(ARG_COMMAND_GROUP_ID, e.getMessage());
    } catch (SQLException e) {
      return new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
    }
  }

  @Override
  public String getDescription(String mode) {
    return "Create/Update command";
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
