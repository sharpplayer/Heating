package uk.org.hrbc.conditions;

import java.util.Map.Entry;

public class Condition implements Entry<String, String> {

  String value = "";
  String key = "";

  public Condition(String key, String value) {
    this.value = value;
    this.key = key;
  }

  @Override
  public String setValue(String value) {
    this.value = value;
    return this.value;
  }

  @Override
  public String getKey() {
    return this.key;
  }

  @Override
  public String getValue() {
    return this.value;
  }

}
