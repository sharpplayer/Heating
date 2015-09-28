package uk.org.hrbc.commands.responses;

public class MessagingResponse extends CommandResponse {

  public MessagingResponse(String email, String msg, String error, String args) {
    super("<error><address>" + email + "</address>" + "<message>" + error + "</message>" + "<body>" + msg + "</body></error>",
        CommandResponse.ERROR_IN_MESSAGING, args);
  }

}
