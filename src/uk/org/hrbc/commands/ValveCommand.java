package uk.org.hrbc.commands;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;

public class ValveCommand extends BasicAreaCommand {

	public final static String TAG_VALVE = "valve";

	public ValveCommand() {
		super(TAG_VALVE);
	}

	@Override
	public CommandResponse execute(HeatingSystem system) {
		return super.executeGet(system, "S2", null, true);
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_ADMIN;
	}

	@Override
	public String getDescription(String mode) {
		return "Get current valve opening";
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT));
	}

	@Override
	public Hashtable<String, String> getConditions(HeatingSystem system) {
		Hashtable<String, String> conds = super.getConditions(system);
		int[] temps = { 5, 95 };
		for (int temp : temps)
			conds.put("Valve is less than " + temp + " percent", "number(/*/" + TAG_VALVE + "/value) &lt; " + temp);
		for (int temp : temps)
			conds.put("Valve is greater than " + temp + " percent", "number(/*/" + TAG_VALVE + "/value) &gt; " + temp);
		return conds;
	}

}
