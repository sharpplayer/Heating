package uk.org.hrbc.comms.testdata;

public class ValveTestData implements TestData {

  static public boolean heatingOn = true;
  public long heatingChanged = System.currentTimeMillis();
  private String prefix = "";

  @Override
  public String getData() {
    float valve = 0;
    if (System.currentTimeMillis() - heatingChanged > 10800000) {
      heatingOn = !heatingOn;
      heatingChanged = System.currentTimeMillis();
    }
    if (heatingOn) {
      valve = 50.5f;
    }
    return prefix + " " + valve;
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
