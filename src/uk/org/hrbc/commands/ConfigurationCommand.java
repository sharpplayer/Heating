package uk.org.hrbc.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class ConfigurationCommand extends CommandImpl {

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {

    String sSql = "";

    try {
      Vector<String> ids = getArguments("id");
      Vector<String> vals = getArguments("val");
      if (ids.size() > 0) {
        for (int loop = 0; loop < ids.size(); loop++) {
          sSql = "UPDATE tblconfig SET value='" + vals.get(loop) + "' WHERE ConfigId=" + ids.get(loop) + " AND Modifiable=1";
          system.executeQuery(sSql);
        }
        system.reset();
      }

      String sXml = "<configs>";
      sSql = "SELECT ConfigId, Param, Value, Description, Modifiable FROM tblconfig";
      ResultSet rs;

      rs = system.executeQuery(sSql);
      while (rs.next()) {
        sXml += "<config><id>" + rs.getInt(1) + "</id>";
        sXml += "<param>" + rs.getString(2) + "</param>";
        String value = rs.getString(3);
        value = value.replace("&", "&amp;");
        value = value.replace("<", "&lt;");
        value = value.replace(">", "&gt;");
        value = value.replace("'", "&apos;");
        value = value.replace("\"", "&quot;");
        sXml += "<value>" + value + "</value>";
        sXml += "<description>" + rs.getString(4) + "</description>";
        sXml += "<modifiable>" + rs.getInt(5) + "</modifiable>";
        sXml += "</config>";
      }
      sXml += "</configs>";
      return new SuccessResponse(sXml, getArgumentsXML());
    } catch (SQLException e) {
      return new SqlErrorResponse(sSql, e.getMessage(), getArgumentsXML());
    }
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    return null;
  }

  @Override
  public String getDescription(String mode) {
    if (mode.equalsIgnoreCase(HeatingSystem.MODE_EDIT))
      return "Modify configuration values";
    else
      return "List configuration values";
  }

  @Override
  public Vector<String> getModes() {
    Vector<String> modes = new Vector<String>();
    modes.add(HeatingSystem.MODE_DEFAULT);
    modes.add(HeatingSystem.MODE_EDIT);
    return modes;
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
