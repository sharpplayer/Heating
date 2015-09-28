package uk.org.hrbc.commands.responses;

public class NullResponseResponse extends CommandResponse {

  public NullResponseResponse(String clazz, String args) {
    super("<error><class>" + clazz + "</class><message>Null Response</message></error>",
        CommandResponse.ERROR_NULL_RESPONSE, args);
  }
}
