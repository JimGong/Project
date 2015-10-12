import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class PartialSearchBuilder {

	private final TreeMap<String, List<SearchResult>> result;

	public PartialSearchBuilder() {
		result = new TreeMap<>();
	}

	public void parseFile(Path file, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file,
				Charset.forName("UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] queryWords = InvertedIndexBuilder.splitLine(line);

				List<SearchResult> resultList = index.partialSearch(queryWords);

				System.out.println("\nqueeryWords: " + line);
				for (SearchResult singleR : resultList) {
					if (singleR.location != "NULL") {
						System.out.println("where:" + singleR.location
								+ "\ncount: " + singleR.frequency + "\nindex: "
								+ singleR.position);
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
				Entry<String, List<SearchResult>> first = result.firstEntry();
				// System.out.println("print first");
				writeResult(first, writer);
				for (Entry<String, List<SearchResult>> entry : result
						.tailMap(first.getKey(), false).entrySet()) {
					writer.write(",");
					writeResult(entry, writer);
				}

			}
			writer.newLine();
			writer.write("}");
		}
	}

	private void writeResult(Entry<String, List<SearchResult>> entry,
			BufferedWriter writer) throws IOException {
		writer.newLine();
		writer.write(JSONWriter.indent(1) + JSONWriter.quote(entry.getKey()));
		writer.write(": [");
		if (entry.getValue() != (new SearchResult(0, 0, ""))) {
			writeResultList(entry.getValue(), writer);
		}
		writer.newLine();
		writer.write(JSONWriter.indent(1) + "]");

	}

	private void writeResultList(List<SearchResult> searchResult,
			BufferedWriter writer) throws IOException {
		writer.newLine();
		writer.write(JSONWriter.indent(2) + "{");

		for (SearchResult result : searchResult) {
			writeSingleResult(result, writer);
		}
		writer.newLine();
		writer.write(JSONWriter.indent(2) + "}");
	}

	private void writeSingleResult(SearchResult searchResult,
			BufferedWriter writer) throws IOException {
		if (searchResult.location != "NULL") {
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
		}

	}
}