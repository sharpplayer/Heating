package uk.org.hrbc.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.TimeoutResponse;
import uk.org.hrbc.commands.responses.WriteErrorResponse;
import uk.org.hrbc.comms.exceptions.CommsTimeoutException;

public class RequiredTemperatureCommand extends BasicAreaCommand {

  private final static String ARG_RQDTEMP = "required";

  public RequiredTemperatureCommand() {
    super("required");
  }

  @Override
  public String getDescription(String mode) {
    if (mode.equalsIgnoreCase(HeatingSystem.MODE_EDIT))
      return "Set set point temperature";
    else
      return "Get set point temperature";
  }

  @Override
  public Vector<String> getModes() {
    return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT, HeatingSystem.MODE_EDIT));
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
  public CommandResponse execute(HeatingSystem system) {
    try {
      executeSet(system, "K1", null, ARG_RQDTEMP);
      return super.executeGet(system, "K1", null, false);
    } catch (IOException e) {
      return new WriteErrorResponse(e.getMessage(), getArgumentsXML());
    } catch (CommsTimeoutException e) {
      return new TimeoutResponse(getArgumentsXML());
    }
  }

  @Override
  public boolean isSetCommand() {
    return hasMultipleArgs();
  }

  @Override
  public String getImmediateResponse() {
    String zone = getArgument("zone");
    String val = getArgument(ARG_RQDTEMP);
    String resp = "<" + getTag() + "><zone>" + zone + "</zone>";
    resp += "<value>" + val + "</value>";
    resp += "</" + getTag() + ">";
    return resp;
  }
}
