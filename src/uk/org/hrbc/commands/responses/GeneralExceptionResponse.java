package uk.org.hrbc.commands.responses;

public class GeneralExceptionResponse extends ErrorResponse {

  public GeneralExceptionResponse(String command, String value, String args) {
    super("General Exception in:" + command + " error:" + value, CommandResponse.ERROR_BAD_ARGUMENT, args);
  }
}
