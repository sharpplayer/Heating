package uk.org.hrbc.commands.responses;

public class DataResponse extends SuccessResponse {

  public DataResponse(String xmlMessage, String args) {
    super(xmlMessage, xmlMessage.length() > 0, args);
  }

}
