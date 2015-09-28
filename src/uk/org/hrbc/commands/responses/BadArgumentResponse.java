package uk.org.hrbc.commands.responses;

public class BadArgumentResponse extends ErrorResponse {

  public BadArgumentResponse(String error, String args) {
    super(error, CommandResponse.ERROR_ARGUMENT_PARSE, args);
  }

  public BadArgumentResponse(String argument, String value, String args) {
    super("Invalid argument:" + argument + " value:" + value, CommandResponse.ERROR_BAD_ARGUMENT, args);
  }
}
