package uk.org.hrbc.commands.responses;

public class RecycledResponse extends CommandResponse {

  public RecycledResponse(String xml, String args) {
    super(xml, CommandResponse.SUCCESS, args);
  }
}
