package uk.org.hrbc.commands.responses;

public class ICSParseErrorResponse extends CommandResponse {

  public ICSParseErrorResponse(String badUrl, String message, String args) {
    super("<error><url>" + badUrl.replace("&", "&amp;") + "</url><message>"
        + message.replace("'", "&apos;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
        + "</message></error>", CommandResponse.ERROR_ICS, args);
  }
}
