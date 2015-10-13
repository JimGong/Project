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
		} catch (Exception e) {
			System.out.println(e.toString());
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

					writeNestedMap(key, result.get(key), writer);
				}
				while (keys.hasNext()) {

					String key = keys.next();
					writer.write(",");
					writeNestedMap(key, result.get(key), writer);
				}

			}
			writer.newLine();
			writer.write("}");
		}
	}

	/**
	 * Print nested map
	 *
	 * @param key
	 * @param resultList
	 * @param writer
	 * @throws IOException
	 */
	private void writeNestedMap(String key, List<SearchResult> resultList,
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
	private void writeResultList(List<SearchResult> searchResult,
			BufferedWriter writer) throws IOException {

		SearchResult first = searchResult.get(0);

		writeSingleResult(first, writer);

		for (SearchResult result : searchResult) {
			if (result != first) {
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
	private void writeSingleResult(SearchResult searchResult,
			BufferedWriter writer) throws IOException {
		if (searchResult.location != "NULL") {
			writer.newLine();
			writer.write(JSONWriter.indent(2) + "{");

			writer.newLine();
			writer.write(
					JSONWriter.indent(3) + JSONWriter.quote("where") + ": ");
			writer.write(JSONWriter.quote(searchResult.location) + ",");

			writer.newLine();
			writer.write(
					JSONWriter.indent(3) + JSONWriter.quote("count") + ": ");
			writer.write(searchResult.frequency + ",");

			writer.newLine();
			writer.write(JSONWriter.indent(3) + JSONWriter.quote("index") + ": "
					+ searchResult.position);

			writer.newLine();
			writer.write(JSONWriter.indent(2) + "}");
		}
	}
}