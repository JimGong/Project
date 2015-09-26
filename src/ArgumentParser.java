import java.util.HashMap;
import java.util.Map;

// TODO Add back Javadoc comments to every member, method, and class.
// TODO http://www.oracle.com/technetwork/articles/java/index-137868.html

public class ArgumentParser {

	private final Map<String, String> argumentMap; // TODO Good use of final

	public ArgumentParser() {
		argumentMap = new HashMap<>();
	}

	public ArgumentParser(String[] args) {
		this();
		// System.out.println("num args: " + args.length);
		parseArguments(args);
	}

	private void parseArguments(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String curArg = args[i];
			String nextArg;

			if ((i + 1) < args.length) {
				// TODO Really only remove spaces at the start or the end of the String args[i + 1].trim()
				// TODO Example: -input "/home/user/sjengle/My Documents/blah" (spaces can appear in the middle of a path)
				nextArg = args[i + 1].replaceAll("\\s+", "");
			}
			else {
				nextArg = args[i];
			}

			if (isFlag(curArg) && isValue(nextArg)) {
				// System.out.println("flag: " + curArg + " found -- value: "
				// + nextArg + " found");
				argumentMap.put(curArg, nextArg);

				i++;
			}
			else if (isFlag(curArg) && (isValue(nextArg) == false)) {
				// System.out.println("flag: " + curArg + " found");
				argumentMap.put(curArg, null);

			}
		}

	}

	// TODO Make static again: faster code and easier to reuse this method
	public boolean isFlag(String arg) {
		arg = arg.trim();
		if (arg.startsWith("-") && (arg.length() > 1) && (!arg.endsWith(" "))) {
			return true;
		}
		else {
			return false;
		}
	}

	// TODO Make static
	public boolean isValue(String arg) {
		if ((!arg.startsWith("-")) && (arg.length() >= 1) && (arg != " ")
				&& (!arg.endsWith("-"))) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean hasFlag(String flag) {
		if (argumentMap.containsKey(flag)) {
			return true;
		}
		return false;
	}

	public boolean hasValue(String flag) {
		if (argumentMap.get(flag) != null) {
			return true;
		}
		return false;
	}

	public String getValue(String flag) {
		return argumentMap.get(flag);
	}

}
