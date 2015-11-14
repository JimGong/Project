import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO Javadoc

public class ThreadSafePartialSearchBuilder {

	private final Map<String, List<SearchResult>> result;

	private static final Logger logger = LogManager.getLogger();
	private final WorkQueue minions;
	private int pending;

	public ThreadSafePartialSearchBuilder(int numThreads) {
		// TODO Can't use any synchronized maps. YOU must synchronize!!
		result = Collections.synchronizedMap(new LinkedHashMap<>());
		minions = new WorkQueue(numThreads);
		pending = 0;
	}

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

	public synchronized void parseLine(String line, InvertedIndex index) {
		// TODO These two lines do NOT need to be synchronized
		String[] queryWords = InvertedIndexBuilder.splitLine(line);
		List<SearchResult> resultList = index.partialSearch(queryWords);

		// TODO Only this needs to be synchonrized
		result.put(line, resultList);
	}

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

	public synchronized void finish() {
		try {
			while (pending > 0) {
				logger.debug("Waiting until finished");
				this.wait();
			}
		} catch (InterruptedException e) {
			logger.debug("Finish interrupted", e);
		}
	}

	public synchronized void shutdown() {
		logger.debug("Shutting down");
		finish();
		minions.shutdown();
	}

	private synchronized void incrementPending() {
		pending++;
		logger.debug("Pending is now {}", pending);
	}

	private synchronized void decrementPending() {
		pending--;
		logger.debug("Pending is now {}", pending);

		if (pending <= 0) {
			this.notifyAll();
		}
	}

	private class LineMinion implements Runnable {

		private String line;
		private ThreadSafeInvertedIndex index;

		public LineMinion(String line, ThreadSafeInvertedIndex index) {
			logger.debug("Minion created for {}", line);
			this.line = line;
			this.index = index;
			incrementPending();
		}

		@Override
		public void run() {
			parseLine(line, index);
			decrementPending();
		}

	}

}
