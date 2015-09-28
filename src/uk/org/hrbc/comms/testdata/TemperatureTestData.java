package uk.org.hrbc.comms.testdata;

import java.util.HashMap;

public class TemperatureTestData implements TestData {

  private HashMap<String, Float> temps = new HashMap<String, Float>();
  private String prefix = "";

  @Override
  public String getData() {

    float temp = temps.get(prefix);
    if (prefix.contains("h1")) {
      temp += (Math.random() - 0.5) * 0.03;
    } else {
      if (ValveTestData.heatingOn) {
        temp += Math.random() * 0.03;
      } else {
        temp -= Math.random() * 0.03;
      }
    }
    temps.put(prefix, temp);
    return prefix + " " + Math.floor(temp * 10.0) / 10;
  }

  @Override
  public String getKey() {
    return null;
  }

  @Override
  public void setData(String data) {
    prefix = data.substring(0, 6);
    if (!temps.containsKey(prefix)) {
      if (prefix.contains("h1")) {
        temps.put(prefix, 10.0F);
      } else {
        temps.put(prefix, 20.0F);
      }
    }
    // int ind = data.indexOf("V=");
    // if (ind != -1)
    // temp = Float.parseFloat(data.substring(ind + 2, ind + 6));
  }
}
