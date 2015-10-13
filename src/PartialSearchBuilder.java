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

public class PartialSearchBuilder {

	private final Map<String, List<SearchResult>> result;

	public PartialSearchBuilder() {
		result = new LinkedHashMap<>();
	}

	public void parseFile(Path file, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file,
				Charset.forName("UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] queryWords = InvertedIndexBuilder.splitLine(line);

				List<SearchResult> resultList = index.partialSearch(queryWords);

				// System.out.println("\nqueeryWords: " + line);
				for (SearchResult singleR : resultList) {
					if (singleR.location != "NULL") {
						// System.out.println("where:" + singleR.location
						// + "\ncount: " + singleR.frequency + "\nindex: "
						// + singleR.position);
					}
				}

				result.put(line, resultList);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public void print(Path output) throws IOException {
		// System.out.println("\nPrinting search result");
		try (BufferedWriter writer = Files.newBufferedWriter(output,
				Charset.forName("UTF-8"))) {
			writer.write("{");
			if (!result.isEmpty()) {
				Iterator<String> keys = result.keySet().iterator();

				if (keys.hasNext()) {
					String key = keys.next();
					// System.out.println("first entry: ");
					// System.out.println(key + ", " + result.get(key));
					writeNestedMap(key, result.get(key), writer);
				}
				while (keys.hasNext()) {

					String key = keys.next();
					// System.out.println(key + ", " + result.get(key));
					writer.write(",");
					writeNestedMap(key, result.get(key), writer);
				}

			}
			writer.newLine();
			writer.write("}");
		}
	}

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