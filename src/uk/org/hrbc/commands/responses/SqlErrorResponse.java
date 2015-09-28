package uk.org.hrbc.commands.responses;

public class SqlErrorResponse extends CommandResponse {

  public SqlErrorResponse(String sql, String message, String args) {
    super("<error><sql>" + sql.replace("'", "&apos;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
        + "</sql><message>"
        + message.replace("'", "&apos;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
        + "</message></error>", CommandResponse.ERROR_BAD_SQL, args);
  }

}
