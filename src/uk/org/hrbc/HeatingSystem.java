package uk.org.hrbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.org.hrbc.commands.ActualRequiredTemperatureCommand;
import uk.org.hrbc.commands.AggregatedLeadTimesCommand;
import uk.org.hrbc.commands.AlertCommand;
import uk.org.hrbc.commands.AlertsCommand;
import uk.org.hrbc.commands.Command;
import uk.org.hrbc.commands.CommandCommand;
import uk.org.hrbc.commands.CommandsCommand;
import uk.org.hrbc.commands.ConditionCommand;
import uk.org.hrbc.commands.ConditionsCommand;
import uk.org.hrbc.commands.ConfigurationCommand;
import uk.org.hrbc.commands.DebugCommand;
import uk.org.hrbc.commands.EmailCommand;
import uk.org.hrbc.commands.FlowTemperatureCommand;
import uk.org.hrbc.commands.GetWeatherCommand;
import uk.org.hrbc.commands.HistoryCommand;
import uk.org.hrbc.commands.HouseKeepingCommand;
import uk.org.hrbc.commands.BoilerStateCommand;
import uk.org.hrbc.commands.LeadTimesCommand;
import uk.org.hrbc.commands.LogLeadTimeCommand;
import uk.org.hrbc.commands.OccupancyCommand;
import uk.org.hrbc.commands.OccupiedCommand;
import uk.org.hrbc.commands.OptimumStartCommand;
import uk.org.hrbc.commands.OptimumStopCommand;
import uk.org.hrbc.commands.PollCommand;
import uk.org.hrbc.commands.PollsCommand;
import uk.org.hrbc.commands.ReportCommand;
import uk.org.hrbc.commands.RequiredTemperatureCommand;
import uk.org.hrbc.commands.SystemCommand;
import uk.org.hrbc.commands.TemperatureCommand;
import uk.org.hrbc.commands.TimeCommand;
import uk.org.hrbc.commands.ValveCommand;
import uk.org.hrbc.commands.ZimbraCommand;
import uk.org.hrbc.commands.responses.ClassErrorResponse;
import uk.org.hrbc.commands.responses.CommandResponse;
import uk.org.hrbc.commands.responses.CompleteWhenPossibleResponse;
import uk.org.hrbc.commands.responses.GeneralExceptionResponse;
import uk.org.hrbc.commands.responses.NullResponseResponse;
import uk.org.hrbc.commands.responses.ReExecuteResponse;
import uk.org.hrbc.comms.CommsPort;
import uk.org.hrbc.comms.exceptions.CommsResendException;
import uk.org.hrbc.comms.exceptions.CommsTimeoutException;
import uk.org.hrbc.conditions.Condition;
import uk.org.hrbc.debug.DebugItem;
import uk.org.hrbc.debug.DebugItems;

public class HeatingSystem {

	public final static SimpleDateFormat SQL_DATE = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public final static String PARAM_MAILHOST = "MAILHOST";
	public final static String PARAM_DEBUG = "DEBUG";
	public final static String PARAM_POLLING = "POLLING";
	public final static String PARAM_ZONES = "ZONES";
	public final static String PARAM_HOME = "HOME";
	public final static String PARAM_POLLEDIT = "POLLEDIT";
	public final static String PARAM_TIMEOUT = "TIMEOUT";
	public final static String PARAM_COMM_TIMEOUT = "COMMTO";
	public final static String PARAM_PORT = "COMMSPORT";
	public final static String PARAM_IP = "COMMSIP";
	public final static String PARAM_DEFAULTZONE = "DEFZONE";
	public final static String PARAM_DEFAULTCONT = "DEFCONT";
	public final static String PARAM_DEFAULTSUBCONT = "DEFSUBCONT";
	public final static String PARAM_ALERTEDIT = "ALERTEDIT";
	public final static String PARAM_CONDEDIT = "CONDEDIT";
	public final static String PARAM_CMDEDIT = "CMDEDIT";
	public final static String PARAM_CONFIG = "CONFEDIT";
	public final static String PARAM_REALERT = "REALERT";
	public final static String PARAM_SYSTEMTAG = "SYSTEMTAG";
	public final static String PARAM_ALERTEMAIL = "ALERTEMAIL";
	public final static String PARAM_HOUSEKEEPING_NON_DATA = "HKNONDATA";
	public final static String PARAM_HOUSEKEEPING_DATA = "HKDATA";
	public final static String PARAM_DEBUG_CMD = "CMDDEBUG";
	public final static String PARAM_REPORTS_CMD = "REPORTS";
	public final static String PARAM_RETRIES = "RETRIES";
	public final static String PARAM_LOGGINGOFFSET = "LOGGINGOFFSET";
	public final static String PARAM_LOGGINGINTERVAL = "LOGGINGINTERVAL";
	public final static String PARAM_METURL = "METURL";
	public final static String PARAM_WEATHER = "WEATHER";
	public final static String PARAM_ZIMBRA = "ZIMBRA";
	public final static String PARAM_ZIMBRA2 = "ZIMBRA2";
	public final static String PARAM_ACTIVE_DATASET = "ADATASET";
	public final static String PARAM_OUTSIDE_TEMP = "OUTTEMP";
	public final static String PARAM_TEMPTOL = "TEMPTOL";
	public final static String PARAM_AGGREGATED_DATASET = "AGGDATASET";

	public final static String MODE_DEFAULT = "default";
	public final static String MODE_EDIT = "edit";

	public final static int COMPLETE_WHEN_POSSIBLE = 0;
	public final static int COMPLETED = 1;
	public final static int COMPLETE_INSTANTLY = 2;
	public final static int COMPLETED_BUT_REEXECUTE = 3;

	public final static int SOURCE_WEB = 0;
	public final static int SOURCE_POLL = 1;
	public final static int SOURCE_ALERT = 2;
	public final static int SOURCE_COMMAND_LINE = 3;
	public final static int SOURCE_COMMAND = 4;

