package uk.org.hrbc.commands;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;

public class FlowTemperatureCommand extends BasicAreaCommand {

  public FlowTemperatureCommand() {
    super("flow");
  }

  @Override
  public String getDescription(String mode) {
    return "Get current flow temperature";
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    Hashtable<String, String> conds = super.getConditions(system);
    int[] temps = { 5, 10, 15, 18, 20, 21, 22, 23, 24, 25 };
    for (int temp : temps)
      conds.put("Flow temperature is less than " + temp + " degrees", "number(/*/flow/value) &lt; " + temp);
    for (int temp : temps)
      conds.put("Flow temperature is greater than " + temp + " degrees", "number(/*/flow/value) &gt; " + temp);
    return conds;
  }
  
  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_ADMIN;
  }

  @Override
  public CommandResponse execute(HeatingSystem system) {
    return super.executeGet(system, "S7", "S1", true);
  }
  
  @Override
  public Vector<String> getModes() {
    return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
  }
}
