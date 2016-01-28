package uk.org.hrbc.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;

public class LeadTimesCommand extends CommandImpl {

	private class Limits {
		int minIn = Integer.MAX_VALUE;
		int maxIn = Integer.MIN_VALUE;
		int minOut = Integer.MAX_VALUE;
		int maxOut = Integer.MIN_VALUE;
	}

	private class Means {
		private double sumN = 0.0;
		private double sumN2 = 0.0;
		private int count = 0;
		private long last = 0;
		private double extreme = 0;

		public void addValues(double val, int count, long last) {
			sumN += val * count;
			sumN2 += val * val * count;
			this.count += count;
			if (Math.abs(val) >= Math.abs(extreme)) {
				this.extreme = val;
				this.last = last;
			}
		}

		public double getAverage() {
			return sumN / count;
		}

		public double getSD() {
			double v = sumN2 - sumN * getAverage();
			if (v < 0.0000001) {
				return 0.0;
			} else {
				return Math.sqrt(v);
			}
		}

		public String getLast() {
			return extreme + "(" + HeatingSystem.SQL_DATE.format(new Date(last)) + ")";
		}

		public Date getLastDate() {
			return new Date(last);
		}
	}

	private HashMap<Integer, HashMap<Integer, Means>> coolMeans = new HashMap<Integer, HashMap<Integer, Means>>();
	private HashMap<Integer, HashMap<Integer, Means>> warmMeans = new HashMap<Integer, HashMap<Integer, Means>>();

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {
		return getXmlResponse(system, "leadtimes");
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
		return "List lead times";
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
	}

	@Override
	public boolean isPollable() {
		return false;
	}

	public String getSet() {
		String ret = getArgument("set");
		if (ret != null && ret.isEmpty()) {
			ret = null;
		}
		return ret;
	}

	public int getMeanInGroup(int inTemp) {
		return inTemp;
	}

	public int getMeanOutGroup(int outTemp) {
		return outTemp;
	}

	public CommandResponse getXmlResponse(HeatingSystem system, String tag) {
		StringBuffer xml = new StringBuffer("<" + tag + ">");
		String set = getSet();
		String zone = getArgument("zone");
		if (zone == null)
			zone = system.getParam(HeatingSystem.PARAM_DEFAULTZONE);
		xml.append("<zone>" + zone + "</zone>");
		if (set != null) {
			xml.append("<set>" + set + "</set>");
			String sql = "SELECT * FROM tblleadtimes WHERE DataGroupId=" + set + " AND zone='" + zone + "'";
			try {
				ResultSet rs = system.executeQuery(sql);
				HashMap<Integer, Limits> lims = new HashMap<Integer, Limits>();
				for (int i = LogLeadTimeCommand.STATE_WARMING; i <= LogLeadTimeCommand.STATE_COOLING; i++) {
					lims.put(i, new Limits());
				}
				while (rs.next()) {
					int state = rs.getInt(3);
					Limits l = lims.get(state);
					int inTemp = rs.getInt(4);
					int outTemp = rs.getInt(5);
					if (inTemp < l.minIn) {
						l.minIn = inTemp;
					}
					if (outTemp < l.minOut) {
						l.minOut = outTemp;
					}
					if (inTemp > l.maxIn) {
						l.maxIn = inTemp;
					}
					if (outTemp > l.maxOut) {
						l.maxOut = outTemp;
					}
					HashMap<Integer, HashMap<Integer, Means>> map = null;
					if (state == LogLeadTimeCommand.STATE_COOLING) {
						map = coolMeans;
					} else {
						map = warmMeans;
					}
					HashMap<Integer, Means> means = null;
					if (!map.containsKey(getMeanInGroup(inTemp))) {
						means = new HashMap<Integer, Means>();
						map.put(getMeanInGroup(inTemp), means);
					} else {
						means = map.get(getMeanInGroup(inTemp));
					}
					Means mean = null;
					if (!means.containsKey(getMeanOutGroup(outTemp))) {
						mean = new Means();
						means.put(getMeanOutGroup(outTemp), mean);
					} else {
						mean = means.get(getMeanOutGroup(outTemp));
					}

					double val = (double) (rs.getInt(6)) / 10.0;
					int count = rs.getInt(7);
					// System.out.println("Adding for " + state + ":" + inTemp +
					// "," +
					// outTemp + "," + val + "*" + count);
					mean.addValues(val, count, rs.getTimestamp(8).getTime());
				}
				HashMap<Integer, HashMap<Integer, Means>> map = null;
				for (int state = LogLeadTimeCommand.STATE_WARMING; state <= LogLeadTimeCommand.STATE_COOLING; state++) {
					if (state == LogLeadTimeCommand.STATE_COOLING) {
						map = coolMeans;
						xml.append("<cooling>");
					} else {
						map = warmMeans;
						xml.append("<warming>");
					}

					Limits l = lims.get(state);
					l.minIn = ((int) l.minIn / getStep()) * getStep();
					l.minOut = ((int) l.minOut / getStep()) * getStep();
					l.maxIn = ((int) (l.maxIn / getStep())) * getStep();
					l.maxOut = ((int) (l.maxOut / getStep())) * getStep();
					for (int in = l.minIn; in <= l.maxIn; in += getStep()) {
						xml.append("<i><t>" + (in / 10.0) + "</t><os>");
						for (int out = l.minOut; out <= l.maxOut; out += getStep()) {
							Means m = null;
							if (map.containsKey(in)) {
								if (map.get(in).containsKey(out)) {
									m = map.get(in).get(out);
								}
							}
							xml.append("<o>");
							xml.append("<t>" + (out / 10.0) + "</t>");
							if (m == null) {
								xml.append("<a>-</a>");
								xml.append("<s>-</s>");
								xml.append("<l>-</l>");
							} else {
								sql = getSaveSql(zone, state, in, out, m.getAverage(), m.getLastDate(), true);
								if (!sql.isEmpty() && Math.floor(m.getAverage()) != Math.ceil(m.getAverage())) {
									system.executeQuery(sql);
									sql = getSaveSql(zone, state, in, out, m.getAverage(), m.getLastDate(), false);
									system.executeQuery(sql);
								}
								xml.append("<a>" + m.getAverage() + "</a>");
								xml.append("<s>" + m.getSD() + "</s>");
								xml.append("<l>" + m.getLast() + "</l>");
							}
							xml.append("</o>");
						}
						xml.append("</os></i>");
					}
					if (state == LogLeadTimeCommand.STATE_COOLING) {
						xml.append("</cooling>");
					} else {
						xml.append("</warming>");
					}
				}
			} catch (SQLException e) {
				return new SqlErrorResponse(sql, e.getMessage(), getArgumentsXML());
			}

		}

		xml.append("</" + tag + ">");
		return new SuccessResponse(xml.toString(), getArgumentsXML());

	}

	protected int getStep() {
		return 1;
	}

	protected String getSaveSql(String zone, int state, int in, int out, double value, Date timestamp,
			boolean lowerLimit) {
		// Do nothing to save here so return blank sql
		return "";
	}

}
