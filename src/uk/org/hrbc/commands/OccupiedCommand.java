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

public class OccupiedCommand extends BasicAreaCommand {

  public final static String TAG_OCCUPIED="occupied";
  
  public OccupiedCommand() {
    super(TAG_OCCUPIED);
  }

  @Override
  public String getDescription(String mode) {
    return "Get current occupied state";
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    Hashtable<String, String> conds = super.getConditions(system);
    conds.put("Zone is occupied", "/*/occupied/value = '1'");
    conds.put("Zone is not occupied", "/*/occupied/value = '0'");
    return conds;
  }

  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_ADMIN;
  }

  @Override
  public CommandResponse execute(HeatingSystem system) {
    Vector<String> zones = getArguments("zone");
    String ret = "";

    if (zones.size() == 0)
      zones.add(system.getParam(HeatingSystem.PARAM_DEFAULTZONE));
    try {
      for (String zone : zones) {
        if (system.isZone(zone) || !system.isController(zone))
          ret += getXMLResponse(system, zone, zone, "I2");
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
}
