package uk.org.hrbc.commands.responses;

public class BadEmailResponse extends CommandResponse {

  public BadEmailResponse(String email, String msg, String error, String args) {
    super("<address>" + email + "</address>" + "<message>" + msg + "</message>" + "<error>" + error + "</error>",
        CommandResponse.ERROR_BAD_ADDRESS, args);
  }

}
