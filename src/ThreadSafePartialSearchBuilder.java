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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Thread safe version of partial search builder.
 */
public class ThreadSafePartialSearchBuilder
		implements PartialSearchBuilderInterface {

	private final Map<String, List<SearchResult>> result;
	private final InvertedIndex index;

	/**
	 * Create a logger for debug
	 */
	private static final Logger logger = LogManager.getLogger();
	/**
	 * Create a work queue for mutlithread
	 */
	private final WorkQueue minions;

	/**
	 * Initiate result map, work queue and inverted index
	 *
	 * @param numThreads
	 * @param index
	 */
	public ThreadSafePartialSearchBuilder(int numThreads, InvertedIndex index) {
		result = new LinkedHashMap<>();
		minions = new WorkQueue(numThreads);
		this.index = index;
	}

	/**
	 * Parse file into partial search
	 *
	 * @param file
	 * @param index
	 * @throws IOException
	 */
	@Override
	public synchronized void parseFile(Path file) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file,
				Charset.forName("UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				parseLine(line);
			}
		}
	}

	/**
	 * Parse query and put into result map.
	 *
	 * @param line
	 * @param index
	 */
	@Override
	public void parseLine(String line) {
		synchronized (result) {
			result.put(line, null);
		}
		minions.execute(new LineMinion(line, this.index));
	}

	/**
	 * Print result map to file
	 *
	 * @param output
	 * @throws IOException
	 */
	@Override
	public synchronized void print(Path output) throws IOException {

		finish();
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

	/**
	 * Helper method, that helps a thread wait until all of the current work is
	 * done. This is useful for resetting the counters or shutting down the work
	 * queue.
	 */
	public synchronized void finish() {
		minions.finish();
	}

	/**
	 * Will shutdown the work queue after all the current pending work is
	 * finished. Necessary to prevent our code from running forever in the
	 * background.
	 */
	public synchronized void shutdown() {
		finish();
		logger.debug("Shutting down");
		minions.shutdown();
	}

	/**
	 * Handles per-line parsing. If a line of queries is encountered, a new
	 * {@link LineMinion} is created to handle that line of queries.
	 */
	private class LineMinion implements Runnable {

		private String line;
		private ThreadSafeInvertedIndex index;

		public LineMinion(String line, InvertedIndex index) {
			logger.debug("******** Minion created for {}", line);
			this.line = line;
			this.index = (ThreadSafeInvertedIndex) index;
		}

		@Override
		public void run() {
			logger.debug("--------- Minion going to run for {}", line);
			String[] queryWords = InvertedIndexBuilder.splitLine(line);
			List<SearchResult> resultList = index.partialSearch(queryWords);
			synchronized (result) {
				result.put(line, resultList);
			}
			logger.debug("######## Minion finished {}", line);
		}

	}

}
