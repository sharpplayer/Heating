package uk.org.hrbc.commands.responses;

public class XMLErrorResponse extends CommandResponse {

  public XMLErrorResponse(String badXml, String message, String args) {
    super("<error><xml><![CDATA[ " + badXml + "]]></xml><message>"
        + message.replace("'", "&apos;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
        + "</message></error>", CommandResponse.ERROR_XML, args);
  }
}
