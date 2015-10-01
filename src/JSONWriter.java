import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

public class JSONWriter {

	// TODO Avoid "bw" as a variable name.
	// TODO There is a better name than "output_Outside". Java does not use underscore _ in variable names in general.
	// TODO How about writeNestedMap()?
	/**
	 * Write the word map as JSON object to the specified output path using the
	 * UTS character set.
	 *
	 * @param entry
	 * @param bw
	 * @throws IOException
	 */
	public static void output_Outside(
			Entry<String, TreeMap<String, TreeSet<Integer>>> entry,
			BufferedWriter bw) throws IOException {

		bw.newLine();
		bw.write(indent(1) + quote(entry.getKey()));
		bw.write(": {");
		TreeMap<String, TreeSet<Integer>> subMap = entry.getValue();
		if (!subMap.isEmpty()) {
			Entry<String, TreeSet<Integer>> subFirst = subMap.firstEntry();

			output_Mid(subFirst, bw);
			for (Entry<String, TreeSet<Integer>> subEntry : subMap
					.tailMap(subFirst.getKey(), false).entrySet()) {
				bw.write(",");
				output_Mid(subEntry, bw);
			}
		}
		bw.newLine();
		bw.write(indent(1) + "}");
	}

	// TODO Rename to something better... like writeNestedSet()...
	/**
	 * Write the path/positions entry to the specified output path using the
	 * UTF-8 character set.
	 *
	 * @param entry
	 * @param bw
	 * @throws IOException
	 */
	private static void output_Mid(Entry<String, TreeSet<Integer>> entry,
			BufferedWriter bw) throws IOException {
		bw.newLine();
		bw.write(indent(2));
		bw.write(quote(entry.getKey()));
		bw.write(": [");
		TreeSet<Integer> subTreeSet = entry.getValue();
		output_Inside(subTreeSet, bw);
		bw.newLine();
		bw.write(indent(2) + "]");

	}

	// TODO Make public, rename writeTreeSet()?
	/**
	 * Write the positions set to the specified output path using the UTF-8
	 * character set.
	 *
	 * @param elements
	 * @param bw
	 * @throws IOException
	 */
	private static void output_Inside(TreeSet<Integer> elements,
			BufferedWriter bw) throws IOException {

		if (!elements.isEmpty()) {
			Integer first = elements.first();
			bw.newLine();
			bw.write(indent(3) + first);

			for (int integer : elements.tailSet(first, false)) {
				bw.write(",");
				bw.newLine();
				bw.write(indent(3) + integer);
			}
		}

	}

	// TODO Could make public. Does not actually throw an IOException!
	/**
	 * Helper method to indent several times by 2 spaces each time. For example,
	 * indent(0) will return an empty string, indent(1) will return 2 spaces,
	 * and indent(2) will return 4 spaces.
	 *
	 * <p>
	 * <em>Using this method is optional!</em>
	 * </p>
	 *
	 * @param times
	 * @return
	 * @throws IOException
	 */
	private static String indent(int times) throws IOException {
		return times > 0 ? String.format("%" + (times * 2) + "s", " ") : "";
	}

	// TODO Could make public.
	/**
	 * Helper method to quote text for output. This requires escaping the
	 * quotation mark " as \" for use in Strings. For example:
	 *
	 * <pre>
	 * String text = "hello world";
	 * System.out.println(text); // output: hello world
	 * System.out.println(quote(text)); // output: "hello world"
	 * </pre>
	 *
	 * @param text
	 *            input to surround with quotation marks
	 * @return quoted text
	 */
	private static String quote(String text) {
		return "\"" + text + "\"";
	}
}
