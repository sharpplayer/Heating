package uk.org.hrbc.commands;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;

public class OptimumStopCommand extends BasicAreaCommand {

  public final static String TAG_OPT_OFF = "optimumoff";
  public final static String TAG_OPT_OFF_DEPRACATED = "opton";

  public OptimumStopCommand() {
    super(TAG_OPT_OFF);
  }

  @Override
  public String getDescription(String mode) {
    return "Get current optimum start state";
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    Hashtable<String, String> conds = super.getConditions(system);
    conds.put("Zone is optimum started", "/*/opton/value = '1'");
    conds.put("Zone is not optimum started", "/*/opton/value = '0'");
    return conds;
  }

  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_ADMIN;
  }

  @Override
  public CommandResponse execute(HeatingSystem system) {
    return super.executeGet(system, "I4", null, false);
  }
  @Override
  public Vector<String> getModes() {
    return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
  }

}
