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

public class CommandsCommand extends CommandImpl {

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {
    String sAccess = getArgument("access");
    if ((sAccess == null) || (sAccess.length() == 0))
      sAccess = Integer.toString(HeatingSystem.ACCESS_NORMAL);
    boolean mod = (sAccess.equals(Integer.toString(HeatingSystem.ACCESS_ADMIN)));
    String sXml = "<commands>";
    String sSql = "SELECT tblcommandgroup.GroupId, Description, Mode, Type, Access FROM tblcommandgroup LEFT JOIN tblcommandgroupx ON tblcommandgroup.GroupId = tblcommandgroupx.GroupId WHERE tblcommandgroupx.Order=1 AND Access>="
        + sAccess;
    ResultSet rs;
    try {
      rs = system.executeQuery(sSql);
      while (rs.next()) {
        sXml += "<group><command>" + rs.getInt(1) + "</command>";
        sXml += "<description>" + rs.getString(2) + "</description>";
        sXml += "<mode>" + rs.getString(3) + "</mode>";
        if (mod)
          sXml += "<modifiable>" + rs.getInt(4) + "</modifiable>";
        sXml += "<access>" + rs.getInt(5) + "</access>";
        sXml += "</group>";
      }
      sXml += "</commands>";
      return new SuccessResponse(sXml, getArgumentsXML());
    } catch (SQLException e) {
      return new SqlErrorResponse(sSql, e.getMessage(), getArgumentsXML());
    }
  }

  @Override
  public String getDescription(String mode) {
    return "List commands";
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

  @Override
  public String getDefaultArgXML(HeatingSystem system) {
    return "<arg id=\"access\">" + HeatingSystem.ACCESS_NORMAL + "</arg>";
  }
}
