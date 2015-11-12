import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiThreadInvertedIndexBuilder {

	private static final Logger logger = LogManager.getLogger();
	private final WorkQueue minions;
	private int pending;

	public MultiThreadInvertedIndexBuilder(int numThreads) {
		minions = new WorkQueue(numThreads);
		pending = 0;
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

	private class FileMinion implements Runnable {

		private Path file;
		private ThreadSafeInvertedIndex index;

		public FileMinion(Path file, ThreadSafeInvertedIndex index) {
			logger.debug("Minion created for {}", file);
			this.file = file;
			this.index = index;
			incrementPending();
		}

		@Override
		public void run() {
			try {
				parseFile(file, index);
				// decrementPending();
			} catch (IOException e) {
				logger.warn("Unable to parse {}", file);
				logger.catching(Level.DEBUG, e);
			} finally {
				decrementPending();
			}
			logger.debug("######## Minion finished {}", file);
		}
	}

	public synchronized void traverseDirectory(Path directory,
			ThreadSafeInvertedIndex index) {
		try {
			if (Files.isDirectory(directory)) {
				traverse(directory, index);
			}
			else {
				if (directory.getFileName().toString().toLowerCase()
						.endsWith(".txt")) {
					minions.execute(new FileMinion(directory, index));
				}
			}
		} catch (IOException e) {
			System.out.println("Input file NOT found");
		}
	}

	private synchronized void traverse(Path path, ThreadSafeInvertedIndex index)
			throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {

			for (Path file : listing) {

				if (Files.isDirectory(file)) {
					traverseDirectory(file, index);
				}
				else {
					if (file.getFileName().toString().toLowerCase()
							.endsWith(".txt")) {
						minions.execute(new FileMinion(file, index));
					}
				}
			}
		}
	}

	private synchronized void parseFile(Path file,
			ThreadSafeInvertedIndex index) throws IOException {
		int position = 1;

		TreeMap<String, TreeMap<String, TreeSet<Integer>>> localIndex = new TreeMap<>();

		try (BufferedReader reader = Files.newBufferedReader(file,
				Charset.forName("UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] words = InvertedIndexBuilder.splitLine(line);
				for (String word : words) {
					// add(word, file.toFile().getPath(), position, localIndex);
					index.add(word, file.toFile().getPath(), position);
					position++;
				}
			}
		}
		index.merge(localIndex);
	}

	private void add(String word, String path, int position,
			TreeMap<String, TreeMap<String, TreeSet<Integer>>> localIndex) {
		if (localIndex.containsKey(word) == false) {
			localIndex.put(word, new TreeMap<String, TreeSet<Integer>>());
		}

		if (localIndex.get(word).containsKey(path) == false) {
			localIndex.get(word).put(path, new TreeSet<Integer>());
		}
		localIndex.get(word).get(path).add(position);
	}
}
