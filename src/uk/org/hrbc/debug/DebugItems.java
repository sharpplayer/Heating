package uk.org.hrbc.debug;

import java.util.Vector;

public class DebugItems {

  private String clazz = "";
  private Vector<DebugItem> items = new Vector<DebugItem>();

  public DebugItems(Object clazz) {
    this.clazz = clazz.getClass().getCanonicalName();
  }

  public void add(DebugItem di) {
    items.add(di);
  }

  public String getXML(int maxLevel) {
    StringBuffer xml = new StringBuffer();
    for (DebugItem di : items)
      if (di.getLevel() <= maxLevel)
        di.add(xml, clazz);
    return xml.toString();
  }

  public boolean hasDebug(int maxLevel) {
    for (DebugItem di : items)
      if (di.getLevel() <= maxLevel)
        return true;
    return false;
  }
}
