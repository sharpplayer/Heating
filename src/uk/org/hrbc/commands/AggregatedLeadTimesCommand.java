package uk.org.hrbc.commands;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;

public class AggregatedLeadTimesCommand extends LeadTimesCommand {

	private String defaultAggregateSet;

	private String aggregateSet;

	@Override
	public boolean isPollable() {
		return false;
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT, HeatingSystem.MODE_EDIT));
	}

	@Override
	public String getDescription(String mode) {
		if (mode.equals(HeatingSystem.MODE_DEFAULT)) {
			return "Get aggregate lead times";
		} else {
			return "Set aggregate lead times";
		}
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
	public String getSet() {
		return aggregateSet;
	}

	@Override
	public int getMeanInGroup(int inTemp) {
		return (int) (10 * Math.floor(inTemp / 10.0));
	}

	@Override
	public int getMeanOutGroup(int outTemp) {
		return (int) (10 * Math.ceil(outTemp / 10.0));
	}

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {
		String reset = getArgument("reset");
		String view = getArgument("view");
		String zone = getArgument("zone");
		defaultAggregateSet = system.getParam(HeatingSystem.PARAM_AGGREGATED_DATASET);
		if (zone == null) {
			zone = system.getParam(HeatingSystem.PARAM_DEFAULTZONE);
		}

		String sql = "";
		try {
			aggregateSet = defaultAggregateSet;
			if (view == null) {
				if (reset != null) {
					if (!reset.isEmpty()) {
						sql = "DELETE FROM tblleadtimes WHERE DataGroupId=" + defaultAggregateSet + " AND Zone='" + zone
								+ "'";
						system.executeQuery(sql);
						aggregateSet = reset;
					} else {
						aggregateSet = "Save";
						String[] warmCold = { "w", "c" };
						for (String state : warmCold) {
							int numState = state == "w" ? LogLeadTimeCommand.STATE_WARMING
									: LogLeadTimeCommand.STATE_COOLING;
							Vector<String> temps = getArguments("agg" + state + "temp");
							int in = (int) (Double.parseDouble(getArgument("agg" + state + "minin")) * 10);
							int out = (int) (Double.parseDouble(getArgument("agg" + state + "minout")) * 10);
							int outs = Integer.parseInt(getArgument("agg" + state + "cols"));
							int count = outs;
							for (String temp : temps) {
								if (temp != null) {
									double value = Double.NaN;
									try {
										value = Double.parseDouble(temp);
									} catch (NumberFormatException ex) {
									}
									if (temp.isEmpty() || !Double.isNaN(value)) {
										sql = "DELETE FROM tblleadtimes WHERE DataGroupId=" + defaultAggregateSet
												+ " AND Zone='" + zone + "' AND State="
												+ LogLeadTimeCommand.STATE_WARMING + " AND InsideTemp=" + in
												+ " AND OutsideTemp=" + out;
										system.executeQuery(sql);
									}

									if (!Double.isNaN(value)) {
										sql = getSaveSql(zone, numState, in, out, value, new Date(), true);
										system.executeQuery(sql);
										if (Math.ceil(value) != Math.floor(value)) {
											sql = getSaveSql(zone, numState, in, out, value, new Date(), false);
											system.executeQuery(sql);
										}
									}
								}
								out += getStep();
								count--;
								if (count == 0) {
									count = outs;
									in += getStep();
									out = (int) (Double.parseDouble(getArgument("agg" + state + "minout")) * 10);
								}
							}
						}
						aggregateSet = defaultAggregateSet;
					}
				}
			}
			return getXmlResponse(system, "aggregates");
		} catch (

		SQLException ex)

		{
			return new SqlErrorResponse(sql, ex.getMessage(), getArgumentsXML());
		}

	}

	@Override
	protected int getStep() {
		return 10;
	}

	@Override
	protected String getSaveSql(String zone, int state, int in, int out, double value, Date timestamp,
			boolean lowerLimit) {
		String sql = "";
		if (!aggregateSet.equals(defaultAggregateSet)) {
			double insertValue = (Math.floor(value) + (lowerLimit ? 0 : 1)) * 10;
			double count = (value - Math.floor(value)) * 100000;
			if (lowerLimit) {
				count = 100000 - count;
			}
			sql = "INSERT INTO tblleadtimes(DataGroupId,Zone,State,InsideTemp,OutsideTemp,DeltaTemp,Count,LastDataTimestamp) VALUES ("
					+ defaultAggregateSet + ",'" + zone + "'," + state + "," + in + "," + out + "," + insertValue + ","
					+ count + ",'" + HeatingSystem.SQL_DATE.format(timestamp) + "')";

		}
		System.out.println(sql);
		return sql;
	}
}
