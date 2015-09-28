package uk.org.hrbc.commands.responses;

public class MissingArgumentResponse extends CommandResponse {

  public MissingArgumentResponse(String arg, String args) {
    super("<error><arg>" + arg + "</arg><message>Missing argument</message</error>", CommandResponse.ERROR_MISSING_ARGUMENT, args);
  }
}
