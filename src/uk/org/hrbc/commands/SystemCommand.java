package uk.org.hrbc.commands;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.BadArgumentResponse;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class SystemCommand extends CommandImpl {

  private final static String ARG_STATUS = "status";

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {
    String stat = getArgument(ARG_STATUS);
    String statuses = "<statuses><status><name>status</name><description>Check Status</description></status><status><name>stop</name><description>Stop System</description></status></statuses>";
    if (stat == null)
      stat = "status";
    if (stat.equalsIgnoreCase("stop")) {
      system.stop();
      return new SuccessResponse("<system><status>Stopped</status></system>", getArgumentsXML());
    } else if (stat.equalsIgnoreCase("status"))
      return new SuccessResponse("<system>" + statuses + "<status>Running</status></system>", getArgumentsXML());
    else
      return new BadArgumentResponse(ARG_STATUS, stat);
  }

  @Override
  public String getDescription(String mode) {
    if (mode.equalsIgnoreCase(HeatingSystem.MODE_EDIT))
      return "Set system status";
    else
      return "Get system status";
  }

  @Override
  public Vector<String> getModes() {
    return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT, HeatingSystem.MODE_EDIT));
  }

  @Override
  public boolean isPollable() {
    return false;
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    Hashtable<String, String> conds = new Hashtable<String, String>();
    conds.put("System stopped", "/*/system/status = 'Stopped'");
    return conds;
  }
  
  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_ADMIN;
  }
  
  @Override
  public String getDefaultArgXML(HeatingSystem system) {
    return "<arg id=\"status\">status</arg>";
  }
}
