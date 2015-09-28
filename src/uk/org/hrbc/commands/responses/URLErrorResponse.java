package uk.org.hrbc.commands.responses;

public class URLErrorResponse extends CommandResponse {

  public URLErrorResponse(String badUrl, String message, String args) {
    super("<error><url>" + badUrl.replace("&", "&amp;") + "</url><message>"
        + message.replace("'", "&apos;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
        + "</message></error>", CommandResponse.ERROR_URL, args);
  }
}
