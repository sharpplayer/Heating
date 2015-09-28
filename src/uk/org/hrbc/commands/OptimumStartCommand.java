package uk.org.hrbc.commands;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;

public class OptimumStartCommand extends BasicAreaCommand {

  public final static String TAG_OPT_ON = "optimumon";
  public final static String TAG_OPT_ON_DEPRACATED = "optoff";

  public OptimumStartCommand() {
    super(TAG_OPT_ON);
  }

  @Override
  public String getDescription(String mode) {
    return "Get current optimum stop state";
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    Hashtable<String, String> conds = super.getConditions(system);
    conds.put("Zone is optimum stopped", "/*/optoff/value = '1'");
    conds.put("Zone is not optimum stopped", "/*/optoff/value = '0'");
    return conds;
  }

  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_ADMIN;
  }

  @Override
  public CommandResponse execute(HeatingSystem system) {
    return super.executeGet(system, "I3", null, false);
  }

  @Override
  public Vector<String> getModes() {
    return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
  }
}
