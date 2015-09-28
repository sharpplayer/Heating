package uk.org.hrbc.comms.testdata;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeTestData implements TestData {

  private long time = System.currentTimeMillis();
  private SimpleDateFormat df = new SimpleDateFormat("'T' HH mm");
  private int dow = 0;
  private Calendar cal = Calendar.getInstance();

  public TimeTestData() {

    dow = cal.get(Calendar.DAY_OF_WEEK);
    dow--;
    if (dow == 0)
      dow = 7;
    time = cal.getTimeInMillis();

    Thread t = new Thread() {
      @Override
      public void run() {
        while (true) {
          cal.setTime(new Date(time));
          int day = cal.get(Calendar.DAY_OF_WEEK);
          time += 1000;
          cal.setTime(new Date(time));
          if (day != cal.get(Calendar.DAY_OF_WEEK)) {
            dow = dow + 1;
            if (dow == 8)
              dow = 1;
          }
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
          }
        }
      }
    };
    t.start();
  }

  @Override
  public String getData() {
    Date d = new Date(time);
    String ret = df.format(d) + "  " + dow;
    return ret;
  }

  @Override
  public String getKey() {
    return null;
  }

  @Override
  public void setData(String data) {
    if (data.contains("=")) {
      String date = getValueAfter(data, "D");
      date += "/" + getValueAfter(data, "M");
      date += "/" + getValueAfter(data, "Y");
      date += " " + getValueAfter(data, "H");
      date += ":" + getValueAfter(data, "N");

      dow = Integer.parseInt(getValueAfter(data, "W"));

      try {
        time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).parse(date).getTime();
      } catch (ParseException e) {
        time = System.currentTimeMillis();
      }
    }
  }

  private String getValueAfter(String data, String token) {
    int ind = data.indexOf(token + "=") + 2;
    String ret = data.substring(ind);
    ind = ret.indexOf(",");
    if (ind == -1)
      ind = ret.indexOf(")");
    ret = ret.substring(0, ind);
    return ret;
  }

}
