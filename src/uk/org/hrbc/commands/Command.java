package uk.org.hrbc.commands;

import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;

public interface Command {

  public boolean isPollable();

  public Vector<String> getModes();

  public String getDescription(String mode);

  public CommandResponse execute(HeatingSystem system, String args, int complete);

  public Hashtable<String, String> getConditions(HeatingSystem system);
  
  public int getAccess();
  
  public String getArgumentsXML();
  
  public String getDefaultArgXML(HeatingSystem system);
}
