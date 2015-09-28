package uk.org.hrbc.commands.responses;

public class WriteErrorResponse extends ErrorResponse {

  public WriteErrorResponse(String error, String args) {
    super(error, CommandResponse.ERROR_COMMS_WRITE, args);
  }

}
