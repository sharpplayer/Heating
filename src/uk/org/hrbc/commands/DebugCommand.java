package uk.org.hrbc.commands;

import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class DebugCommand extends CommandImpl {

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {
    return new SuccessResponse("<debugs></debugs>", getArgumentsXML());
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    return null;
  }

  @Override
  public String getDescription(String mode) {
    return "Debug output";
  }

  @Override
  public Vector<String> getModes() {
    Vector<String> modes = new Vector<String>();
    modes.add(HeatingSystem.MODE_DEFAULT);
    return modes;
  }

  @Override
  public boolean isPollable() {
    return true;
  }

  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_ADMIN;
  }
}
