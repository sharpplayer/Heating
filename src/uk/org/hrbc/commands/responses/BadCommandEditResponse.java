package uk.org.hrbc.commands.responses;

public class BadCommandEditResponse extends ErrorResponse {

  public BadCommandEditResponse(String args) {
    super("Cannot modify system command groups", CommandResponse.ERROR_SYSTEM_COMMAND, args);
  }
}
