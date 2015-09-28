package uk.org.hrbc.commands.responses;

public class RetryFailedResponse extends ErrorResponse {

  public RetryFailedResponse(String args) {
    super("Retry attempts exceeded in sending data heating system.", CommandResponse.ERROR_COMMS_RETRY_EXCEED, args);
  }

}
