package uk.org.hrbc.commands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.BadArgumentResponse;
import uk.org.hrbc.commands.responses.CommandResponse;

public abstract class CommandImpl implements Command {

	private String args;
	private XPath xpath;
	private Document argDoc;

	@Override
	public final CommandResponse execute(HeatingSystem system, String args, int complete) {

		this.args = args;
		if (args.equals("<args />") || args.equals("<args></args>") || args.isEmpty()) {
			String defArg = getDefaultArgXML(system);
			if (defArg.length() > 0) {
				this.args = "<args>" + defArg + "</args>";
			} else {
				this.args = "<args />";
			}
		}
		try {
			argDoc = system.getXMLFactory().newDocumentBuilder()
					.parse(new ByteArrayInputStream(this.args.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			return new BadArgumentResponse(e.getMessage(), args);
		} catch (SAXException e) {
			return new BadArgumentResponse(e.getMessage(), args);
		} catch (IOException e) {
			return new BadArgumentResponse(e.getMessage(), args);
		} catch (ParserConfigurationException e) {
			return new BadArgumentResponse(e.getMessage(), args);
		}

		xpath = system.getXPath();
		return execute(system, complete);
	}

	private Vector<String> getArgumentsById(String arg) {
		Vector<String> ret = new Vector<String>();
		try {
			NodeList list = (NodeList) xpath.evaluate("/args/arg[@id='" + arg + "']", argDoc, XPathConstants.NODESET);
			for (int node = 0; node < list.getLength(); node++)
				ret.add(list.item(node).getTextContent());
		} catch (XPathExpressionException e) {
		}
		return ret;
	}

	public Vector<String> getArguments(String arg) {
		Vector<String> args = getArgumentsById(arg);
		if (args.size() == 0) {
			int count = 0;
			try {
				Object value = xpath.evaluate("count(/args/arg[starts-with(@id, '" + arg + "')])", argDoc,
						XPathConstants.NUMBER);
				count = ((Number) value).intValue();
			} catch (XPathExpressionException e) {
			}
			for (int loop = 1; loop < Integer.MAX_VALUE && count > 0; loop++) {
				String a = getArgument(arg + loop);
				if (a != null) {
					args.add(a);
					count--;
				} else {
					args.add(null);
				}
			}
		}
		return args;
	}

	@Override
	public String getArgumentsXML() {
		return args;
	}

	public String getArgument(String arg) {
		Vector<String> values = getArgumentsById(arg);
		if (values.size() == 1)
			return values.get(0);
		else
			return null;
	}

	@Override
	public String getDefaultArgXML(HeatingSystem system) {
		return "";
	}

	public abstract CommandResponse execute(HeatingSystem system, int complete);

	protected Date getDate(HeatingSystem system, String datePrefix) throws ParseException {
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MM yyyy");
		String start = "";
		if ((getArgument(datePrefix + "day").length() > 0) && (getArgument(datePrefix + "month").length() > 0)
				&& (getArgument(datePrefix + "year").length() > 0)) {
			start = getArgument(datePrefix + "day") + " " + getArgument(datePrefix + "month") + " "
					+ getArgument(datePrefix + "year");
			return DATE_FORMAT.parse(start);
		} else
			return null;

	}

	protected String getInterval(HeatingSystem system, String datePrefix, Date def) {
		String ret = "<" + datePrefix + ">";
		Calendar cal = Calendar.getInstance();
		cal.setTime(def);
		ret += getDatePart("day", getArgument(datePrefix + "day"), cal.get(Calendar.DAY_OF_MONTH));
		ret += getDatePart("month", getArgument(datePrefix + "month"), cal.get(Calendar.MONTH) + 1);
		ret += getDatePart("year", getArgument(datePrefix + "year"), cal.get(Calendar.YEAR));
		ret += "</" + datePrefix + ">";
		return ret;
	}

	protected String getDatePart(String tag, String val, int def) {
		return "<" + tag + ">" + (val == null ? def : val) + "</" + tag + ">";

	}

	public String getValidXML(String xml) {
		return xml.replaceAll("^\\u0009\\u000a\\u000d\\u0020-\\uD7FF\\uE000-\\uFFFD]", "");
	}

}
