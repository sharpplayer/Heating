package uk.org.hrbc.commands.responses;

public class ErrorResponse extends CommandResponse {

  protected ErrorResponse(String error, int code, String args) {
    super("<error><message>" + error + "</message></error>", code, args);
  }

}
