package uk.org.hrbc.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Vector;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.filter.Rule;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Location;
import sun.misc.BASE64Encoder;
import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.ICSParseErrorResponse;
import uk.org.hrbc.commands.responses.SqlErrorResponse;
import uk.org.hrbc.commands.responses.SuccessResponse;
import uk.org.hrbc.commands.responses.URLErrorResponse;

public class ZimbraCommand extends CommandImpl {

	private final static long INTERVAL = 10 * 60 * 1000;

	private final static int MIDNIGHT = 24 * 60;

	private HashMap<Long, Double> temps = new HashMap<Long, Double>();

	private class LocTemp {
		String location;
		double temp;
		String zone;

		public LocTemp(String loct, double defaultTemp, String zone) {
			if (loct.contains("_")) {
				String[] s = loct.split("_");
				location = s[0];
				try {
					temp = Double.parseDouble(s[1]);
				} catch (NumberFormatException e) {
					temp = defaultTemp;
				}
			} else {
				location = loct;
				temp = defaultTemp;
			}
			this.zone = zone;
		}

		boolean isEmpty() {
			return location.isEmpty();
		}

		String getZone() {
			return zone;
		}

	}

	private class Required {
		long time;
		double temp;
		boolean occupied;
		long duration;

		@Override
		public String toString() {
			return new SimpleDateFormat().format(new Date(time)) + " for "
					+ duration + ":" + temp;
		}
	}

	private class Heating {
		long now;
		long timeOffset;
		double inside;
		double outside;
		double required;
		double target = Double.NaN;
		boolean heatingOn;
		Heating prev;
		Heating next;
		boolean occupied;

		@Override
		public String toString() {
			String ret = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
					.format(new Date(getTimeMillis()))
					+ "- Occ:"
					+ occupied
					+ ", Heat:";
			ret += heatingOn;
			return ret;
		}

		public void initialise(HeatingSystem sys, Vector<Required> reqs,
				long now, long offset, Heating prev, String zone) {
			this.prev = prev;
			this.now = now;
			if (prev != null) {
				occupied = prev.occupied;
			} else {
				occupied = false; // get occupancy?
			}
			timeOffset = offset;
			long time = now + timeOffset;
			required = Double.NaN;
			for (Required req : reqs) {
				if (time >= req.time + req.duration) {
					occupied = false;
					required = Double.NaN;
				} else if (time >= req.time) {
					occupied = req.occupied;
					required = req.temp;
				} else {
					break;
				}
			}
			heatingOn = false;
			if (timeOffset == 0) {
				// From system
				outside = Double.parseDouble(sys
						.getParam(HeatingSystem.PARAM_OUTSIDE_TEMP));
			} else {
				// From forecast
				outside = sys.getTempForecast(now + timeOffset);
			}
			if (prev != null) {
				inside = sys.getNextTemp(zone, prev.inside, prev.outside,
						prev.heatingOn, INTERVAL);
			} else {
				inside = 15; // From system
			}
			target = required;
			if (occupied) {
				System.out.println("Calculating");
				calculate(sys, zone);
			}
		}

		public Heating addNext(HeatingSystem sys, Vector<Required> reqs,
				long interval, String zone) {
			next = new Heating();
			next.initialise(sys, reqs, now, timeOffset + interval, this, zone);
			return next;
		}

		public boolean calculate(HeatingSystem sys, String zone) {
			if (!Double.isNaN(target)) {
				if (inside < target) {
					if (prev != null && !prev.heatingOn && !prev.occupied) {
						prev.heatingOn = true;
						long hash = (int) (prev.outside * 100) * 10000
								+ (int) (target * 100);
						if (temps.containsKey(hash)) {
							prev.target = temps.get(hash);
						} else {
							prev.target = sys.getTempToReach(zone, target,
									prev.outside, prev.heatingOn, INTERVAL);
							temps.put(hash, prev.target);
						}
						return prev.calculate(sys, zone);
					} else {
						return false;
					}
				} else {
					return true;
				}
			} else {
				return false;
			}
		}

		public long getTimeMillis() {
			return now + timeOffset;
		}

		public int getTime() {
			Calendar time = Calendar.getInstance();
			time.setTimeInMillis(getTimeMillis());
			int hrs = time.get(Calendar.HOUR_OF_DAY);
			int mins = time.get(Calendar.MINUTE);
			return hrs * 60 + mins;
		}

