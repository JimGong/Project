import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Create inverted index to store word, path and position
 */
public class InvertedIndex {

	/*
	 * Rule of thumb: Make variables/members FINAL when possible (without
	 * breaking anything else) Make functions/methods STATIC when possible
	 * (without breaking anything else)
	 */

	/**
	 * Initializes an empty inverted index map, the key is the word
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;

	/**
	 * Initializes an empty inverted index map, the key is the word
	 */
	public InvertedIndex() {
		index = new TreeMap<>();
	}

	// TODO Capitalize the first word in your javadoc comments. You do usually
	// TODO describe the parameters too, but in this case they are self-explainatory. 
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

			if (hasPath(word, path) == false) {
				index.get(word).put(path, new TreeSet<Integer>());
			}
			index.get(word).get(path).add(position);
		}
	}

	/**
	 * test if the the word can be found in the map.
	 *
	 * @param word
	 * @return true if the map has the word
	 */
	public boolean hasWord(String word) {
		// TODO Can make this a 1 line method, since index.containsKey(word) returns true or false...
		// TODO Specifically: "return index.containsKey(word);" is the only line you need to achieve the same logic.
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
	public boolean hasPath(String word, String path) {
		// TODO See comments in hasWord(), this can be simplified a bit.
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
	public boolean hasPosition(String word, String path, int position) {
		// TODO See comments in hasWord(), this can be simplified a bit.
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
	 * Write the map as a JSON object to the specified output path using the
	 * UTF-8 character set.
	 *
	 * @param output
	 */
	public void print(Path output) {
		// TODO Try to avoid variable names like "bw". Use something like "writer" instead.
		try (BufferedWriter bw = Files.newBufferedWriter(output,
				Charset.forName("UTF-8"))) {
			bw.write("{");
			if (!index.isEmpty()) {
				Entry<String, TreeMap<String, TreeSet<Integer>>> first = index
						.firstEntry();

				JSONWriter.output_Outside(first, bw);

				for (Entry<String, TreeMap<String, TreeSet<Integer>>> entry : index
						.tailMap(first.getKey(), false).entrySet()) {
					bw.write(",");
					JSONWriter.output_Outside(entry, bw);
				}
			}
			bw.newLine();
			bw.write("}");

		} catch (IOException e) {
			System.err.println("NO output Found");
		}
		// TODO You need try-with-resources, but you could throw the exception.
		// TODO This will let Driver (or any other class) react to the exception.
		// TODO For example, another version of Driver might re-prompt the user for a new output path.
	}

}
