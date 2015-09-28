package uk.org.hrbc.commands;

import java.util.Arrays;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;

public class ActualRequiredTemperatureCommand extends BasicAreaCommand {

  public ActualRequiredTemperatureCommand() {
    super("actual");
  }

  @Override
  public String getDescription(String mode) {
    return "Get required temperature";
  }
  
  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_NORMAL;
  }

  @Override
  public CommandResponse execute(HeatingSystem system) {
    return super.executeGet(system, "S3", null, false);
  }
  
  @Override
  public Vector<String> getModes() {
    return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
  }
}
