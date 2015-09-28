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

public class ConditionCommand extends CommandImpl {

  private final static String ARG_CONDITION_GROUP_ID = "conditionid";
  private final static String ARG_DESCRIPTION = "description";
  private final static String ARG_CONDITION = "condition";

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {

    String sql = "";
    StringBuilder xml = new StringBuilder("");

    try {
      int id;
      String ids = getArgument(ARG_CONDITION_GROUP_ID);
      String desc = getArgument(ARG_DESCRIPTION);
      Vector<String> conds = getArguments(ARG_CONDITION);

      if (ids == null) {
        if (desc == null) {
          id = -1;
        } else {
          sql = "INSERT INTO tblconditiongroup(Description) VALUES ('" + desc + "')";
          ResultSet rs = system.executeQuery(sql);
          if (rs.next())
            id = rs.getInt(1);
          else
            id = -1;
        }
      } else {
        id = Integer.parseInt(ids);
        if (desc != null) {
          sql = "UPDATE tblconditiongroup SET Description='" + desc + "'" + " WHERE ConditionGroupId=" + id;
          system.executeQuery(sql);
        }
      }

      if ((id != -1) && (desc != null)) {
        sql = "DELETE FROM tblconditiongroupx WHERE ConditionGroupId=" + id;
        system.executeQuery(sql);
        for (String cond : conds) {
          if (cond.length() > 0) {
            sql = "INSERT INTO tblconditiongroupx(ConditionGroupId, ConditionId) VALUES(" + id + "," + cond + ")";
            system.executeQuery(sql);
          }
        }
      }

      xml.append("<condition>");
      sql = "SELECT ConditionId, Description FROM tblcondition";
      ResultSet rsc = system.executeQuery(sql);
      xml.append("<conditions>");
      while (rsc.next()) {
        xml.append("<condition>");
        xml.append("<id>" + rsc.getInt(1) + "</id>");
        xml.append("<description>" + rsc.getString(2) + "</description>");
        xml.append("</condition>");
      }
      xml.append("</conditions>");

      if (id != -1) {
        xml.append("<conditionid>" + id + "</conditionid>");
        sql = "SELECT Description FROM tblconditiongroup WHERE ConditionGroupId=" + id;
        ResultSet rsd = system.executeQuery(sql);
        if (rsd.next())
          xml.append("<description>" + rsd.getString(1) + "</description>");
        sql = "SELECT ConditionId FROM tblconditiongroupx WHERE ConditionGroupId=" + id;
        ResultSet rscs = system.executeQuery(sql);
        xml.append("<conds>");
        while (rscs.next())
          xml.append("<condition>" + rscs.getInt(1) + "</condition>");
        xml.append("</conds>");
      } else
        xml.append("<description>New Condition</description>");
      xml.append("</condition>");

      return new SuccessResponse(xml.toString(), getArgumentsXML());
    } catch (NumberFormatException e) {
      return new BadArgumentResponse(ARG_CONDITION_GROUP_ID, e.getMessage());
    } catch (SQLException e) {
      return new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
    }
  }

  @Override
  public String getDescription(String mode) {
    return "Create/Update condition";
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
