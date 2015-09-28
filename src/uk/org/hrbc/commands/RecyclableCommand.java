package uk.org.hrbc.commands;

import java.sql.ResultSet;
import java.sql.SQLException;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.CompleteWhenPossibleResponse;
import uk.org.hrbc.commands.responses.ReExecuteResponse;
import uk.org.hrbc.commands.responses.RecycledResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;

public abstract class RecyclableCommand extends CommandImpl {

  private String tag;

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {
    String sql = "";
    try {
      if (complete == HeatingSystem.COMPLETE_INSTANTLY) {
        if (isSetCommand()) {
          return new ReExecuteResponse(getImmediateResponse(), getArgumentsXML());
        } else {
          String pureArgs = getArgumentsXML().replace("<args>", "").replace("</args>", "");
          sql = "SELECT InXML, OutXML FROM tblcompleted LEFT JOIN tblcommand ON tblcompleted.CommandId=tblcommand.CommandId WHERE Class='"
              + getClass().getCanonicalName()
              + "' AND InXML LIKE '%"
              + pureArgs
              + "%' AND Success="
              + CommandResponse.SUCCESS + " ORDER BY CompletedId DESC LIMIT 1";
          ResultSet rs = system.executeQuery(sql);
          if (rs.next()) {
            String xml = rs.getString(2);
            int x = xml.indexOf(getArgXML());
            int z = xml.substring(0, x).lastIndexOf("<" + getTag() + ">");
            int y = xml.indexOf("</" + getTag() + ">", z) + ("</" + getTag() + ">").length();
            String ret = xml.substring(z, y);
            return new RecycledResponse(ret, getArgumentsXML());
          } else
            return new CompleteWhenPossibleResponse();
        }
      } else {
        System.out.println("Executing:" + getClass().getCanonicalName());
        return execute(system);
      }
    } catch (SQLException e) {
      return new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
    }
  }

  public abstract CommandResponse execute(HeatingSystem system);

  public String getImmediateResponse() {
    return "";
  }

  final public String getTag() {
    return tag;
  }

  final public void setTag(String tg) {
    tag = tg;
  }

  public boolean isSetCommand() {
    return false;
  }

  public boolean hasMultipleArgs() {
    int x = getArgumentsXML().indexOf("<arg ");
    if (x == -1)
      return false;
    x = getArgumentsXML().indexOf("<arg ", x + 1);
    if (x == -1)
      return false;
    else
      return true;
  }

  public String getArgXML() {
    String zone = getArgument("zone");
    return "<zone>" + zone + "</zone>";
  }

}
