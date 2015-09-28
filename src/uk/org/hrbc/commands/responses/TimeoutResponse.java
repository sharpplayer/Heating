package uk.org.hrbc.commands.responses;

public class TimeoutResponse extends ErrorResponse {

  public TimeoutResponse(String args) {
    super("Timeout receiving data from heating system.", CommandResponse.ERROR_COMMS_TIMEOUT, args);
  }

}
