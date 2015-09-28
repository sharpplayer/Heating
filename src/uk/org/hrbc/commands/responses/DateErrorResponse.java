package uk.org.hrbc.commands.responses;

public class DateErrorResponse extends ErrorResponse {

  public DateErrorResponse(String args) {
    super("Invalid date", CommandResponse.ERROR_BAD_DATE, args);
  }
}
