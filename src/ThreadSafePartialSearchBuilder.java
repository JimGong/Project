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

// TODO Might make sense to have an interface or abstract class that is used by both your thread-safe and single-threaded search builders because they have the same methods.
/**
 * Thread safe version of partial search builder.
 */
public class ThreadSafePartialSearchBuilder {

	private final Map<String, List<SearchResult>> result;

	/*
	 * Create a logger for debug
	 */
	private static final Logger logger = LogManager.getLogger();
	/*
	 * Create a work queue for mutlithread
	 */
	private final WorkQueue minions;

	public ThreadSafePartialSearchBuilder(int numThreads) {
		result = new LinkedHashMap<>();
		minions = new WorkQueue(numThreads);
	}

	/**
	 * Parse file into partial search
	 *
	 * @param file
	 * @param index
	 * @throws IOException
	 */
	public synchronized void parseFile(Path file, ThreadSafeInvertedIndex index)
			throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file,
				Charset.forName("UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				result.put(line, null);
				minions.execute(new LineMinion(line, index));
			}
		}
	}

	/**
	 * Parse query and put into result map.
	 *
	 * @param line
	 * @param index
	 */
	public void parseLine(String line, InvertedIndex index) {
		String[] queryWords = InvertedIndexBuilder.splitLine(line);
		List<SearchResult> resultList = index.partialSearch(queryWords);
		synchronized (result) {
			result.put(line, resultList);
		}
	}

	/**
	 * Print result map to file
	 *
	 * @param output
	 * @throws IOException
	 */
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

	private class LineMinion implements Runnable {

		private String line;
		private ThreadSafeInvertedIndex index;

		public LineMinion(String line, ThreadSafeInvertedIndex index) {
			logger.debug("******** Minion created for {}", line);
			this.line = line;
			this.index = index;
		}

		@Override
		public void run() {
			logger.debug("--------- Minion going to run for {}", line);
			parseLine(line, index);
			logger.debug("######## Minion finished {}", line);
		}

	}

}