		public int getDay() {
			Calendar time = Calendar.getInstance();
			time.setTimeInMillis(getTimeMillis());
			int day = (time.get(Calendar.DAY_OF_WEEK) - 1);
			if (day == 0) {
				day = 7;
			}
			return day;
		}
	}

	@Override
	public CommandResponse execute(HeatingSystem system, int complete) {
		CommandResponse retResp = null;
		String ret = "<zimbra>";
		String url = "";
		// http://zimbra/home/church/calendar?auth=ba\&fmt=ics\&start=0day\&end=p7day

		String username = getArgument("username");
		String password = getArgument("password");
		InputStream conn = null;
		String sqlArgs = "";
		String sql = "";

		double tempTolerance = 0.5;
		try {
			tempTolerance = Double.parseDouble(system
					.getParam(HeatingSystem.PARAM_TEMPTOL));
		} catch (NumberFormatException ex) {
		}

		if (username != null && !username.isEmpty()) {
			try {
				String[] paramsUrl = new String[] { HeatingSystem.PARAM_ZIMBRA,
						HeatingSystem.PARAM_ZIMBRA2 };
				URLConnection uc = null;
				Vector<VEvent> processedEvents = new Vector<VEvent>();
				Vector<VEvent> allEvents = new Vector<VEvent>();
				HashMap<String, Vector<Required>> reqs = new HashMap<String, Vector<Required>>();

				Calendar now = Calendar.getInstance();
				long millis = System.currentTimeMillis();
				millis = (long) (millis / INTERVAL) * INTERVAL;
				now.setTimeInMillis(millis);
				now.add(Calendar.HOUR, -12);

				CalendarBuilder builder = new CalendarBuilder();

				Calendar now2 = (Calendar) now.clone();
				now2.add(Calendar.DATE, 8);

				// create a period starting now with a duration of one week
				// (8
				// days)..
				Period period = new Period(new DateTime(now.getTime()),
						new DateTime(now2.getTime()));
				Rule[] rules = new Rule[] { new PeriodRule(period) };
				Filter filter = new Filter(rules, Filter.MATCH_ALL);
				now.add(Calendar.HOUR, 12);

				long endTime = period.getEnd().getTime();
				endTime -= 12 * 60 * 60 * 1000; // Take off 12 hrs

				// Process ics files for getting events
				for (String paramUrl : paramsUrl) {
					url = system.getParam(paramUrl);
					if (!url.isEmpty()) {
						if (!url.startsWith("file://")) {
							if (url.contains("?")) {
								url += "&";
							} else {
								url += "?";
							}
							url += "fmt=ics&start=0dat&end=p7day";

							uc = new URL(url).openConnection();

							String userPassword = username + ":" + password;
							String encoding = new BASE64Encoder()
									.encode(userPassword.getBytes());
							uc.setRequestProperty("Authorisation", "Basic "
									+ encoding);
						} else {
							uc = new URL(url).openConnection();
						}
						uc.connect();
						conn = uc.getInputStream();

						byte[] data = new byte[conn.available()];
						conn.read(data);

						StringReader sr = new StringReader(new String(data));
						net.fortuna.ical4j.model.Calendar calendar = builder
								.build(sr);

						Collection<?> events = filter.filter(calendar
								.getComponents(Component.VEVENT));

						for (Object obj : events) {
							if (obj instanceof VEvent) {
								VEvent event = (VEvent) obj;
								if (!allEvents.contains(event)) {
									allEvents.add(event);
								}
								LocTemp loc = null;
								for (String zone : system.getZonesMap()
										.keySet()) {
									String locNames = system.getLocations(zone);
									loc = getLocation(zone,
											system.getDefaultTemp(zone), event,
											locNames);
									if (loc != null) {
										if (!reqs.containsKey(zone)) {
											reqs.put(
													zone,
													new Vector<ZimbraCommand.Required>());
										}
										break;
									}
								}

								if (loc != null && !loc.isEmpty()) {
									if (processedEvents.contains(event)) {
										// Error?, event already processed
										// for
										// loc
									} else {
										processedEvents.add(event);
									}
									PeriodList perl = event
											.calculateRecurrenceSet(period);
									for (Object p : perl) {
										if (p instanceof Period) {
											Period pd = (Period) p;
											System.out.println(pd.getStart()
													+ "->" + pd.getEnd());
											Required req = new Required();
											req.occupied = true;
											req.time = pd.getStart().getTime();
											req.temp = loc.temp;
											Dur dur = pd.getDuration();
											long durM = dur.getDays() * 24 * 60
													* 60 + dur.getHours() * 60
													* 60 + dur.getMinutes()
													* 60 + dur.getSeconds();
											durM *= 1000;
											req.duration = durM;
											reqs.get(loc.getZone()).add(req);
										}
									}
								}
							}
						}
					}
				}

				Vector<String> tempNotReached = new Vector<>();
				for (String zone : reqs.keySet()) {
					if (system.isZone(zone)) {
						ret += "<occupancy>";
						ret += "<zone>" + zone + "</zone>";
						System.out.println("Processing zone :" + zone);

						// Sort the meetings in order...
						for (Vector<Required> req : reqs.values()) {
							Collections.sort(req, new Comparator<Required>() {
								@Override
								public int compare(Required o1, Required o2) {
									return (int) (o1.time - o2.time);
								}
							});
							for (Required r : req) {
								System.out.println(r);
							}
						}

						// Go through the zones, and sort try and reach the
						// required
						// temperature
						Vector<Required> requireds = reqs.get(zone);
						if (requireds != null) {
							Heating current = new Heating();
							Heating first = current;
							current.initialise(system, requireds,
									now.getTimeInMillis(), 0, null, zone);
							while (current.getTimeMillis() < endTime) {
								current = current.addNext(system, requireds,
										INTERVAL, zone);
							}

							Heating start = null;
							current = first;
							while (current != null) {
								// Weeks start on Monday in heating system
								if (current.getDay() == 1
										&& current.getTime() == 0) {
									if (current.prev != null) {
										current.prev.next = null;
									}
									start = current;
									current = current.next;
								} else if (current.next == null) {
									current.next = first;
									current = null;
								} else {
									current = current.next;
								}
								// Failed to reach target temperature
								if (!tempNotReached.contains(zone)
										&& current != null
										&& current.target - tempTolerance > current.inside) {
									tempNotReached.add(zone);
								}
							}

							// Now construct arguments for sending to
							// heating
							// command
							boolean heating = false;
							int day = 0;
							int id = 0;
							HashMap<String, Integer> occs = new HashMap<String, Integer>();
							while (start != null) {
								if (start.getTime() == 0) {
									if (heating) {
										occs.put("occout" + day + id, MIDNIGHT);
									}
									heating = false;
									day++;
									id = 0;
								}
								if (start.heatingOn && !heating) {
									heating = true;
									boolean add = true;
									if (id == 4) {
										int firstInt = occs.get("occin" + day
												+ "1")
												- occs.get("occout" + day + "0");
										int secondInt = occs.get("occin" + day
												+ "2")
												- occs.get("occout" + day + "1");
										int lastInt = occs.get("occin" + day
												+ "2")
												- start.getTime();
										// Last interval smallest, keep last
										// start
										// time
										if (lastInt <= secondInt
												&& lastInt <= firstInt) {
											add = false;
										} else if (firstInt <= secondInt
												&& firstInt <= lastInt)
										// First interval smallest, combine
										// first
										// two intervals and move second up
										{
											occs.put(
													"occout" + day + "0",
													occs.get("occout" + day
															+ "1"));
											occs.put(
													"occin" + day + "1",
													occs.get("occin" + day
															+ "2"));
											occs.put(
													"occout" + day + "1",
													occs.get("occout" + day
															+ "2"));
										} else
										// Second interval smallest, so
										// combine
										// with
										// last
										{
											occs.put(
													"occout" + day + "1",
													occs.get("occout" + day
															+ "2"));
										}
										id = 3;
									}
									if (add) {
										occs.put("occin" + day + id,
												start.getTime());
									}
								} else if (heating && !start.heatingOn) {
									heating = false;
									occs.put("occout" + day + id,
											start.getTime());
									id++;
								}
								start = start.next;
							}

							sqlArgs = "<args><arg id=\"zone\">" + zone
									+ "</arg>";
							for (day = 1; day <= 7; day++) {
								ret += "<day>";
								ret += "<weekday>" + ((day % 7) + 1)
										+ "</weekday>";
								ret += "<times>";
								for (id = 0; id < 3; id++) {
									String idin = "occin" + day + id;
									String idout = "occout" + day + id;
									int valin = MIDNIGHT;
									int valout = MIDNIGHT;
									if (occs.containsKey(idin)) {
										valin = occs.get(idin);
									}
									if (occs.containsKey(idout)) {
										valout = occs.get(idout);
									}
									sqlArgs += "<arg id=\"" + idin + "\">"
											+ valin + "</arg>";
									sqlArgs += "<arg id=\"" + idout + "\">"
											+ valout + "</arg>";
									ret += "<in id=\"occin" + day + id + "\">"
											+ valin + "</in>";
									ret += "<out id=\"occout" + day + id
											+ "\">" + valout + "</out>";
								}
								ret += "</times>";
								ret += "</day>";
							}
							sqlArgs += "</args>";

							// This is not true sql, but instead a traceable
							// error
							// if the pending fails
							sql = "Selecting and Inserting OccupancyCommand into tblPending";
							int pend = system.pendCommand(
									OccupancyCommand.class,
									HeatingSystem.MODE_EDIT, sqlArgs, true);
							if (pend == -1) {
								throw new SQLException();
							}
							ret += "</occupancy>";
						}
					}
				}

				// See which events were not processed because of invalid
				// location
				Vector<String> locs = new Vector<String>();
				if (processedEvents.size() != allEvents.size()) {
					for (Object obj : allEvents) {
						if (!processedEvents.contains(obj)) {
							if (obj instanceof VEvent) {
								VEvent event = (VEvent) obj;
								String loc = getLocation(event);
								if (loc.isEmpty()) {
									loc = "[NO LOCATION SPECIFIED]";
								}
								if (!locs.contains(loc)) {
									locs.add(loc);
								}
							}
						}
					}
				}

				if (locs.size() > 0 || tempNotReached.size() > 0) {
					ret += "<errors>";
					for (String zone : tempNotReached) {
						ret += "<error>Cannot reach temperature in zone "
								+ zone + "</error>";
					}
					for (String loc : locs) {
						ret += "<error>" + loc + "</error>";
					}
					ret += "</errors>";
				}

			} catch (MalformedURLException e) {
				retResp = new URLErrorResponse(url, e.getClass()
						.getSimpleName() + ":" + e.getMessage(),
						getArgumentsXML());
			} catch (IOException e) {
				retResp = new URLErrorResponse(url, e.getClass()
						.getSimpleName() + ":" + e.getMessage(),
						getArgumentsXML());
			} catch (ParserException e) {
				retResp = new ICSParseErrorResponse(url, e.getClass()
						.getSimpleName() + ":" + e.getMessage(),
						getArgumentsXML());
			} catch (SQLException e) {
				retResp = new SqlErrorResponse(sql, e.getMessage(), sqlArgs);
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		if (retResp != null) {
			return retResp;
		} else {
			ret += "</zimbra>";
			return new SuccessResponse(ret, getArgumentsXML());
		}
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_ADMIN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.org.hrbc.commands.Command#getConditions(uk.org.hrbc.HeatingSystem)
	 */
	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription(String mode) {
		return "Zimbra calendar processor";
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
	}

	@Override
	public boolean isPollable() {
		return true;
	}

	private LocTemp getLocation(String zone, double defaultTemp, VEvent event,
			String locNames) {
		Location loc = event.getLocation();
		boolean locOk = false;
		if (loc != null) {
			locOk = loc.getValue().matches(locNames);
		}
		// go through properties to see if matches locations
		if (locOk) {
			return new LocTemp(loc.getValue(), defaultTemp, zone);
		} else {
			PropertyList pl = event.getProperties(Property.RESOURCES);
			for (Object prop : pl) {
				if (prop instanceof Property) {
					Property p = (Property) prop;
					System.out.println(p.getValue());
					if (p.getValue().matches(locNames)) {
						return new LocTemp(p.getValue(), defaultTemp, zone);
					}
				}
			}
		}

		return null;
	}

	private String getLocation(VEvent event) {
		Location loc = event.getLocation();
		if (loc != null) {
			String ret = loc.getName();
			if (!ret.isEmpty()) {
				return ret;
			}
		}
		// go through properties to see if matches locations
		PropertyList pl = event.getProperties(Property.RESOURCES);
		for (Object prop : pl) {
			if (prop instanceof Property) {
				Property p = (Property) prop;
				if (!p.getValue().isEmpty()) {
					return p.getValue();
				}
			}
		}

		return "";
	}

}
