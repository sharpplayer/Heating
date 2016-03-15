package uk.org.hrbc.commands;

import java.util.Arrays;
import java.util.Vector;

import uk.org.hrbc.HeatingSystem;
import uk.org.hrbc.commands.responses.CommandResponse;

public class SetPointNotOccupiedCommand extends BasicAreaCommand {

	public final static String TAG_VALVE = "spno";

	public SetPointNotOccupiedCommand() {
		super(TAG_VALVE);
	}

	@Override
	public CommandResponse execute(HeatingSystem system) {
		return super.executeGet(system, "K2", null, true);
	}

	@Override
	public int getAccess() {
		return HeatingSystem.ACCESS_ADMIN;
	}

	@Override
	public String getDescription(String mode) {
		if (mode.equals(HeatingSystem.MODE_DEFAULT)) {
			return "Get Set Point not occupied (Frost temperature)";
		} else {
			return "Set Set Point not occupied (Frost temperature)";
		}
	}

	@Override
	public Vector<String> getModes() {
		return new Vector<String>(Arrays.asList(HeatingSystem.MODE_DEFAULT, HeatingSystem.MODE_EDIT));
	}
}
