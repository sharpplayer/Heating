package uk.org.hrbc.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.DataResponse;
import uk.org.hrbc.commands.responses.RetryFailedResponse;
import uk.org.hrbc.commands.responses.TimeoutResponse;
import uk.org.hrbc.commands.responses.WriteErrorResponse;
import uk.org.hrbc.comms.exceptions.CommsResendException;
import uk.org.hrbc.comms.exceptions.CommsTimeoutException;

public class BoilerStateCommand extends BasicAreaCommand {

  public BoilerStateCommand() {
    super("boiler");
  }

  @Override
  public String getDescription(String mode) {
    return "Get boiler state";
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    Hashtable<String, String> conds = super.getConditions(system);
    conds.put("Boiler is off", "/*/boiler/state/value = 'Off'");
    conds.put("Boiler is on", "/*/boiler/state/value = 'On'");
    conds.put("Boiler is locked out", "/*/boiler/state/value = 'Locked'");
    return conds;
  }

  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_NORMAL;
  }

  @Override
  public CommandResponse execute(HeatingSystem system) {

    String[] cascades = { "main", "c1", "c2" };
    Vector<String> zones = getArguments("zone");
    if (zones.size() == 0)
      zones.add(system.getParam(HeatingSystem.PARAM_DEFAULTCONT));
    String ret = "";
    try {
      for (String zone : zones) {
        if (system.isController(zone)) {
          ret += "<" + getTag() + ">";
          ret += "<zone>" + zone + "</zone>";
          for (String cas : cascades) {
            ret += "<state>";
            String casName = zone + (cas.equals("main") ? "" : cas);
            String stat = super.get(system, (cas.equals("main") ? "I3" : "I6"), casName);
            ret += "<cascade>" + cas + "</cascade>";
            if (stat.contains("0"))
              ret += "<value>Off</value>";
            else {
              stat = super.get(system, "I1", casName);
              if (stat.contains("0"))
                ret += "<value>On</value>";
              else
                ret += "<value>Locked</value>";
            }
            ret += "</state>";
          }
          ret += "</" + getTag() + ">";
        }
      }
      return new DataResponse(ret, getArgumentsXML());
    } catch (IOException e) {
      return new WriteErrorResponse(e.getMessage(), getArgumentsXML());
    } catch (CommsTimeoutException e) {
      return new TimeoutResponse(getArgumentsXML());
    } catch (CommsResendException e) {
      return new RetryFailedResponse(getArgumentsXML());
    }
  }

  @Override
  public Vector<String> getModes() {
    return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
  }

  @Override
  public String getDefaultArgXML(HeatingSystem system) {
    return "<arg id=\"zone\">" + system.getParam(HeatingSystem.PARAM_DEFAULTCONT) + "</arg>";
  }
}
