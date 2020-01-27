package uk.org.hrbc.commands;

import java.io.ByteArrayOutputStream;
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
import net.fortuna.ical4j.model.property.Attendee;
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

	private final static long POLL_SET_TEMPERATURE = 10 * 60 * 1000;

	private HashMap<Long, Double> temps = new HashMap<Long, Double>();

	private class LocTemp {
		String location;
		double temp;

		public LocTemp(String loct, double defaultTemp) {
			temp = Double.NaN;
			int index = loct.indexOf(")");
			if (index != -1) {
				int openIndex = loct.indexOf("(");
				if (openIndex + 1 < index) {
					location = loct.substring(0, openIndex).trim();
					try {
						temp = Double.parseDouble(loct.substring(openIndex + 1, index));
					} catch (NumberFormatException e) {
						temp = defaultTemp;
					}
				}
			}

			if (Double.isNaN(temp)) {
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
			}
		}

		boolean isEmpty() {
			return location.isEmpty();
		}
	}

	private class Required {
		long time;
		double temp;
		boolean occupied;
		long duration;

		@Override
		public String toString() {
			return new SimpleDateFormat().format(new Date(time)) + " for " + duration + ":" + temp;
		}

		public int getDay() {
			Calendar calTime = Calendar.getInstance();
			calTime.setTime(new Date(time));
			return calTime.get(Calendar.DAY_OF_MONTH);
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
			String ret = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(getTimeMillis())) + "- Occ:"
					+ occupied + ", Heat:";
			ret += heatingOn;
			return ret;
		}

		public void initialise(HeatingSystem sys, Vector<Required> reqs, long now, long offset, Heating prev,
				String zone, boolean timeDriven, long timeDrivingOffset) {
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
				if (time >= req.time + req.duration + timeDrivingOffset) {
					occupied = false;
					required = Double.NaN;
					heatingOn = true;
				} else if (time >= req.time - timeDrivingOffset) {
					occupied = req.occupied;
					required = req.temp;
					heatingOn = true;
				} else {
					break;
				}
			}

			if (!timeDriven) {
				heatingOn = false;
				if (timeOffset == 0) {
					// From system
					outside = Double.parseDouble(sys.getParam(HeatingSystem.PARAM_OUTSIDE_TEMP));
				} else {
					// From forecast
					outside = sys.getTempForecast(now + timeOffset);
				}
				if (prev != null) {
					if (prev.occupied) {
						inside = prev.inside;
					} else {
						inside = sys.getNextTemp(zone, prev.inside, prev.outside, prev.heatingOn, INTERVAL, false,
								sys.getMinTemperature(zone));
					}
				} else {
					try {
						inside = Double.parseDouble(sys.getParam(HeatingSystem.PARAM_INSIDE_TEMP + zone.toUpperCase()));
					} catch (NumberFormatException ex) {
						inside = 15;
					}
				}
				target = required;
				if (occupied) {
					calculate(sys, zone);
				}
			}
		}

		public Heating addNext(HeatingSystem sys, Vector<Required> reqs, long interval, String zone, boolean timeDriven,
				long timeDrivingOffset) {
			next = new Heating();
			next.initialise(sys, reqs, now, timeOffset + interval, this, zone, timeDriven, timeDrivingOffset);
			return next;
		}

		public boolean calculate(HeatingSystem sys, String zone) {
			if (!Double.isNaN(target)) {
				if (inside < target) {
					if (prev != null && !prev.heatingOn && !prev.occupied) {
						prev.heatingOn = true;
						long hash = (int) (prev.outside * 100) * 10000 + (int) (target * 100);
						if (temps.containsKey(hash)) {
							prev.target = temps.get(hash);
						} else {
							prev.target = sys.getTempToReach(zone, target, prev.outside, prev.heatingOn, INTERVAL);
							temps.put(hash, prev.target);
						}
						inside = target;
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

		boolean isHeatingOn() {
			return heatingOn || occupied;
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
		String actionString = getArgument("action");
		boolean preview = actionString != null && actionString.equalsIgnoreCase("preview");
		InputStream conn = null;
		String sqlArgs = "";
		String sql = "";
		long timeDrivenOffset = 0;
		try {
			timeDrivenOffset = Long.parseLong(system.getParam(HeatingSystem.PARAM_OCCUPANCY_MARGIN));
		} catch (NumberFormatException ex) {
		}

		double tempTolerance = 0.5;
		try {
			tempTolerance = Double.parseDouble(system.getParam(HeatingSystem.PARAM_TEMPTOL));
		} catch (NumberFormatException ex) {
		}

		if (username != null && !username.isEmpty()) {
			try {
				String[] paramsUrl = new String[] { HeatingSystem.PARAM_ZIMBRA, HeatingSystem.PARAM_ZIMBRA2 };
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
				// (7 days)..
				Period period = new Period(new DateTime(now.getTime()), new DateTime(now2.getTime()));
				Rule[] rules = new Rule[] { new PeriodRule(period) };
				Filter filter = new Filter(rules, Filter.MATCH_ALL);
				now.add(Calendar.HOUR, 12);

				long endTime = period.getEnd().getTime();
				endTime -= 12 * 60 * 60 * 1000; // Take off 12 hrs

				// Process ics files for getting events
				for (String paramUrl : paramsUrl) {
					url = system.getParam(paramUrl);
					// if (paramUrl.equals(HeatingSystem.PARAM_ZIMBRA)) {
					// url = "file:///c:/temp/private.ics";
					// } else {
					// url = "file:///c:/temp/public.ics";
					// }
					if (!url.isEmpty()) {
						if (!url.startsWith("file://")) {
							if (!url.trim().toLowerCase().endsWith(".ics")) {
								if (url.contains("?")) {
									url += "&";
								} else {
									url += "?";
								}
								url += "fmt=ics&start=0day&end=p7day";
							}

							uc = new URL(url).openConnection();

							String userPassword = username + ":" + password;
							String encoding = new BASE64Encoder().encode(userPassword.getBytes());
							uc.setRequestProperty("Authorization", "Basic " + encoding);
						} else {
							uc = new URL(url).openConnection();
						}
						uc.connect();
						conn = uc.getInputStream();

						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] chunk = new byte[4096];
						int byteCount;

						while ((byteCount = conn.read(chunk)) > 0) {
							baos.write(chunk, 0, byteCount);
						}

						byte[] data = baos.toByteArray();
						conn.read(data);

						StringReader sr = new StringReader(new String(data));
						net.fortuna.ical4j.model.Calendar calendar = builder.build(sr);

						Collection<?> events = filter.filter(calendar.getComponents(Component.VEVENT));

						for (Object obj : events) {
							if (obj instanceof VEvent) {
								VEvent event = (VEvent) obj;
								if (!allEvents.contains(event)) {
									allEvents.add(event);
								}
								LocTemp loc = null;
								for (String zone : system.getZonesMap().keySet()) {
									String locNames = system.getLocations(zone);
									loc = getLocation(zone, system.getDefaultTemp(zone), event, locNames);
									if (loc != null && !loc.isEmpty()) {
										if (!reqs.containsKey(zone)) {
											reqs.put(zone, new Vector<ZimbraCommand.Required>());
										}

										if (!processedEvents.contains(event)) {
											processedEvents.add(event);
										}
										PeriodList perl = event.calculateRecurrenceSet(period);
										for (Object p : perl) {
											if (p instanceof Period) {
												Period pd = (Period) p;
												Required req = new Required();
												req.occupied = true;
												req.time = pd.getStart().getTime();
												req.temp = loc.temp;
												Dur dur = pd.getDuration();
												long durM = dur.getDays() * 24 * 60 * 60 + dur.getHours() * 60 * 60
														+ dur.getMinutes() * 60 + dur.getSeconds();
												durM *= 1000;
												req.duration = durM;
												reqs.get(zone).add(req);
												System.out.println(event.getName() + " desc:" + event.getDescription()
														+ ", from:" + pd.getStart() + " duration " + dur.toString()
														+ " loc:" + event.getLocation());
											}
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

						// Go through the zones, and sort try and reach the
						// required temperature
						Vector<Required> requireds = reqs.get(zone);
						if (requireds != null) {
							Collections.sort(requireds, new Comparator<Required>() {
								@Override
								public int compare(Required o1, Required o2) {
									return (int) (o1.time - o2.time);
								}
							});

							HashMap<Integer, Vector<Required>> map = new HashMap<>();
							Vector<Required> requiredsClone = new Vector<>(requireds);
							for (Required r : requireds) {
								Vector<Required> list = map.get(r.getDay());
								if (list == null) {
									list = new Vector<>();
									map.put(r.getDay(), list);
								}
								if (list.size() > 1) {
									Required first = list.get(0);
									Required second = list.get(1);
									long firstInterval = second.time - first.time - first.duration;
									long secondInterval = r.time - second.time - second.duration;
									if (firstInterval > secondInterval) {
										// Combine last two slots
										second.duration = r.time - second.time + r.duration;
										second.temp = Math.max(second.temp, r.temp);
									} else {
										// Combine first two slots
										first.duration = second.time - first.time + second.duration;
										first.temp = Math.max(first.temp, second.temp);
										second.time = r.time;
										second.duration = r.duration;
										second.temp = r.temp;
									}
									requiredsClone.remove(r);
								} else {
									list.add(r);
								}
							}

							requireds = requiredsClone;
							Heating current = new Heating();
							Heating first = current;
							boolean isTimeDriven = Boolean.valueOf(system.getZoneProperty(zone, "timeDriven"));
							current.initialise(system, requireds, now.getTimeInMillis(), 0, null, zone, isTimeDriven,
									isTimeDriven ? timeDrivenOffset : 0);
							while (current.getTimeMillis() < endTime) {
								current = current.addNext(system, requireds, INTERVAL, zone, isTimeDriven,
										isTimeDriven ? timeDrivenOffset : 0);
							}

							Heating start = null;
							Heating planStart = null;
							current = first;
							while (current != null) {
								// Weeks start on Monday in heating system
								if (current.getDay() == 1 && current.getTime() == 0) {
									if (current.prev != null) {
										current.prev.next = null;
									}
									planStart = current;
									start = current;
									current = current.next;
								} else if (current.next == null) {
									current.next = first;
									current = null;
								} else {
									current = current.next;
								}
								// Failed to reach target temperature
								if (!tempNotReached.contains(zone) && current != null
										&& current.target - tempTolerance > current.inside) {
									tempNotReached.add(zone);
								}
							}

							// Now construct arguments for sending to
							// heating command
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
								if (start.isHeatingOn() && !heating) {
									heating = true;
									boolean add = true;
									if (id == 4) {
										int firstInt = occs.get("occin" + day + "1") - occs.get("occout" + day + "0");
										int secondInt = occs.get("occin" + day + "2") - occs.get("occout" + day + "1");
										int lastInt = occs.get("occin" + day + "2") - start.getTime();
										// Last interval smallest, keep last
										// start time
										if (lastInt <= secondInt && lastInt <= firstInt) {
											add = false;
										} else if (firstInt <= secondInt && firstInt <= lastInt)
										// First interval smallest, combine
										// first two intervals and move second
										// up
										{
											occs.put("occout" + day + "0", occs.get("occout" + day + "1"));
											occs.put("occin" + day + "1", occs.get("occin" + day + "2"));
											occs.put("occout" + day + "1", occs.get("occout" + day + "2"));
										} else
										// Second interval smallest, so
										// combine with last
										{
											occs.put("occout" + day + "1", occs.get("occout" + day + "2"));
										}
										id = 3;
									}
									if (add) {
										occs.put("occin" + day + id, start.getTime());
									}
								} else if (heating && !start.isHeatingOn()) {
									heating = false;
									occs.put("occout" + day + id, start.getTime());
									id++;
								}
								start = start.next;
							}

							sqlArgs = "<args><arg id=\"zone\">" + zone + "</arg>";
							for (day = 1; day <= 7; day++) {
								ret += "<day>";
								ret += "<weekday>" + ((day % 7) + 1) + "</weekday>";
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
									sqlArgs += "<arg id=\"" + idin + "\">" + valin + "</arg>";
									sqlArgs += "<arg id=\"" + idout + "\">" + valout + "</arg>";
									ret += "<in id=\"occin" + day + id + "\">" + valin + "</in>";
									ret += "<out id=\"occout" + day + id + "\">" + valout + "</out>";
								}
								ret += "</times>";
								ret += "</day>";
							}
							sqlArgs += "</args>";

							// This is not true sql, but instead a traceable
							// error if the pending fails
							if (!preview) {
								sql = "Selecting and Inserting OccupancyCommand into tblPending";
								int pend = system.pendCommand(OccupancyCommand.class, HeatingSystem.MODE_EDIT, sqlArgs,
										true);
								if (pend == -1) {
									conn.close();
									throw new SQLException();
								}
								Required lastRequired = null;

								for (Required r : requireds) {
									if (lastRequired != null) {
										String requiredArgs = "<args><arg id=\"zone\">" + zone
												+ "</arg><arg id=\"required\">" + r.temp + "</arg></args>";

										long commandTime = lastRequired.time + lastRequired.duration;
										system.executeQuery(
												"INSERT INTO tblpoll(GroupId,Frequency,NextPoll,StartTime,EndTime,Active,InXML,Source) VALUES ("
														+ system.getParam(HeatingSystem.PARAM_REQUIRED_CMD) + ","
														+ POLL_SET_TEMPERATURE + "," + commandTime + ",'"
														+ HeatingSystem.SQL_DATE.format(new Date(commandTime)) + "','"
														+ HeatingSystem.SQL_DATE.format(
																new Date(commandTime + POLL_SET_TEMPERATURE))
														+ "',1,'" + requiredArgs + "'," + HeatingSystem.SOURCE_COMMAND
														+ ")");
									}
									lastRequired = r;
								}
							}

							ret += "<plan>";
							start = first;
							do {
								ret += "<s>";
								ret += "<d>" + new SimpleDateFormat().format(new Date(start.now + start.timeOffset))
										+ "</d>";
								ret += "<t>" + (start.now + start.timeOffset) + "</t>";
								ret += "<i>" + round3dp(start.inside) + "</i>";
								ret += "<o>" + round3dp(start.outside) + "</o>";
								ret += "<r>" + (Double.isNaN(start.target) ? "None" : round3dp(start.target)) + "</r>";
								ret += "<h>" + start.isHeatingOn() + "</h>";
								ret += "</s>";
								start = start.next;
								if (start == null) {
									start = planStart;
								}
							} while (start != first);
							ret += "</plan>";

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
								String loc = "";
								if (event.getLocation() != null) {
									loc = event.getLocation().getValue();
								}
								if (loc == null || loc.isEmpty()) {
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
						ret += "<error>Cannot reach temperature in zone " + zone + "</error>";
					}
					for (String loc : locs) {
						ret += "<error>Unknown location \"" + getValidXML(loc, true).replace("'", "") + "\"</error>";
					}
					ret += "</errors>";
				}

			} catch (MalformedURLException e) {
				retResp = new URLErrorResponse(url, e.getClass().getSimpleName() + ":" + e.getMessage(),
						getArgumentsXML());
			} catch (IOException e) {
				retResp = new URLErrorResponse(url, e.getClass().getSimpleName() + ":" + e.getMessage(),
						getArgumentsXML());
			} catch (ParserException e) {
				retResp = new ICSParseErrorResponse(url, e.getClass().getSimpleName() + ":" + e.getMessage(),
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

	private double round3dp(double value) {
		return Math.round(value * 1000) / 1000.0;
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

	private LocTemp getLocation(String zone, double defaultTemp, VEvent event, String locNames) {
		PropertyList pl = event.getProperties(Property.RESOURCES);
		for (Object prop : pl) {
			if (prop instanceof Property) {
				Property p = (Property) prop;
				if (p.getValue().matches(locNames)) {
					return new LocTemp(p.getValue(), defaultTemp);
				}
			}
		}
		PropertyList al = event.getProperties(Property.ATTENDEE);
		for (Object prop : al) {
			if (prop instanceof Attendee) {
				Attendee p = (Attendee) prop;
				if (p.getParameter("CUTYPE") != null) {
					if (p.getParameter("CUTYPE").getValue().equals("RESOURCE")) {
						if (p.getParameter("CN") != null) {
							if (p.getParameter("CN").getValue().matches(locNames)) {
								return new LocTemp(p.getParameter("CN").getValue(), defaultTemp);
							}
						}
					}
				}
			}
		}

		Location loc = event.getLocation();
		// go through properties to see if matches locations
		if (loc != null && loc.getValue().matches(locNames)) {
			return new LocTemp(loc.getValue(), defaultTemp);
		}

		return null;
	}
}
