import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Build Partial Search
 */
public class PartialSearchBuilder {

	/**
	 * Stores search result in a map, where the key is the path
	 */
	private final Map<String, List<SearchResult>> result;

	/**
	 * Initializes an empty result map.
	 */
	public PartialSearchBuilder() {
		result = new LinkedHashMap<>();
	}

	/**
	 * Parse file into partial search
	 *
	 * @param file
	 * @param index
	 * @throws IOException
	 */
	public void parseFile(Path file, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file,
				Charset.forName("UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] queryWords = InvertedIndexBuilder.splitLine(line);

				List<SearchResult> resultList = index.partialSearch(queryWords);

				result.put(line, resultList);
			}
		}
	}

	/**
	 * Print result map to file
	 *
	 * @param output
	 * @throws IOException
	 */
	public void print(Path output) throws IOException {
		// System.out.println("\nPrinting search result");
		try (BufferedWriter writer = Files.newBufferedWriter(output,
				Charset.forName("UTF-8"))) {
			writer.write("{");
			if (!result.isEmpty()) {
				Iterator<String> keys = result.keySet().iterator();

				if (keys.hasNext()) {
					String key = keys.next();

					JSONWriter.writeNestedMap(key, result.get(key), writer);
				}
				while (keys.hasNext()) {

					String key = keys.next();
					writer.write(",");
					JSONWriter.writeNestedMap(key, result.get(key), writer);
				}

			}
			writer.newLine();
			writer.write("}");
		}
	}

}
