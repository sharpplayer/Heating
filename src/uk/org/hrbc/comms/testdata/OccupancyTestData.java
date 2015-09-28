package uk.org.hrbc.comms.testdata;

public class OccupancyTestData implements TestData {

  private String key;
  private String data;

  public OccupancyTestData(String key, String data) {
    this.key = key;
    this.data = data;
  }

  @Override
  public String getData() {
    return data;
  }

  @Override
  public void setData(String data) {
    if (data.length() > 0)
      this.data = data;
  }

  @Override
  public String getKey() {
    return key;
  }
}
