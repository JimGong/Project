import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

public class JSONWriter {

	/**
	 * Write the word map as JSON object to the specified output path using the
	 * UTS character set.
	 *
	 * @param entry
	 * @param writer
	 * @throws IOException
	 */
	public static void writeNestedMap(
			Entry<String, TreeMap<String, TreeSet<Integer>>> entry,
			BufferedWriter writer) throws IOException {

		writer.newLine();
		writer.write(indent(1) + quote(entry.getKey()));
		writer.write(": {");
		TreeMap<String, TreeSet<Integer>> subMap = entry.getValue();
		if (!subMap.isEmpty()) {
			Entry<String, TreeSet<Integer>> subFirst = subMap.firstEntry();

			writeNestedSet(subFirst, writer);
			for (Entry<String, TreeSet<Integer>> subEntry : subMap
					.tailMap(subFirst.getKey(), false).entrySet()) {
				writer.write(",");
				writeNestedSet(subEntry, writer);
			}
		}
		writer.newLine();
		writer.write(indent(1) + "}");
	}

	/**
	 * Write the path/positions entry to the specified output path using the
	 * UTF-8 character set.
	 *
	 * @param entry
	 * @param writer
	 * @throws IOException
	 */
	private static void writeNestedSet(Entry<String, TreeSet<Integer>> entry,
			BufferedWriter writer) throws IOException {
		writer.newLine();
		writer.write(indent(2));
		writer.write(quote(entry.getKey()));
		writer.write(": [");
		TreeSet<Integer> subTreeSet = entry.getValue();
		writeTreeSet(subTreeSet, writer);
		writer.newLine();
		writer.write(indent(2) + "]");

	}

	/**
	 * Write the positions set to the specified output path using the UTF-8
	 * character set.
	 *
	 * @param elements
	 * @param writer
	 * @throws IOException
	 */
	public static void writeTreeSet(TreeSet<Integer> elements,
			BufferedWriter writer) throws IOException {

		if (!elements.isEmpty()) {
			Integer first = elements.first();
			writer.newLine();
			writer.write(indent(3) + first);

			for (int integer : elements.tailSet(first, false)) {
				writer.write(",");
				writer.newLine();
				writer.write(indent(3) + integer);
			}
		}
	}

	/**
	 * Helper method to indent several times by 2 spaces each time. For example,
	 * indent(0) will return an empty string, indent(1) will return 2 spaces,
	 * and indent(2) will return 4 spaces.
	 *
	 * @param times
	 * @return
	 * @throws IOException
	 */
	public static String indent(int times) {
		return times > 0 ? String.format("%" + (times * 2) + "s", " ") : "";
	}

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
	public static String quote(String text) {
		return "\"" + text + "\"";
	}

	/**
	 * Print nested map
	 *
	 * @param key
	 * @param resultList
	 * @param writer
	 * @throws IOException
	 */
	public static void writeNestedMap(String key, List<SearchResult> resultList,
			BufferedWriter writer) throws IOException {
		writer.newLine();
		writer.write(JSONWriter.indent(1) + JSONWriter.quote(key));
		writer.write(": [");
		if (!resultList.isEmpty()) {
			writeResultList(resultList, writer);
		}
		writer.newLine();
		writer.write(JSONWriter.indent(1) + "]");
	}

	/**
	 * Print result list
	 *
	 * @param searchResult
	 * @param writer
	 * @throws IOException
	 */
	public static void writeResultList(List<SearchResult> searchResult,
			BufferedWriter writer) throws IOException {

		SearchResult first = searchResult.get(0);

		writeSingleResult(first, writer);

		// TODO Traditional for loop from i = 1, -or- use listIterator() -or- subList()
		for (SearchResult result : searchResult) {
			if (result != first) { // TODO Risky, because using == != instead of .equals()
				writer.write(",");
				writeSingleResult(result, writer);
			}
		}

	}

	/**
	 * Print single result
	 *
	 * @param searchResult
	 * @param writer
	 * @throws IOException
	 */
	public static void writeSingleResult(SearchResult searchResult,
			BufferedWriter writer) throws IOException {
		if (searchResult.getLocation() != "NULL") {
			writer.newLine();
			writer.write(JSONWriter.indent(2) + "{");

			writer.newLine();
			writer.write(
					JSONWriter.indent(3) + JSONWriter.quote("where") + ": ");
			writer.write(JSONWriter.quote(searchResult.getLocation()) + ",");

			writer.newLine();
			writer.write(
					JSONWriter.indent(3) + JSONWriter.quote("count") + ": ");
			writer.write(searchResult.getFrequency() + ",");

			writer.newLine();
			writer.write(JSONWriter.indent(3) + JSONWriter.quote("index") + ": "
					+ searchResult.getPosition());

			writer.newLine();
			writer.write(JSONWriter.indent(2) + "}");
		}
	}
}
