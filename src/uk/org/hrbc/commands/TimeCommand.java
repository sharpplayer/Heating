package uk.org.hrbc.commands;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.RetryFailedResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;
import uk.org.hrbc.commands.responses.TimeoutResponse;
import uk.org.hrbc.commands.responses.WriteErrorResponse;
import uk.org.hrbc.comms.exceptions.CommsResendException;
import uk.org.hrbc.comms.exceptions.CommsTimeoutException;

public class TimeCommand extends CommandImpl {

  private SimpleDateFormat df = new SimpleDateFormat(" HH mm");
  private SimpleDateFormat dfSend = new SimpleDateFormat("'T'('H'=HH,'N'=mm,'W'='wd','D'=d,'M'=M,'Y'=yy)");
  private SimpleDateFormat dfShortSend = new SimpleDateFormat("'D'=d,'M'=M,'Y'=yy");

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {

    String day = getArgument("day");
    String hour = getArgument("hour");
    String min = getArgument("min");
    int dayno = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

    int retry = 3;
    try {
      retry = Integer.parseInt(system.getParam(HeatingSystem.PARAM_RETRIES));
    } catch (NumberFormatException ex) {
    }

    if (day != null) {
      String message;
      if (hour.equalsIgnoreCase("system")) {
        message = dfSend.format(new Date());
      } else {
        message = "T(H=" + hour + ",N=" + min + ",W=wd," + dfShortSend.format(new Date()) + ")";
      }
      if (day.equalsIgnoreCase("system")) {
        if (dayno == 1) {
          day = "7";
        } else {
          day = Integer.toString(dayno - 1);
        }
      } else {
        int d = Integer.parseInt(day);
        d--;
        if (d == 0) {
          d = 7;
        }
        day = Integer.toString(d);
      }
      message = message.replace("wd", day);
      try {
        system.sendMessage("u00", message, true);
      } catch (IOException e) {
        return new WriteErrorResponse(e.getMessage(), getArgumentsXML());
      }
    }

    while (retry > 0) {
      StringBuffer xml = new StringBuffer("<time>");
      try {
        system.sendMessage("uAT", "T(H,N,W,D,M,Y,p)", true);
        String data = system.receiveMessage("uAT");
        String x = data.substring(8).trim();
        System.out.println("Day:" + x);
        int d = 1 + (Integer.parseInt(data.substring(8).trim()) % 7);
        addTimeXML(xml, "remote", data.substring(1, 4).trim(), data.substring(4, 7).trim(), d);
        data = df.format(new Date());
        addTimeXML(xml, "local", data.substring(0, 3).trim(), data.substring(3, 6).trim(), dayno);
        xml.append("</time>");
        return new SuccessResponse(xml.toString(), getArgumentsXML());
      } catch (IOException e) {
        return new WriteErrorResponse(e.getMessage(), getArgumentsXML());
      } catch (CommsTimeoutException e) {
        return new TimeoutResponse(getArgumentsXML());
      } catch (CommsResendException e) {
        retry--;
      }
    }
    return new RetryFailedResponse(getArgumentsXML());

  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    return null;
  }

  @Override
  public String getDescription(String mode) {
    if (mode.equalsIgnoreCase(HeatingSystem.MODE_EDIT))
      return "Set heating system time";
    else
      return "Get system times";
  }

  @Override
  public Vector<String> getModes() {
    return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT, HeatingSystem.MODE_EDIT));
  }

  @Override
  public boolean isPollable() {
    return true;
  }

  private void addTimeXML(StringBuffer xml, String tag, String hr, String min, int day) {
    xml.append("<" + tag + ">");
    xml.append("<weekday>");
    xml.append(day);
    xml.append("</weekday>");
    xml.append("<hour>");
    xml.append(hr);
    xml.append("</hour>");
    xml.append("<minute>");
    xml.append(min);
    xml.append("</minute>");
    xml.append("</" + tag + ">");
  }

  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_ADMIN;
  }

  @Override
  public String getDefaultArgXML(HeatingSystem system) {
    return "<arg id=\"status\">status</arg>";
  }
}
