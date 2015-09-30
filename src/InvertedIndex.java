import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Create inverted index to store word, path and position
 */
public class InvertedIndex {
	
	/* TODO Rule of thumb:
	   Make variables/members FINAL when possible (without breaking anything else)
	   Make functions/methods STATIC when possible (without breaking anything else)
	*/

	// TODO Make index final NOT static.
	/**
	 * Initializes an empty inverted index map, the key is the word
	 */
	private static TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Initializes an empty inverted index map, the key is the word
	 */
	public InvertedIndex() {
		index = new TreeMap<>();
	}

	/**
	 * add word, path, position into the map. If the word is found, will attempt
	 * to see if the path can be found. If so, the path/position pair will be
	 * add into the map.
	 *
	 * @param word
	 * @param path
	 * @param position
	 */
	public void add(String word, String path, int position) {
		if (hasWord(word) == false) {
			TreeSet<Integer> positions = new TreeSet<Integer>();
			positions.add(position);

			TreeMap<String, TreeSet<Integer>> fileMap = new TreeMap<String, TreeSet<Integer>>();
			fileMap.put(path, positions);
			index.put(word, fileMap);
		}
		else if (hasWord(word) == true) {

			TreeMap<String, TreeSet<Integer>> fileMap = index.get(word);
			// 1. create new subMap, add new file and position (file doesn't
			// exist)

			// 2. get old subMap, add new position (file exist)
			if (hasPath(word, path) == false) {
				TreeSet<Integer> newSet = new TreeSet<Integer>();
				newSet.add(position);
				fileMap.put(path, newSet);
				index.put(word, fileMap);
			}
			else {
				TreeSet<Integer> set = fileMap.get(path);
				set.add(position);
				fileMap.put(path, set);
				index.put(word, fileMap);
			}
		}
	}

	// TODO Make some of these methods public, makes your code more useful for others
	/**
	 * test if the the word can be found in the map.
	 *
	 * @param word
	 * @return true if the map has the word
	 */
	private boolean hasWord(String word) {
		if (index.containsKey(word)) {
			return true;
		}
		return false;
	}

	/**
	 * test if the path can be found in the map, if the word can be found.
	 *
	 * @param word
	 * @param path
	 * @return true if the path and word both exist
	 */
	private boolean hasPath(String word, String path) {
		if (hasWord(word)) {
			TreeMap<String, TreeSet<Integer>> fileMap = index.get(word);
			if (fileMap.containsKey(path)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * test if the position can be found in the map, if the word and path both
	 * can be found
	 *
	 * @param word
	 * @param path
	 * @param position
	 * @return
	 */

	private boolean hasPosition(String word, String path, int position) {
		if (hasWord(word) && hasPath(word, path)) {
			TreeMap<String, TreeSet<Integer>> fileMap = index.get(word);
			TreeSet<Integer> positions = fileMap.get(path);
			if (positions.contains(position)) {
				return true;
			}
		}
		return false;
	}

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

	// TODO Should NOT be static, because your index will NOT be static.
	// TODO Maybe call this print() or toJSON(), etc. 
	/**
	 * Write the map as a JSON object to the specified output path using the
	 * UTF-8 character set.
	 *
	 * @param output
	 */
	public static void printWordMap(Path output) {

		try (BufferedWriter bw = new BufferedWriter(
				new FileWriter(output.toFile()))) {

			bw.write("{");
			if (!index.isEmpty()) {
				Entry<String, TreeMap<String, TreeSet<Integer>>> first = index
						.firstEntry();

				// TODO JSONWriter.outputEntry();
				output_Outside(first, bw);

				for (Entry<String, TreeMap<String, TreeSet<Integer>>> entry : index
						.tailMap(first.getKey(), false).entrySet()) {
					bw.write(",");
					output_Outside(entry, bw);
				}
			}
			// TODO bw.newLine();
			bw.write("\n}");

		} catch (IOException e) {
			System.err.println("NO output Found");
		}

	}
	
	// TODO Make these public static methods in a separate JSONWriter class (might help with project 2)

	/**
	 * Write the word map as JSON object to the specified output path using the
	 * UTS character set.
	 *
	 * @param entry
	 * @param bw
	 * @throws IOException
	 */
	private static void output_Outside(
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

}
