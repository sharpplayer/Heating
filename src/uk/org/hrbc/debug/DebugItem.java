package uk.org.hrbc.debug;

public class DebugItem {

  public final static int NONE = 0;
  public final static int ERROR = 1;
  public final static int WARNING = 2;
  public final static int INFO = 3;

  private int level = INFO;
  private long timestamp;
  private String message;

  public DebugItem(int lev, String msg) {
    level = lev;
    timestamp = System.currentTimeMillis();
    message = msg;
  }

  public void add(StringBuffer xml, String clazz) {
    xml.append("<debug>");
    xml.append("<time>");
    xml.append(timestamp);
    xml.append("</time>");
    xml.append("<level>");
    xml.append(level);
    xml.append("</level>");
    xml.append("<class>");
    xml.append(clazz);
    xml.append("</class>");
    xml.append("<message>");
    xml.append(message);
    xml.append("</message>");
    xml.append("</debug>");
  }

  public int getLevel() {
    return level;
  }
}