	public final static int TYPE_NO_EDIT = 0;
	public final static int TYPE_USER = 1;

	public final static int ACCESS_ADMIN = 1;
	public final static int ACCESS_NORMAL = 2;

	private final static int MAX_RESPONSE_SIZE = 32768;

	private Thread threadInstant = null;
	private Thread threadWait = null;
	private Connection con = null;
	private boolean exit = false;
	private DocumentBuilderFactory xmlFactory = null;
	private CommsPort comms = null;
	private String command = "";
	private HashMap<String, String> params = null;
	private DebugItems debugItems;
	private XPath xpath = null;

	public HeatingSystem(String[] args) {

		boolean start = false;
		debugItems = new DebugItems(this);

		if (args.length > 0) {
			for (int arg = 0; arg < args.length; arg++) {
				if (args[arg].equalsIgnoreCase("-s")) {
					start = true;
					break;
				} else if (args[arg].equalsIgnoreCase("-c")) {
					String command = "";
					String arguments = "";
					if (args.length > arg + 1) {
						command = args[arg + 1];
						if (command.indexOf("Command") == -1)
							command += "Command";
						if (command.indexOf(".") == -1)
							command = "uk.org.hrbc.commands." + command;
					}
					if (args.length > arg + 2) {
						arguments = args[arg + 2];
					}
					CommandResponse cr = executeCommand(command,
							arguments.replace("'", "\""), COMPLETE_INSTANTLY);
					System.out.println(cr.getMessage());
					break;
				} else if (args[arg].equalsIgnoreCase("-t")) {
					if (isServerRunning())
						System.exit(1);
					else
						System.exit(0);
					break;
				}
			}
		} else {
			System.out
					.println("Usage: HeatingSystem [-s | -c command [args] | -t]");
			System.out
					.println("       -s                Start as a background application");
			System.out
					.println("       -c command [args] Run command with optional XML arguments");
			System.out
					.println("       -t                Test if the Heating System monitor is running");
		}

		if (start) {
			if (!isServerRunning()) {
				// This thread is so the UI gets
				// an instant response and dooesn't
				// get held up by heating system polling
				threadInstant = new Thread() {
					@Override
					public void run() {
						HeatingSystem.this.run(COMPLETE_INSTANTLY);
					}
				};
				threadInstant.start();

				// Handles non-UI commands
				threadWait = new Thread() {
					@Override
					public void run() {
						HeatingSystem.this.run(COMPLETE_WHEN_POSSIBLE);
					}
				};
				threadWait.start();
			} else
				System.out.println("Heating instance already running");
		}
	}

	public static void main(String[] args) {
		new HeatingSystem(args);
	}

	public String sendMessage(String key, String message, boolean lf)
			throws IOException {
		return getComms().sendMessage(key, message, lf);
	}

	public String sendMessageEx(String key, String message, boolean lf)
			throws IOException {
		return getComms().sendMessageEx(key, message, lf);
	}

	public String sendMessage(String message, boolean lf) throws IOException {
		return getComms().sendMessage(message, lf, command);
	}

	public String receiveMessage(String key) throws CommsTimeoutException,
			CommsResendException {
		return getComms().receiveMessage(key);
	}

	public void stop() {
		exit = true;
	}

	public void reset() {
		if (comms != null)
			comms.closeConnection();
		comms = null;
		params = null;
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		Statement query = getConnection().createStatement();
		if (sql.startsWith("SELECT"))
			return query.executeQuery(sql);
		else if (sql.startsWith("INSERT")) {
			query.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			return query.getGeneratedKeys();
		} else {
			query.executeUpdate(sql);
			return null;
		}
	}

