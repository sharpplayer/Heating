package uk.org.hrbc.commands.responses;

public class ClassErrorResponse extends CommandResponse {

  public ClassErrorResponse(String clazz, String error) {
    super("<error><class>" + clazz + "</class><message>" + error + "</message></error>", CommandResponse.ERROR_IN_CLASS, "");
  }

}
