package uk.org.hrbc.commands.responses;

public class SuccessResponse extends CommandResponse {

  public SuccessResponse(String xmlMessage, String args) {
    super(xmlMessage, CommandResponse.SUCCESS, args);
  }

  protected SuccessResponse(String xmlMessage, boolean isData, String args) {
    super(xmlMessage, CommandResponse.SUCCESS, isData, args);
  }

  
}
