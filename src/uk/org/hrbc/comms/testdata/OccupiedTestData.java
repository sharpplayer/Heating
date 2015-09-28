package uk.org.hrbc.comms.testdata;

public class OccupiedTestData implements TestData {

  private boolean occupied = true;
  private String prefix = "";

  @Override
  public String getData() {
    occupied = !occupied;
    if (occupied)
      return prefix + " 1";
    else
      return prefix + " 0";
  }

  @Override
  public String getKey() {
    return null;
  }

  @Override
  public void setData(String data) {
    prefix = data.substring(0, 6);
  }

}