	public void run(int completeState) {

		ResultSet rs;
		CommandResponse response;
		Statement query1;
		Statement query2;
		int pending;
		String xml;
		int groupId;
		int source;
		long poll = 0;

		try {
			query1 = getConnection().createStatement();
			query2 = getConnection().createStatement();

			boolean alert = false;
			while (!exit || alert) {
				alert = false;
				try {
					Date execTime = new Date();
					HashMap<Integer, Integer> polls = new HashMap<Integer, Integer>();
					if (completeState == COMPLETE_WHEN_POSSIBLE) {
						rs = query1
								.executeQuery("SELECT PollId, tblpoll.GroupId, Frequency, InXML, NextPoll, EndTime FROM tblpoll WHERE NextPoll < "
										+ System.currentTimeMillis()
										+ " AND Active = 1");
						while (rs.next()) {
							int cmd = pendCommand(query2, rs.getInt(2),
									SOURCE_POLL, rs.getString(4), false);
							polls.put(cmd, rs.getInt(1));
							long nextPoll = rs.getLong(5) + rs.getLong(3);
							if (nextPoll < System.currentTimeMillis()) {
								long diff = System.currentTimeMillis()
										- nextPoll;
								diff = ((long) (diff / rs.getLong(3)) + 1)
										* rs.getLong(3);
								nextPoll += diff;
							}

							String extraSql = "";
							if (rs.getDate(6) != null)
								if (rs.getDate(6).before(new Date(nextPoll)))
									extraSql = ", Active=0";
							query2.executeUpdate("UPDATE tblpoll SET NextPoll="
									+ nextPoll + extraSql + " WHERE PollId="
									+ rs.getInt(1));
						}
					}
					String where = "WHERE Completed=" + completeState;
					if (completeState == COMPLETE_WHEN_POSSIBLE)
						where += " OR Completed=" + COMPLETED_BUT_REEXECUTE;

					rs = query1
							.executeQuery("SELECT PendingId, tblcommand.CommandId, tblpending.InXML, InTimestamp, Source, Class, Mode, tblcommandgroupx.GroupId, Completed FROM tblpending LEFT JOIN tblcommandgroupx ON tblpending.GroupId=tblcommandgroupx.GroupId INNER JOIN tblcommand on tblcommand.CommandId=tblcommandgroupx.CommandId "
									+ where
									+ " ORDER BY PendingId, tblcommandgroupx.GroupId, tblcommandgroupx.Order");
					xml = "";
					pending = -1;
					groupId = -1;
					source = -1;
					String debug = "";
					int level;
					try {
						level = Integer.parseInt(getParam(PARAM_DEBUG));
					} catch (NumberFormatException ex) {
						level = DebugItem.NONE;
					}

					int toComplete = COMPLETED;
					while (rs.next()) {
						if (pending != rs.getInt(1)) {
							if (pending != -1)
								alert |= completeCommand(query2, pending,
										groupId, xml, execTime, source, debug,
										toComplete);
							pending = rs.getInt(1);
							groupId = rs.getInt(8);
							source = rs.getInt(5);
							debug = "";
						}
						response = executeCommand(rs.getString(6),
								rs.getString(3), completeState);

						if (response == null)
							response = new NullResponseResponse(
									rs.getString(6), rs.getString(3));

						if (response.isAutoPollStop()) {
							if (polls.containsKey(pending)) {
								executeQuery("UPDATE tblpoll SET Active=0 WHERE PollId="
										+ polls.get(pending));
							}
						}

						if (response instanceof CompleteWhenPossibleResponse)
							toComplete = COMPLETE_WHEN_POSSIBLE;
						else if (response instanceof ReExecuteResponse)
							toComplete = COMPLETED_BUT_REEXECUTE;
						else if (rs.getInt(9) == COMPLETED_BUT_REEXECUTE) {
							if (response.getStatus() == CommandResponse.SUCCESS)
								toComplete = COMPLETED;
							else
								toComplete = COMPLETED_BUT_REEXECUTE;
						} else
							toComplete = COMPLETED;

						if (toComplete != COMPLETE_WHEN_POSSIBLE) {
							if (response.getDebug() != null) {
								if (response.getDebug().hasDebug(level)) {
									debug = response.getDebug().getXML(level);
								}
							}
							if (comms != null) {
								DebugItems dis = getComms().getDebug();
								if (dis.hasDebug(level)) {
									debug += dis.getXML(level);
								}
							}
							if (debugItems.hasDebug(level)) {
								debug += debugItems.getXML(level);
								debugItems = new DebugItems(this);
							}
							xml += response.getMessage();
							writeMessage(query2, response.getMessage(),
									response.getStatus(), rs.getInt(1),
									rs.getInt(2), response.getArgs(),
									rs.getTimestamp(4), execTime, rs.getInt(5),
									rs.getString(7), response.isData(),
									rs.getInt(9) == COMPLETED_BUT_REEXECUTE,
									"Thread " + Integer.toString(completeState));
						}
					}
					if (pending != -1)
						alert |= completeCommand(query2, pending, groupId, xml,
								execTime, source, debug, toComplete);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

				try {
					try {
						poll = Long.parseLong(getParam(PARAM_POLLING));
					} catch (NumberFormatException e) {
						poll = 1000;
					}
					Thread.sleep(poll);
				} catch (InterruptedException e) {
					exit = true;
				}
			}
		} catch (SQLException e2) {
			e2.printStackTrace();
		}

		if (comms != null)
			getComms().closeConnection();
		System.exit(0);
	}

	public String getTimeXML(Date date, String tag) {
		String ret = "<" + tag + ">";
		if (date == null)
			ret += "<timestamp>0</timestamp>";
		else {
			Calendar time = Calendar.getInstance();
			time.setTime(date);
			ret += "<timestamp>" + time.getTimeInMillis() + "</timestamp>";
			ret += "<yearday>" + time.get(Calendar.DAY_OF_YEAR) + "</yearday>";
			ret += "<year>" + time.get(Calendar.YEAR) + "</year>";
			ret += "<month>" + (time.get(Calendar.MONTH) + 1) + "</month>";
			ret += "<day>" + time.get(Calendar.DAY_OF_MONTH) + "</day>";
			ret += "<weekday>" + time.get(Calendar.DAY_OF_WEEK) + "</weekday>";
			ret += "<hour>" + time.get(Calendar.HOUR_OF_DAY) + "</hour>";
			ret += "<minute>" + time.get(Calendar.MINUTE) + "</minute>";
			ret += "<time>"
					+ String.format("%1$02d%1$02d",
							time.get(Calendar.HOUR_OF_DAY),
							time.get(Calendar.MINUTE)) + "</time>";
			ret += "<second>" + time.get(Calendar.SECOND) + "</second>";
			ret += "<millisecond>" + time.get(Calendar.MILLISECOND)
					+ "</millisecond>";
		}
		ret += "</" + tag + ">";
		return ret;
	}

	public String getParam(String param) {
		if (params == null) {
			registerParams();
		}
		if (params.containsKey(param))
			return params.get(param);
		return "";
	}

	private CommandResponse executeCommand(String command, String argXml,
			int completeState) {
		CommandResponse response = null;
		Class<?> clazz;
		Command cmd = null;

		try {
			this.command = command;
			clazz = Class.forName(command);
			cmd = (Command) clazz.newInstance();
			response = cmd.execute(this, argXml, completeState);
		} catch (ClassNotFoundException e) {
			response = new ClassErrorResponse(command, e.getMessage());
		} catch (InstantiationException e) {
			response = new ClassErrorResponse(command, e.getMessage());
		} catch (IllegalAccessException e) {
			response = new ClassErrorResponse(command, e.getMessage());
		} catch (Exception e) {
			if (cmd == null)
				response = new GeneralExceptionResponse(command,
						e.getMessage(), argXml);
			else
				response = new GeneralExceptionResponse(command,
						e.getMessage(), cmd.getArgumentsXML());
		}
		return response;
	}

	private Connection getConnection() throws SQLException {
		if (con == null) {
			con = DriverManager.getConnection("jdbc:mysql:///heating",
					"heating", "toohottoocold");
			registerParams();
			registerCommands();
			registerGlobalConditions();
		}
		return con;
	}

	public DocumentBuilderFactory getXMLFactory() {
		if (xmlFactory == null)
			xmlFactory = DocumentBuilderFactory.newInstance();
		return xmlFactory;
	}

	private CommsPort getComms() {
		if (comms == null)
			comms = new CommsPort(this);
		return comms;
	}

	public int pendCommand(Class<? extends Command> cmd, String mode,
			String args, boolean key) throws SQLException {
		Statement query = getConnection().createStatement();
		ResultSet rs = query
				.executeQuery("SELECT CommandId FROM tblcommand WHERE Class='"
						+ cmd.getCanonicalName() + "'");
		if (rs.next()) {
			rs = query
					.executeQuery("SELECT GroupId FROM tblcommandgroupx WHERE CommandId="
							+ rs.getInt(1) + " AND Mode='" + mode + "'");
			if (rs.next()) {
				return pendCommand(query, rs.getInt(1), SOURCE_COMMAND, args,
						key);
			}
		}
		return -1;
	}

	private int pendCommand(Statement query, int cmdGroup, int src,
			String args, boolean key) throws SQLException {
		String sql = "INSERT INTO tblpending(GroupId, InTimestamp, Source, InXML, Completed) VALUES("
				+ cmdGroup
				+ ",\'"
				+ SQL_DATE.format(new Date())
				+ "\',"
				+ src
				+ ",\'" + args + "\', 0)";
		if (key) {
			ResultSet id;
			query.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			id = query.getGeneratedKeys();
			if (id.next())
				return id.getInt(1);
			else
				return -1;
		} else {
			query.executeUpdate(sql);
			return -1;
		}
	}

	private void registerCommands() {
		try {
			ArrayList<Command> commands = new ArrayList<Command>();
			Vector<String> classes = new Vector<String>();
			classes.add(ActualRequiredTemperatureCommand.class
					.getCanonicalName());
			classes.add(AggregatedLeadTimesCommand.class.getCanonicalName());
			classes.add(AlertCommand.class.getCanonicalName());
			classes.add(AlertsCommand.class.getCanonicalName());
			classes.add(BoilerStateCommand.class.getCanonicalName());
			classes.add(CommandCommand.class.getCanonicalName());
			classes.add(CommandsCommand.class.getCanonicalName());
			classes.add(ConditionCommand.class.getCanonicalName());
			classes.add(ConditionsCommand.class.getCanonicalName());
			classes.add(ConfigurationCommand.class.getCanonicalName());
			classes.add(DebugCommand.class.getCanonicalName());
			classes.add(EmailCommand.class.getCanonicalName());
			classes.add(FlowTemperatureCommand.class.getCanonicalName());
			classes.add(GetWeatherCommand.class.getCanonicalName());
			classes.add(HistoryCommand.class.getCanonicalName());
			classes.add(HouseKeepingCommand.class.getCanonicalName());
			classes.add(LeadTimesCommand.class.getCanonicalName());
			classes.add(LogLeadTimeCommand.class.getCanonicalName());
			classes.add(OccupancyCommand.class.getCanonicalName());
			classes.add(OccupiedCommand.class.getCanonicalName());
			classes.add(OptimumStartCommand.class.getCanonicalName());
			classes.add(OptimumStopCommand.class.getCanonicalName());
			classes.add(PollCommand.class.getCanonicalName());
			classes.add(PollsCommand.class.getCanonicalName());
			classes.add(ReportCommand.class.getCanonicalName());
			classes.add(RequiredTemperatureCommand.class.getCanonicalName());
			classes.add(SystemCommand.class.getCanonicalName());
			classes.add(TemperatureCommand.class.getCanonicalName());
			classes.add(TimeCommand.class.getCanonicalName());
			classes.add(ValveCommand.class.getCanonicalName());
			classes.add(ZimbraCommand.class.getCanonicalName());

			ResultSet rs;
			Statement query = getConnection().createStatement();
			Statement query2 = getConnection().createStatement();
			Statement query3 = getConnection().createStatement();
			Statement query4 = getConnection().createStatement();
			ResultSet key;
			int grpId;

			for (String clazz : classes) {
				Class<?> c = Class.forName(clazz);
				if (!Modifier.isAbstract(c.getModifiers())
						& !Modifier.isInterface(c.getModifiers())
						& Command.class.isAssignableFrom(c)) {
					Command cmd = (Command) c.newInstance();
					commands.add(cmd);
				}
			}

			for (Command cmd : commands) {
				rs = query
						.executeQuery("SELECT CommandId FROM tblcommand WHERE Class='"
								+ cmd.getClass().getCanonicalName() + "'");
				if (!rs.next()) {
					rs.close();
					query.executeUpdate(
							"INSERT INTO tblcommand(Class, Pollable) VALUES('"
									+ cmd.getClass().getCanonicalName() + "',"
									+ cmd.isPollable() + ")",
							Statement.RETURN_GENERATED_KEYS);
					key = query.getGeneratedKeys();
					key.next();
				} else
					key = rs;

				for (String mode : cmd.getModes()) {
					grpId = -1;
					rs = query2
							.executeQuery("SELECT GroupId FROM tblcommandgroupx WHERE CommandId="
									+ key.getInt(1)
									+ " AND Mode='"
									+ mode
									+ "'");
					if (!rs.next()) {
						query2.executeUpdate(
								"INSERT INTO tblcommandgroup(Description, Pollable, Type, Access) VALUES('"
										+ cmd.getDescription(mode)
										+ "',"
										+ ((cmd.isPollable() & mode
												.equalsIgnoreCase(MODE_DEFAULT)) ? 1
												: 0) + "," + TYPE_NO_EDIT + ","
										+ cmd.getAccess() + ")",
								Statement.RETURN_GENERATED_KEYS);
						ResultSet grp = query2.getGeneratedKeys();
						if (grp.next()) {
							grpId = grp.getInt(1);
							query3.executeUpdate(
									"INSERT INTO tblcommandgroupx(GroupId, `Order`, CommandId, Mode) VALUES("
											+ grpId + ",1," + key.getInt(1)
											+ ",'" + mode + "')",
									Statement.RETURN_GENERATED_KEYS);
						}
					} else
						grpId = rs.getInt(1);

					if (grpId != -1) {
						if ((cmd instanceof CommandsCommand)
								&& (mode.equalsIgnoreCase(MODE_DEFAULT)))
							registerParam(query3, PARAM_HOME,
									Integer.toString(grpId), true, "Home",
									false);
						else if ((cmd instanceof PollCommand)
								&& (mode.equalsIgnoreCase(MODE_EDIT)))
							registerParam(query3, PARAM_POLLEDIT,
									Integer.toString(grpId), true,
									"Poll editor command", false);
						else if ((cmd instanceof AlertCommand)
								&& (mode.equalsIgnoreCase(MODE_EDIT)))
							registerParam(query3, PARAM_ALERTEDIT,
									Integer.toString(grpId), true,
									"Alert editor command", false);
						else if ((cmd instanceof ConditionCommand)
								&& (mode.equalsIgnoreCase(MODE_EDIT)))
							registerParam(query3, PARAM_CONDEDIT,
									Integer.toString(grpId), true,
									"Command editor command", false);
						else if ((cmd instanceof CommandCommand)
								&& (mode.equalsIgnoreCase(MODE_EDIT)))
							registerParam(query3, PARAM_CMDEDIT,
									Integer.toString(grpId), true,
									"Condition editor command", false);
						else if ((cmd instanceof ConfigurationCommand)
								&& (mode.equalsIgnoreCase(MODE_EDIT)))
							registerParam(query3, PARAM_CONFIG,
									Integer.toString(grpId), true,
									"Configuration editor command", false);
						else if ((cmd instanceof DebugCommand))
							registerParam(query3, PARAM_DEBUG_CMD,
									Integer.toString(grpId), true,
									"Debug output command", false);
						else if ((cmd instanceof ReportCommand))
							registerParam(query3, PARAM_REPORTS_CMD,
									Integer.toString(grpId), true,
									"Report command", false);
					}
				}
				Hashtable<String, String> conds = cmd.getConditions(this);
				if (conds != null) {
					for (Entry<String, String> cond : conds.entrySet()) {
						registerCondition(cond, query4);
					}
				}
				key.close();
			}
			return;
			// } catch (IOException e) {
			// e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void registerParams() {
		try {
			params = new HashMap<String, String>();
			Statement query = getConnection().createStatement();
			registerParam(query, PARAM_MAILHOST, "localhost", false,
					"Mail server host name", true);
			registerParam(query, PARAM_DEBUG, "0", false, "Debug level", true);
			registerParam(query, PARAM_POLLING, "1000", false,
					"Polling interval in milliseconds", true);
			registerParam(query, PARAM_TIMEOUT, "10000", false,
					"Web interface timeout in milliseconds", true);
			registerParam(query, PARAM_COMM_TIMEOUT, "1000", false,
					"Comms timeout in milliseconds", true);
			registerParam(
					query,
					PARAM_REALERT,
					"3600000",
					false,
					"Interval after which an alert can be triggered again in milliseconds",
					true);
			registerParam(query, PARAM_ALERTEMAIL, "heatingalert@hrbc.org.uk",
					false, "Recipient of heating system alert emails", true);
			registerParam(query, PARAM_PORT, "/dev/ttyS0", false,
					"Serial port name or ethernet port number", true);
			registerParam(query, PARAM_IP, "192.168.124.160", false,
					"Ethernet IP address of heating controller", true);
			registerParam(query, PARAM_SYSTEMTAG, "heating", false,
					"Root XML for data", false);

			String zoneStr;
			try {
				zoneStr = new String(
						Files.readAllBytes(Paths.get("zones.xml")),
						Charset.defaultCharset());
				zoneStr = zoneStr.trim().replace("\n", "");
				zoneStr = zoneStr.replace("\t", "");
				zoneStr = zoneStr.replace("\r", "");
				registerParam(query, PARAM_ZONES, zoneStr, true,
						"Zones description", false);
			} catch (IOException e) {
				e.printStackTrace();
			}
			registerParam(query, PARAM_DEFAULTZONE, "z1", false,
					"Default zone", true);
			registerParam(query, PARAM_DEFAULTCONT, "h1", false,
					"Default controller", true);
			registerParam(query, PARAM_DEFAULTSUBCONT, "h2", false,
					"Default subcontroller", true);
			registerParam(query, PARAM_RETRIES, "3", false,
					"Comms retry attempts", true);
			registerParam(query, PARAM_LOGGINGOFFSET, "-1", false,
					"Data offset for logging", false);
			registerParam(
					query,
					PARAM_LOGGINGINTERVAL,
					"3600000",
					false,
					"Current lead time logging time sample interval in milliseconds",
					true);
			registerParam(
					query,
					PARAM_HOUSEKEEPING_NON_DATA,
					"86400000",
					false,
					"Amount of time of non-data history to keep in milliseconds",
					true);
			registerParam(query, PARAM_HOUSEKEEPING_DATA, "864000000000",
					false,
					"Amount of time of data history to keep in milliseconds",
					true);
			registerParam(
					query,
					PARAM_METURL,
					"http://datapoint.metoffice.gov.uk/public/data/val/wxfcs/all/xml/310091?res=3hourly&key=a400136c-cf6d-492b-86cf-714e142c5494",
					false, "Met Office URL for weather forecasts", true);
			registerParam(query, PARAM_WEATHER, "<weather />", false,
					"Abridged weather data", false);
			registerParam(query, PARAM_ZIMBRA,
					"http://zimbra/home/church/calendar?auth=ba&fmt=ics",
					false, "Zimbra URL", true);
			registerParam(query, PARAM_ZIMBRA2,
					"http://zimbra/home/office/calendar?auth=ba&fmt=ics",
					false, "Zimbra 2nd URL", true);
			registerParam(query, PARAM_ACTIVE_DATASET, "1", false,
					"Active Dataset Number", true);
			registerParam(query, PARAM_OUTSIDE_TEMP, "0", false,
					"Current Outside Temp", false);
			registerParam(query, PARAM_TEMPTOL, "0.5", false,
					"Tolerance below required temperature to trigger error",
					true);
			registerParam(query, PARAM_AGGREGATED_DATASET, "100000001", false,
					"Data set number for aggregated data", true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getLocations(String zone) {
		Document zonesList;
		try {
			String z = getParam(PARAM_ZONES);
			zonesList = getXMLFactory().newDocumentBuilder().parse(
					new ByteArrayInputStream(z.getBytes("UTF-8")));
			String ins = (String) getXPath().evaluate(
					"/zones/zone[name='" + zone + "']/locations", zonesList,
					XPathConstants.STRING);
			if (ins != null) {
				return ins;
			}
		} catch (UnsupportedEncodingException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
		} catch (XPathExpressionException e) {
		}

		return "";
	}

	public double getDefaultTemp(String zone) {
		try {
			return Double.parseDouble(getZoneProperty(zone, "defaultTemp"));
		} catch (NumberFormatException ex) {
			return 19.0;
		}
	}

	public void setParam(String name, String value) {
		try {
			executeQuery("UPDATE tblconfig SET Value='" + value
					+ "' WHERE Param='" + name + "'");
			params.put(name, value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void registerParam(Statement query, String name, String value,
			boolean force, String desc, boolean modifiable) throws SQLException {
		ResultSet rs = query
				.executeQuery("SELECT ConfigId,Value FROM tblconfig WHERE Param='"
						+ name + "'");
		if (!rs.next()) {
			rs.close();
			query.executeUpdate("INSERT INTO tblconfig(Param, Value, Description, Modifiable) VALUES('"
					+ name
					+ "','"
					+ value
					+ "','"
					+ desc
					+ "',"
					+ (modifiable ? "1" : "0") + ")");
			params.put(name, value);
		} else if (force) {
			rs.close();
			query.executeUpdate("UPDATE tblconfig SET Value='" + value
					+ "' WHERE Param='" + name + "'");
			params.put(name, value);
		} else {
			params.put(name, rs.getString(2));
		}
	}

	private boolean completeCommand(Statement query, int pending, int groupId,
			String response, Date execTime, int source, String debug,
			int toComplete) throws SQLException {
		ResultSet alerts;
		int alertId = -1;
		boolean raiseAlert = true;
		int alertCommand = -1;
		String alertArgs = "<args />";

		if (toComplete == COMPLETED) {
			alerts = query
					.executeQuery("SELECT AlertId, XPath, AlertCommandGroupId, InXML, LastExec FROM tblalert INNER JOIN tblconditiongroupx ON tblalert.ConditionGroupId=tblconditiongroupx.ConditionGroupId INNER JOIN tblcondition ON tblconditiongroupx.ConditionId=tblcondition.ConditionId WHERE GroupId="
							+ groupId
							+ " AND Source="
							+ source
							+ " ORDER BY AlertId");
			while (alerts.next()) {
				try {
					if (alertId != alerts.getInt(1)) {
						if ((alertId != -1) && raiseAlert) {
							pendCommand(query, alertCommand, SOURCE_ALERT,
									alertArgs, false);
							updateAlert(alertId);
						}
						alertId = alerts.getInt(1);
						raiseAlert = true;
						alertCommand = alerts.getInt(3);
						alertArgs = alerts.getString(4);
					}
					if (alerts.getLong(5)
							+ Long.parseLong(getParam(PARAM_REALERT)) <= System
								.currentTimeMillis()) {
						String xml = ("<" + getParam(PARAM_SYSTEMTAG) + ">"
								+ getTimeXML(execTime, "timestamp") + response
								+ "</" + getParam(PARAM_SYSTEMTAG) + ">");
						Document resp = getXMLFactory().newDocumentBuilder()
								.parse(new ByteArrayInputStream(xml
										.getBytes("UTF-8")));
						String cond = alerts.getString(2);
						boolean nl = (Boolean) getXPath().evaluate(cond, resp,
								XPathConstants.BOOLEAN);
						if (!nl)
							raiseAlert = false;
					} else
						raiseAlert = false;
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			}
			if ((alertId != -1) && raiseAlert) {
				pendCommand(query, alertCommand, SOURCE_ALERT, alertArgs, false);
				updateAlert(alertId);
			} else
				raiseAlert = false;
		}
		query.executeUpdate("UPDATE tblpending SET Completed=" + toComplete
				+ " WHERE PendingId=" + pending);
		if (toComplete == COMPLETED && debug.length() > 0) {
			writeMessage(query, "<debugs>" + debug + "</debugs>",
					CommandResponse.SUCCESS, pending,
					Integer.parseInt(getParam(PARAM_DEBUG_CMD)), "<args />",
					new Date(), execTime, source, MODE_DEFAULT, false, false,
					"debug");
		}
		return raiseAlert;
	}

	private void updateAlert(int alertId) throws SQLException {
		Statement update = getConnection().createStatement();
		update.executeUpdate("UPDATE tblalert SET LastExec="
				+ System.currentTimeMillis() + " WHERE AlertId=" + alertId);
	}

	public String getSourcesXML() {
		String xml = "<sources>";
		xml += "<source><id>" + SOURCE_COMMAND_LINE
				+ "</id><description>Command Line</description></source>";
		xml += "<source><id>" + SOURCE_WEB
				+ "</id><description>Web Interface</description></source>";
		xml += "<source><id>" + SOURCE_POLL
				+ "</id><description>Poll</description></source>";
		xml += "<source><id>" + SOURCE_ALERT
				+ "</id><description>Alert</description></source>";
		xml += "<source><id>" + SOURCE_COMMAND
				+ "</id><description>Another Command</description></source>";
		xml += "</sources>";
		return xml;
	}

	public boolean isZone(String zone) {
		return "Zone".equalsIgnoreCase(getZoneProperty(zone, "type"));
	}

	public boolean isController(String zone) {
		return "Controller".equalsIgnoreCase(getZoneProperty(zone, "type"));
	}

	public String getController(String zone) {
		String con = getZoneProperty(zone, "controller");
		if (con.length() == 0)
			con = zone;
		return con;
	}

	public String getZoneFromAlias(String alias) {
		try {
			Document zonesList = getXMLFactory().newDocumentBuilder().parse(
					new ByteArrayInputStream(getParam(PARAM_ZONES).getBytes(
							"UTF-8")));
			String ins = (String) getXPath().evaluate(
					"/zones/zone/aliases[alias='" + alias + "']/../name",
					zonesList, XPathConstants.STRING);
			return ins;
		} catch (UnsupportedEncodingException e) {
			return "";
		} catch (SAXException e) {
			return "";
		} catch (IOException e) {
			return "";
		} catch (ParserConfigurationException e) {
			return "";
		} catch (XPathExpressionException e) {
			return "";
		}
	}

	public int getZoneIndex(String zone) {
		int ins = -1;
		try {
			Document zonesList = getXMLFactory().newDocumentBuilder().parse(
					new ByteArrayInputStream(getParam(PARAM_ZONES).getBytes(
							"UTF-8")));
			ins = Integer.parseInt((String) getXPath().evaluate(
					"count(/zones/zone[name='" + zone
							+ "']/preceding-sibling::*)", zonesList,
					XPathConstants.STRING));
		} catch (SAXException | IOException | ParserConfigurationException
				| NumberFormatException | XPathExpressionException e) {
			e.printStackTrace();
		}
		return ins + 1;

	}

	public String getZoneProperty(String zone, String prop) {
		try {
			Document zonesList = getXMLFactory().newDocumentBuilder().parse(
					new ByteArrayInputStream(getParam(PARAM_ZONES).getBytes(
							"UTF-8")));
			String ins = (String) getXPath().evaluate(
					"/zones/zone[name='" + zone + "']/" + prop, zonesList,
					XPathConstants.STRING);
			return ins;
		} catch (UnsupportedEncodingException e) {
			return "";
		} catch (SAXException e) {
			return "";
		} catch (IOException e) {
			return "";
		} catch (ParserConfigurationException e) {
			return "";
		} catch (XPathExpressionException e) {
			return "";
		}
	}

	public HashMap<String, String> getZonesMap() {
		HashMap<String, String> zones = new HashMap<String, String>();
		try {
			Document zonesList = getXMLFactory().newDocumentBuilder().parse(
					new ByteArrayInputStream(getParam(PARAM_ZONES).getBytes(
							"UTF-8")));
			NodeList list = (NodeList) getXPath().evaluate("/zones/zone",
					zonesList, XPathConstants.NODESET);
			for (int node = 0; node < list.getLength(); node++) {
				String zone = (String) getXPath().evaluate("description",
						list.item(node), XPathConstants.STRING);
				String id = (String) getXPath().evaluate("name",
						list.item(node), XPathConstants.STRING);
				zones.put(id, zone);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return zones;
	}

	private void registerCondition(Entry<String, String> cond, Statement query)
			throws SQLException {
		ResultSet rs = query
				.executeQuery("SELECT ConditionId FROM tblcondition WHERE Description='"
						+ cond.getKey() + "'");
		if (!rs.next()) {
			String sText = "INSERT INTO tblcondition(Description,XPath) VALUES(\""
					+ cond.getKey() + "\",\"" + cond.getValue() + "\")";
			query.executeUpdate(sText);
		}
	}

	private void registerGlobalConditions() throws SQLException {
		Statement query = getConnection().createStatement();
		String[] days = { "Sunday", "Monday", "Tuesday", "Wednesday",
				"Thursday", "Friday", "Saturday" };
		for (int day = 0; day < 7; day++)
			registerCondition(new Condition("Day is " + days[day],
					"/*/timestamp/day = \'" + day + "\'"), query);
		for (int hour = 0; hour < 24; hour++)
			registerCondition(
					new Condition("Hour is " + String.format("%1$02d", hour)
							+ ":00", "/*/timestamp/time = \'"
							+ String.format("%1$02d", hour) + "00\'"), query);
		registerCondition(new Condition("General error", "/*/error"), query);
	}

	private void writeMessage(Statement query2, String msg, int status,
			int pend, int cmd, String argXML, Date createTime, Date execTime,
			int src, String mode, boolean data, boolean update, String from)
			throws SQLException {
		int index = 0;
		int len = msg.length();
		int error;
		do {
			String message = msg.substring(index,
					Math.min(index + MAX_RESPONSE_SIZE, len));
			index += MAX_RESPONSE_SIZE;
			if (len < index)
				error = status;
			else
				error = CommandResponse.LONG_RESPONSE;

			String sql;
			if (!update) {
				sql = "INSERT INTO tblcompleted(PendingId, CommandId, InXML, OutXML, InTimestamp, ExecTimestamp, Source, Success, Mode, Data) VALUES("
						+ pend
						+ ","
						+ cmd
						+ ",\'"
						+ argXML
						+ "\',\'"
						+ message
						+ "\',\'"
						+ SQL_DATE.format(createTime)
						+ "\',\'"
						+ SQL_DATE.format(execTime)
						+ "\',"
						+ src
						+ ","
						+ error
						+ ",\'" + mode + "\'," + (data ? "1" : "0") + ")";
			} else {
				sql = "UPDATE tblcompleted SET OutXML=" + "\'" + message
						+ "\',InTimestamp=\'" + SQL_DATE.format(createTime)
						+ "\',ExecTimestamp=\'" + SQL_DATE.format(execTime)
						+ "\',Success=" + error + ",Data=" + (data ? "1" : "0")
						+ " WHERE PendingId=" + pend;
			}
			query2.executeUpdate(sql);
		} while (index < len);

	}

	private boolean isServerRunning() {
		Statement query;
		Statement query1;
		Statement query2;
		try {
			query = getConnection().createStatement();
			query1 = getConnection().createStatement();
			query2 = getConnection().createStatement();
			ResultSet rs = query
					.executeQuery("SELECT a.GroupId FROM tblcommandgroupx a INNER JOIN tblcommandgroup b ON a.GroupId=b.GroupId AND Mode='"
							+ MODE_DEFAULT
							+ "' AND Type="
							+ TYPE_NO_EDIT
							+ " INNER JOIN tblcommand c ON a.CommandId=c.CommandId WHERE Class='"
							+ SystemCommand.class.getCanonicalName() + "'");
			if (rs.next()) {
				int key = pendCommand(query1, rs.getInt(1),
						SOURCE_COMMAND_LINE,
						"<args><arg id=\"status\">status</arg></args>", true);
				Thread.sleep(Long.parseLong(getParam(PARAM_POLLING)) + 5000);
				ResultSet rs2 = query2
						.executeQuery("SELECT Completed FROM tblpending WHERE PendingId="
								+ key);
				if (rs2.next()) {
					if (rs2.getInt(1) == 0)
						return false;
					else
						return true;
				} else
					return true;
			} else {
				System.out
						.println("System command not registered with database.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	public XPath getXPath() {
		if (xpath == null)
			xpath = XPathFactory.newInstance().newXPath();
		return xpath;
	}

	public double getNextTemp(String zone, double inside, double outside,
			boolean heating, long interval) {
		int intInside = (int) (inside * 10);
		int intOutside = (int) (outside * 10);
		int state = LogLeadTimeCommand.STATE_COOLING;
		if (heating) {
			state = LogLeadTimeCommand.STATE_WARMING;
		}
		String ds = getParam(PARAM_ACTIVE_DATASET);
		String sql = "SELECT * FROM tblleadtimes WHERE DataGroupId=" + ds
				+ " AND zone='" + zone + "' AND State=" + state
				+ " AND InsideTemp=" + intInside + " AND OutsideTemp="
				+ intOutside;
		double ret = 0.0;
		try {
			ResultSet rs = executeQuery(sql);
			int total = 0;
			int count = 0;
			while (rs.next()) {
				total += rs.getInt(6) * rs.getInt(7);
				count += rs.getInt(7);
			}
			if (count > 0) {
				ret = (double) total / (double) count / 10.0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return inside + ret;
	}

	public double getTempToReach(String zone, double targetInside,
			double outside, boolean heating, long interval) {

		double ret = -1000;
		double retT = -1000;
		// See which next temperature is nearest the targetTemperature
		for (double t = targetInside - 3; t < targetInside + 3; t += 0.1) {
			double t2 = getNextTemp(zone, t, outside, heating, interval);
			if (Math.abs(retT - targetInside) > Math.abs(t2 - targetInside)) {
				ret = t;
				retT = t2;
			}
		}
		if (ret == -1000) {
			ret = targetInside;
		}
		return ret;
	}

	public double getTempForecast(long timemillis) {
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(timemillis);
		int day = time.get(Calendar.DAY_OF_WEEK);
		String xpath = "weather/fcs[day=" + day + "]";
		String fc = getParam(PARAM_WEATHER);
		double ret = 0;

		try {
			ByteArrayInputStream conn = new ByteArrayInputStream(
					fc.getBytes("UTF-8"));
			Document forecast = getXMLFactory().newDocumentBuilder()
					.parse(conn);
			NodeList nl = (NodeList) getXPath().evaluate(xpath, forecast,
					XPathConstants.NODESET);
			double beforeTime = -1;
			double afterTime = -1;
			double beforeTemp = 0;
			double afterTemp = 0;
			double searchTime = time.get(Calendar.HOUR_OF_DAY) * 60
					+ time.get(Calendar.MINUTE);
			for (int node = 0; node < nl.getLength(); node++) {
				Node n = nl.item(node);
				NodeList reps = n.getChildNodes();
				for (int c = 0; c < reps.getLength(); c++) {
					NodeList rep = reps.item(c).getChildNodes();
					boolean getAfter = false;
					boolean getBefore = false;
					for (int d = 0; d < rep.getLength(); d++) {
						if (rep.item(d).getNodeName().equals("time")) {
							int t = Integer.parseInt(rep.item(d)
									.getTextContent());
							if (t < searchTime) {
								if (t > beforeTime) {
									beforeTime = t;
									getBefore = true;
								}
							} else if (t == searchTime) {
								beforeTime = t;
								afterTime = t;
								getAfter = true;
							} else {
								if (t < afterTime || afterTime == -1) {
									afterTime = t;
									getAfter = true;
								}
							}
						} else if (rep.item(d).getNodeName().equals("temp")) {
							if (getBefore) {
								beforeTemp = Double.parseDouble(rep.item(d)
										.getTextContent());
								getBefore = false;
							} else if (getAfter) {
								afterTemp = Double.parseDouble(rep.item(d)
										.getTextContent());
								getAfter = false;
							}
						}
					}
				}
				if (beforeTime == -1) {
					ret = afterTemp;
				} else if (afterTime == -1) {
					ret = beforeTemp;
				} else {
					ret = beforeTemp + (afterTemp - beforeTemp)
							* (searchTime - beforeTime)
							/ (afterTime - beforeTime);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return ret;
	}
}
