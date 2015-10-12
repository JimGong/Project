import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	// You do usually describe the parameters too, but in this case they
	// are self-explainatory.
	/**
	 * Add word, path, position into the map. If the word is found, will attempt
	 * to see if the path can be found. If so, the path/position pair will be
	 * add into the map.
	 *
	 * @param word
	 *            the word that add into map
	 * @param path
	 *            the path the word belong to
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

			if (hasPath(word, path) == false) {
				index.get(word).put(path, new TreeSet<Integer>());
			}
			index.get(word).get(path).add(position);
		}
	}

	/**
	 * Test if the the word can be found in the map.
	 *
	 * @param word
	 * @return true if the map has the word
	 */
	public boolean hasWord(String word) {
		return index.containsKey(word);
	}

	/**
	 * Test if the path can be found in the map, if the word can be found.
	 *
	 * @param word
	 * @param path
	 * @return true if the path and word both exist
	 */
	public boolean hasPath(String word, String path) {
		if (hasWord(word)) {
			return index.get(word).containsKey(path);
		}
		return false;
	}

	/**
	 * Test if the position can be found in the map, if the word and path both
	 * can be found
	 *
	 * @param word
	 * @param path
	 * @param position
	 * @return
	 */
	public boolean hasPosition(String word, String path, int position) {
		if (hasWord(word) && hasPath(word, path)) {
			return index.get(word).get(path).contains(position);
		}
		return false;
	}

	/**
	 * Write the map as a JSON object to the specified output path using the
	 * UTF-8 character set.
	 *
	 * @param output
	 */
	public void print(Path output) throws IOException {

		try (BufferedWriter writer = Files.newBufferedWriter(output,
				Charset.forName("UTF-8"))) {
			writer.write("{");
			if (!index.isEmpty()) {
				Entry<String, TreeMap<String, TreeSet<Integer>>> first = index
						.firstEntry();

				JSONWriter.writeNestedMap(first, writer);

				for (Entry<String, TreeMap<String, TreeSet<Integer>>> entry : index
						.tailMap(first.getKey(), false).entrySet()) {
					writer.write(",");
					JSONWriter.writeNestedMap(entry, writer);
				}
			}
			writer.newLine();
			writer.write("}");
		}
	}

	public List<SearchResult> partialSearch(String[] queryWords) {
		String location = "NULL"; /* where */
		int frequency = 0; /* count */
		int position = 0; /* index */
		Map<String, SearchResult> result = new HashMap<>();/*
															 * path is key
															 */
		for (String queryWord : queryWords) {
			// System.out.println("\nquery word: [" + queryWord + "]");
			for (String word : index.keySet()) {
				if (word.startsWith(queryWord)) {
					// System.out.println("find word: " + word + " in path: "
					// + index.get(word).firstKey() + " position: "
					// + index.get(word).firstEntry().getValue().first());
					frequency++;
					location = index.get(word).firstKey();
					position = index.get(word).firstEntry().getValue().first();
				}
			}
			// System.out
			// .println("where: " + location + "\nquery word: " + queryWord
			// + "\ncount: " + frequency + "\nindex: " + position);
			SearchResult searchResult = new SearchResult(frequency, position,
					location);

			result.put(location, searchResult);
		}
		List<SearchResult> resultList = null;
		try {
			resultList = new ArrayList<SearchResult>(result.values());
			Collections.sort(resultList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// System.out.println("\nprint unsorted result list");
			// System.out.println(resultList);
			// Collections.sort(resultList);
			// System.out.println("\nprint sorted result list");
			// System.out.println(resultList);
		}
		return resultList;
	}

	@Override
	public String toString() {
		return "InvertedIndex [index=" + index + "]";
	}

}
