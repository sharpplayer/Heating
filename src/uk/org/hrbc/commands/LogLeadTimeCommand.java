package uk.org.hrbc.commands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.BadArgumentResponse;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class LogLeadTimeCommand extends CommandImpl {

	private final static int VALUE_DOESNT_EXIST = -2;
	private final static int STATE_INVALID = -1;
	public final static int STATE_WARMING = 1;
	public final static int STATE_COOLING = 2;

	private final static String TAG_ROOT = "log";

	private final static int ONE_HOUR = 60 * 60 * 1000;

	private class LogData {
		long timestamp;
		int valve = -1;
		int opton = -1;
		int optoff = -1;
		int occ = -1;
		double startInTemp = 0;
		double outTemp = 0;
		Date tsD;
		boolean recorded = false;
		LogData prev;

		LogData(Date time) {
			timestamp = time.getTime();
			tsD = time;
		}

		boolean isInRange(int val) {
			return val == 0 || val == 1;
		}

		int getState() {
			if (opton == 0 && optoff == 0 && occ == 0 && valve == 0) {
				return STATE_COOLING;
			} else if (valve > 0 && valve <= 100) {
				if (isInRange(opton) && isInRange(optoff)) { // && (occ == 0)??
					return STATE_WARMING;
				}
			}
			return STATE_INVALID;
		}

		boolean isValid() {
			return getState() != STATE_INVALID;
		}

		@Override
		public String toString() {
			return (timestamp / 1000) % (3600 * 24) + ":"
					+ ((timestamp / 1000) % (3600 * 24) / 60.0 / 60.0) + ":"
					+ startInTemp + " Out:" + outTemp + " State:" + getState()
					+ " (" + valve + ")";
		}

		boolean isRecorded() {
			return recorded;
		}

		void recorded() {
			this.recorded = true;
		}
	}

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {

		String sql = "SELECT ExecTimestamp, OutXML FROM tblcompleted WHERE Data=1";

		CommandResponse ret = null;
		String xml = "";

		try {

			if (getArgument("startday") != null) {
				Date startDate = getDate(system, "start");
				int interval = Integer.parseInt(system
						.getParam(HeatingSystem.PARAM_LOGGINGINTERVAL));
				int offset = Integer.parseInt(system
						.getParam(HeatingSystem.PARAM_LOGGINGOFFSET));
				if (offset < 0) {
					offset = 0;
				}
				int intervalMultiplier = 0;
				try {
					intervalMultiplier = Integer
							.parseInt(getArgument("intervals"));
				} catch (NumberFormatException ex) {
				}
				Calendar cal = Calendar.getInstance();
				cal.setTime(startDate);
				cal.add(Calendar.SECOND, interval * (offset - 1) / 1000);
				Date fromDate = cal.getTime();
				Date endDate = getDate(system, "end");
				if (intervalMultiplier != 0) {
					cal.add(Calendar.SECOND, interval
							* (intervalMultiplier + offset) / 1000);
					Date toDate = cal.getTime();
					if (toDate.before(endDate)) {
						endDate = toDate;
					}
					offset += intervalMultiplier;
					system.setParam(HeatingSystem.PARAM_LOGGINGOFFSET,
							Integer.toString(offset));
				} else {
					resetOffset(system);
				}

				String andClause = " AND ExecTimestamp >= \""
						+ HeatingSystem.SQL_DATE.format(fromDate) + "\"";
				andClause += " AND ExecTimestamp < \""
						+ HeatingSystem.SQL_DATE.format(endDate) + "\"";
				sql += andClause;
				sql += " ORDER BY Data, ExecTimestamp";
				ResultSet rs = system.executeQuery(sql);

				Date time = null;
				HashMap<String, Vector<LogData>> data = new HashMap<String, Vector<LogData>>();
				HashMap<Date, Double> outSides = new HashMap<Date, Double>();
				for (String zone : system.getZonesMap().keySet()) {
					data.put(zone, new Vector<LogData>());
				}
				while (rs.next()) {
					boolean newRecord = false;
					if (time == null) {
						newRecord = true;
					} else if (time.compareTo(rs.getTimestamp(1)) != 0) {
						newRecord = true;
					}
					if (newRecord) {
						if (!xml.isEmpty()) {
							xml += "</" + TAG_ROOT + ">";
							processXML(system, xml, outSides, time, data);
						}
						xml = "<" + TAG_ROOT + ">";
						time = rs.getTimestamp(1);
					}
					xml += rs.getString(2);
				}
				if (!xml.isEmpty()) {
					xml += "</" + TAG_ROOT + ">";
					processXML(system, xml, outSides, time, data);
				}

				String dataSet = getArgument("set");
				if (dataSet.isEmpty()) {
					resetOffset(system);
					return new BadArgumentResponse(
							"No lead times data set id specified",
							getArgumentsXML());
				}

				for (String zone : data.keySet()) {
					System.out.println("Processing zone:" + zone + ".");
					if (!system.isController(zone)) {
						if (data.containsKey(zone) && data.get(zone).size() > 0) {
							int state = data.get(zone).get(0).getState();
							Vector<LogData> period = new Vector<LogData>();
							for (LogData ld : data.get(zone)) {
								if (ld.getState() == state) {
									if (outSides.containsKey(ld.tsD)) {
										LogData first = ld;
										if (period.size() > 0) {
											first = period.get(0);
										}
										period.add(ld);
										ld.outTemp = outSides.get(ld.tsD);
										if (zone.equals("z1")) {
											System.out.println(ld);
										}
										long deltaT = ld.timestamp
												- first.timestamp;
										if (deltaT >= 2 * ONE_HOUR) {
											period.clear();
											period.add(ld);
											first = ld;
										} else if (deltaT >= ONE_HOUR) {
											double mean = 0;
											double outmean = 0;
											int count = 0;
											Vector<LogData> periodDup = new Vector<LogData>();
											for (LogData ld2 : period) {
												periodDup.add(ld2);
												mean += ld2.startInTemp;
												outmean += ld2.outTemp;
												count++;
											}
											mean /= count;
											outmean /= count;
											double md = 0;
											double outmd = 0;
											for (LogData ld2 : period) {
												md += Math.abs(ld2.startInTemp
														- mean);
												outmd += Math.abs(ld2.outTemp
														- outmean);
											}
											md /= count;
											outmd /= count;
											LogData prev = null;
											period = new Vector<LogData>();
											boolean add = true;
											for (LogData ld2 : periodDup) {
												ld.prev = prev;
												double m1 = ld2.startInTemp
														- mean;
												double m2 = ld2.outTemp
														- outmean;
												if (Math.abs(m1
														- Math.signum(m1) * md) < 2.5
														&& Math.abs(m2
																- Math.signum(m2)
																* outmd) < 2) {
													if (first == null) {
														first = ld2;
														add &= (ld.timestamp
																- first.timestamp >= ONE_HOUR);
													}
													period.add(ld2);
													prev = ld2;
												} else {
													if (ld2 == ld) {
														add = false;
													} else if (ld2 == first) {
														first = null;
													}
												}
											}
											if (add && first != null) {
												addData(system, dataSet, zone,
														state, first, ld);
											}
											while ((period.size() > 0)
													&& ld.timestamp
															- period.get(0).timestamp > ONE_HOUR) {
												period.remove(0);
											}
										}
									}
								} else {
									state = ld.getState();
									period.clear();
								}
							}
						}
					}
				}

				if (fromDate.after(endDate)) {
					ret = new SuccessResponse(
							"<log><result>Completed Data Logging</result></log>",
							getArgumentsXML());
				} else {
					ret = new SuccessResponse(
							"<log><result>Active Data Logging</result></log>",
							getArgumentsXML());
				}
			}
		} catch (ParseException e) {
			ret = new BadArgumentResponse(e.getMessage(), getArgumentsXML());
		} catch (NumberFormatException e) {
			resetOffset(system);
			ret = new BadArgumentResponse(e.getMessage(), getArgumentsXML());
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			resetOffset(system);
			ret = new BadArgumentResponse(e.getMessage(), getArgumentsXML());
		} catch (SQLException e) {
			resetOffset(system);
			ret = new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
		}

		if (ret == null) {
			ret = new SuccessResponse(
					"<log><result>No logging date</result></log>",
					getArgumentsXML());
		} else {
			resetOffset(system);
			ret.setAutoPollStop(true);
		}

		return ret;
	}

	private void addData(HeatingSystem system, String dataSet, String zone,
			int state, LogData first, LogData ld) throws SQLException {
		if (!ld.isRecorded()) {
			int ot = (int) (first.outTemp * 10);
			int it = (int) (first.startInTemp * 10);
			long interpolatedTime = first.timestamp + ONE_HOUR;
			int interpTemp = (int) (Math
					.round(10 * ((ld.startInTemp - ld.prev.startInTemp)
							* (double) (interpolatedTime - ld.prev.timestamp)
							/ (double) (ld.timestamp - ld.prev.timestamp) + ld.prev.startInTemp)));
			int deltaIn = interpTemp - it;
			String sql = "SELECT Count FROM tblleadtimes WHERE DataGroupId = "
					+ dataSet + " AND Zone='" + zone + "' AND State=" + state
					+ " AND InsideTemp=" + it + " AND OutsideTemp=" + ot
					+ " AND DeltaTemp=" + deltaIn;
			ResultSet cnt = system.executeQuery(sql);
			int count = 0;
			if (cnt.next()) {
				count = cnt.getInt(1);
				count++;
				sql = "UPDATE tblleadtimes SET Count=" + count
						+ ",LastDataTimestamp='"
						+ HeatingSystem.SQL_DATE.format(ld.timestamp)
						+ "' WHERE DataGroupId = " + dataSet + " AND Zone='"
						+ zone + "' AND State=" + state + " AND InsideTemp="
						+ it + " AND OutsideTemp=" + ot + " AND DeltaTemp="
						+ deltaIn;
			} else {
				sql = "INSERT INTO tblleadtimes(DataGroupId,Zone,State,InsideTemp,OutsideTemp,DeltaTemp,Count,LastDataTimestamp) VALUES ("
						+ dataSet
						+ ",'"
						+ zone
						+ "',"
						+ state
						+ ","
						+ it
						+ ","
						+ ot
						+ ","
						+ deltaIn
						+ ",1,'"
						+ HeatingSystem.SQL_DATE.format(ld.timestamp) + "')";
			}
			system.executeQuery(sql);
			ld.recorded();
		}
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_ADMIN;
	}

	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription(String mode) {
		return "Generate lead times";
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
	}

	@Override
	public boolean isPollable() {
		return true;
	}

	private void resetOffset(HeatingSystem system) {
		system.setParam(HeatingSystem.PARAM_LOGGINGOFFSET, "-1");
	}

	private int processData(XPath xpath, Document dataItem, String tag,
			String zone, int def, int max) {
		String ret = processData(xpath, dataItem, tag, zone, "");
		try {
			int val = Integer.parseInt(ret);
			if (val < 0 || val > max) {
				return STATE_INVALID;
			} else {
				return val;
			}
		} catch (NumberFormatException ex) {
			if (ret.isEmpty()) {
				return VALUE_DOESNT_EXIST;
			} else {
				return STATE_INVALID;
			}
		}
	}

	private double processDataDouble(XPath xpath, Document dataItem,
			String tag, String zone, double def) {
		String ret = processData(xpath, dataItem, tag, zone, "");
		try {
			return Double.parseDouble(ret);
		} catch (NumberFormatException ex) {
			if (ret.isEmpty()) {
				return VALUE_DOESNT_EXIST;
			} else {
				return STATE_INVALID;
			}
		}
	}

	private String processData(XPath xpath, Document dataItem, String tag,
			String zone, String def) {
		try {
			String s = (String) xpath.evaluate("/" + TAG_ROOT + "/" + tag
					+ "[zone='" + zone + "']/value", dataItem,
					XPathConstants.STRING);
			return s.trim();
		} catch (XPathExpressionException e) {
			return def;
		}

	}

	private void processXML(HeatingSystem system, String xml,
			HashMap<Date, Double> outSides, Date ts,
			HashMap<String, Vector<LogData>> data) {
		try {
			int value;
			double dvalue;

			Document dataItem = system.getXMLFactory().newDocumentBuilder()
					.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

			for (String zone : system.getZonesMap().keySet()) {
				LogData current = new LogData(ts);
				if (system.isController(zone)) {
					if ((dvalue = processDataDouble(system.getXPath(),
							dataItem, TemperatureCommand.TAG_TEMPERATURE, zone,
							STATE_INVALID)) != VALUE_DOESNT_EXIST) {
						outSides.put(ts, dvalue);
					}
				} else {
					if ((value = processData(system.getXPath(), dataItem,
							ValveCommand.TAG_VALVE, zone, STATE_INVALID, 100)) != VALUE_DOESNT_EXIST) {
						current.valve = value;
					}
					if ((value = processData(system.getXPath(), dataItem,
							OptimumStartCommand.TAG_OPT_ON, zone,
							STATE_INVALID, 1)) != VALUE_DOESNT_EXIST) {
						current.opton = value;
					}
					if ((value = processData(system.getXPath(), dataItem,
							OptimumStopCommand.TAG_OPT_OFF, zone,
							STATE_INVALID, 1)) != VALUE_DOESNT_EXIST) {
						current.optoff = value;
					}
					if ((value = processData(system.getXPath(), dataItem,
							OccupiedCommand.TAG_OCCUPIED, zone, STATE_INVALID,
							1)) != VALUE_DOESNT_EXIST) {
						current.occ = value;
					}
					if ((value = processData(system.getXPath(), dataItem,
							OptimumStartCommand.TAG_OPT_ON_DEPRACATED, zone,
							STATE_INVALID, 1)) != VALUE_DOESNT_EXIST) {
						current.opton = value;
					}
					if ((value = processData(system.getXPath(), dataItem,
							OptimumStopCommand.TAG_OPT_OFF_DEPRACATED, zone,
							STATE_INVALID, 1)) != VALUE_DOESNT_EXIST) {
						current.optoff = value;
					}
					if ((dvalue = processDataDouble(system.getXPath(),
							dataItem, TemperatureCommand.TAG_TEMPERATURE, zone,
							STATE_INVALID)) != VALUE_DOESNT_EXIST) {
						current.startInTemp = dvalue;
					}
				}

				if (current.isValid()) {
					data.get(zone).add(current);
				}
			}
		} catch (UnsupportedEncodingException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
		}
	}
}
