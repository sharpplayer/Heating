package uk.org.hrbc.commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;
import uk.org.hrbc.commands.responses.URLErrorResponse;
import uk.org.hrbc.commands.responses.XMLErrorResponse;

public class GetWeatherCommand extends CommandImpl {

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {
		String xpath = "SiteRep/DV/Location/Period";
		String ret = "<weather>";
		Calendar cal = Calendar.getInstance();
		String fromweb = getArgument("fromweb");
		if (fromweb == null) {
			fromweb = "";
		}
		InputStream conn;
		String xml = system.getParam(HeatingSystem.PARAM_WEATHER);
		String url = system.getParam(HeatingSystem.PARAM_METURL);
		try {
			if (fromweb.equalsIgnoreCase("true")) {
				conn = new URL(url).openStream();
				// conn = new ByteArrayInputStream(
				// Files.readAllBytes(Paths
				// .get("C:\\Users\\Raymond\\workspace\\HRBC\\Weather.xml")));
			} else {
				return new SuccessResponse(xml, getArgumentsXML());
			}
			Document forecast = system.getXMLFactory().newDocumentBuilder()
					.parse(conn);
			NodeList nl = (NodeList) system.getXPath().evaluate(xpath,
					forecast, XPathConstants.NODESET);
			HashMap<Integer, String> dayData = new HashMap<Integer, String>();
			HashMap<Integer, String> day1440Data = new HashMap<Integer, String>();
			for (int node = 0; node < nl.getLength(); node++) {
				Node n = nl.item(node);
				NamedNodeMap atts = n.getAttributes();
				String day = atts.getNamedItem("value").getNodeValue();
				NodeList reps = n.getChildNodes();
				for (int rep = 0; rep < reps.getLength(); rep++) {
					Node r = reps.item(rep);
					String time = r.getTextContent().trim();
					if (time.length() > 0) {
						day = day.substring(0, 10);
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						cal.setTime(df.parse(day));
						int dayNo = cal.get(Calendar.DAY_OF_WEEK);
						if (!dayData.containsKey(dayNo)) {
							dayData.put(dayNo, "");
						}
						String temp = r.getAttributes().getNamedItem("T")
								.getNodeValue();
						int t = -1;
						try {
							t = Integer.parseInt(time);
						} catch (NumberFormatException ex) {
						}
						if (t == 0) {
							day1440Data.put(dayNo,
									"<fc><time>1440</time><temp>" + temp
											+ "</temp></fc>");
						}
						dayData.put(dayNo, dayData.get(dayNo) + "<fc><time>"
								+ time + "</time><temp>" + temp
								+ "</temp></fc>");
					}
				}
			}
			for (int dayNo = 1; dayNo <= 7; dayNo++) {
				int d = dayNo;
				while (dayData.get(d) == null) {
					d = d - 1;
					if (d < 1) {
						d = 7;
					}
				}
				if (d != dayNo) {
					dayData.put(dayNo, dayData.get(d));
					day1440Data.put(dayNo, day1440Data.get(d));
				}
			}

			for (int dayNo = 1; dayNo <= 7; dayNo++) {
				ret += "<fcs><day>" + dayNo + "</day>";
				ret += dayData.get(dayNo);
				String data = day1440Data.get(dayNo + 1 > 7 ? 1 : dayNo + 1);
				if (data != null) {
					ret += data;
				}
				ret += "</fcs>";
			}
			ret += "</weather>";
		} catch (MalformedURLException e) {
			return new URLErrorResponse(url, e.getClass().getSimpleName() + ":"
					+ e.getMessage(), getArgumentsXML());
		} catch (IOException e) {
			return new URLErrorResponse(url, e.getClass().getSimpleName() + ":"
					+ e.getMessage(), getArgumentsXML());
		} catch (SAXException e) {
			return new XMLErrorResponse(xml, e.getMessage(), getArgumentsXML());
		} catch (ParseException e) {
			return new XMLErrorResponse(xml, e.getMessage(), getArgumentsXML());
		} catch (ParserConfigurationException e) {
			return new XMLErrorResponse(xml, e.getMessage(), getArgumentsXML());
		} catch (XPathExpressionException e) {
			return new XMLErrorResponse(xpath, e.getMessage(),
					getArgumentsXML());
		}

		system.setParam(HeatingSystem.PARAM_WEATHER, ret);

		return new SuccessResponse(ret, getArgumentsXML());
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_ADMIN;
	}

	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		return null;
	}

	@Override
	public String getDescription(String mode) {
		return "Get weather forecast";
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
	}

	@Override
	public boolean isPollable() {
		return true;
	}

}
