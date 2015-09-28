package uk.org.hrbc.comms.testdata;

public class BoilerStatusTestData implements TestData {

  private int state = 3;
  private String prefix = "";

  @Override
  public String getData() {
    state++;
    state %= 10;
    if (state > 2)
      return prefix + " 0";
    else
      return prefix + " 1";
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
