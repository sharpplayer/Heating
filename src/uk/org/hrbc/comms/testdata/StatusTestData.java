package uk.org.hrbc.comms.testdata;

public class StatusTestData implements TestData {

  // private boolean state = true;
  private String prefix = "";

  @Override
  public String getData() {
    boolean state = ValveTestData.heatingOn;
    if (state && !prefix.contains("I2"))
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
