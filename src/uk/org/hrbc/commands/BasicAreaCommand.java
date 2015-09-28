package uk.org.hrbc.commands;

import java.io.IOException;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.DataResponse;
import uk.org.hrbc.commands.responses.RetryFailedResponse;
import uk.org.hrbc.commands.responses.TimeoutResponse;
import uk.org.hrbc.commands.responses.WriteErrorResponse;
import uk.org.hrbc.comms.exceptions.CommsResendException;
import uk.org.hrbc.comms.exceptions.CommsTimeoutException;

public abstract class BasicAreaCommand extends BasicCommand {

  public BasicAreaCommand(String tag) {
    super();
    setTag(tag);
  }

  final protected CommandResponse executeGet(HeatingSystem system, String zoneCommand, String contCommand, boolean cont) {
    Vector<String> zones = getArguments("zone");
    String ret = "";
    try {
      for (String zone : zones) {
        if (system.isZone(zone) && (zoneCommand != null)) {
          if (cont)
            ret += getXMLResponse(system, zone, system.getController(zone), zoneCommand);
          else
            ret += getXMLResponse(system, zone, zone, zoneCommand);
        } else if (system.isController(zone) && (contCommand != null))
          ret += getXMLResponse(system, zone, zone, contCommand);
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

  protected String getXMLResponse(HeatingSystem system, String zone, String obj, String command) throws IOException,
      CommsTimeoutException, CommsResendException {
    String resp = "<" + getTag() + "><zone>" + zone + "</zone>";
    resp += super.get(system, command, obj);
    resp += "</" + getTag() + ">";
    return resp;
  }

  final protected void executeSet(HeatingSystem system, String zoneCommand, String contCommand, String valueArg)
      throws IOException, CommsTimeoutException {
    Vector<String> zones = getArguments("zone");
    Vector<String> vals = getArguments(valueArg);

    int ind = 0;
    for (String zone : zones) {
      if (vals.size() > ind) {
        if (system.isZone(zone)) {
          if (zoneCommand != null)
            set(system, zoneCommand, zone, vals.get(ind));
        } else if (contCommand != null)
          set(system, contCommand, zone, vals.get(ind));
      }
      ind++;
    }
  }

  @Override
  public String getDefaultArgXML(HeatingSystem system) {
    return "<arg id=\"zone\">" + system.getParam(HeatingSystem.PARAM_DEFAULTZONE) + "</arg>";
  }
}
