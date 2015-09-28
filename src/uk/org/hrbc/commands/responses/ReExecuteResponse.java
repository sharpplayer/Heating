package uk.org.hrbc.commands.responses;

public class ReExecuteResponse extends CommandResponse {

  public ReExecuteResponse(String xml, String args) {
    super(xml, CommandResponse.SUCCESS, args);
  }
}
