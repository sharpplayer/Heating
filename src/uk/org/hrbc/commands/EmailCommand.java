package uk.org.hrbc.commands;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.BadEmailResponse;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.MessagingResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class EmailCommand extends CommandImpl {

  @Override
  public CommandResponse execute(HeatingSystem system, int complete) {

    String addr = "";
    String message = getArgument("message");
    if(message == null)
      message = "No message!";

    Properties props = new Properties();
    props.put("mail.host", system.getParam(HeatingSystem.PARAM_MAILHOST));

    Session mailConn = Session.getInstance(props, null);
    Message msg = new MimeMessage(mailConn);

    try {
      addr = "heating@hrbc.org.uk";
      Address from = new InternetAddress(addr);
      Vector<String> addresses = getArguments("to");
      if (addresses.size() == 0)
        addresses.add(system.getParam(HeatingSystem.PARAM_ALERTEMAIL));
      for (String toAdd : addresses) {
        addr = toAdd;
        Address to = new InternetAddress(addr);
        msg.setContent(message, "text/plain");
        msg.setFrom(from);
        msg.setRecipient(Message.RecipientType.TO, to);
        msg.setSubject("Heating System Email");
        Transport.send(msg);
      }
      return new SuccessResponse("<email><status>Sent</status></email>", getArgumentsXML());
    } catch (AddressException e) {
      return new BadEmailResponse(addr, message, e.getMessage(), getArgumentsXML());
    } catch (MessagingException e) {
      return new MessagingResponse(addr, message, e.getMessage(), getArgumentsXML());
    }
  }

  @Override
  public String getDescription(String mode) {
    return "Send email";
  }

  @Override
  public Vector<String> getModes() {
    return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
  }

  @Override
  public boolean isPollable() {
    return true;
  }

  @Override
  public Hashtable<String, String> getConditions(HeatingSystem system) {
    return null;
  }
  
  @Override
  public int getAccess() {
    return HeatingSystem.ACCESS_ADMIN;
  }

  @Override
  public String getDefaultArgXML(HeatingSystem system) {
    return "<arg id=\"message\">No message!</arg>";
  }
}
