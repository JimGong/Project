import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiThreadInvertedIndexBuilder {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * Create a WorkQueue
	 */
	private final WorkQueue minions;

	/**
	 * Initiate the WorkQueue and set the numThread
	 *
	 * @param numThreads
	 */
	public MultiThreadInvertedIndexBuilder(int numThreads) {
		minions = new WorkQueue(numThreads);
	}

	/**
	 * Helper method, that helps a thread wait until all of the current work is
	 * done. This is useful for resetting the counters or shutting down the work
	 * queue.
	 */
	public void finish() {
		minions.finish();
	}

	/**
	 * Will shutdown the work queue after all the current pending work is
	 * finished. Necessary to prevent our code from running forever in the
	 * background.
	 */
	public void shutdown() {
		finish();
		logger.debug("Shutting down");
		minions.shutdown();
	}

	/**
	 * Handles per-text file parsing. If a txt file is encountered, a new
	 * {@link FileMinion} is created to handle that txt file.
	 */
	private class FileMinion implements Runnable {

		private Path file;
		private ThreadSafeInvertedIndex index;

		public FileMinion(Path file, ThreadSafeInvertedIndex index) {
			logger.debug("******** Minion created for {}", file);
			this.file = file;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.parseFile(file, local);
				logger.debug("going to addAll {}", file);
				index.addAll(local);
				logger.debug("done with addAll {}", file);
			} catch (IOException e) {
				logger.warn("Unable to parse {}", file);
				logger.catching(Level.DEBUG, e);
			}
			logger.debug("######## Minion finished {}", file);
		}
	}

	/**
	 * Traverse the directory to build inverted index map
	 *
	 * @param directory
	 * @param index
	 */
	public void traverseDirectory(Path directory,
			ThreadSafeInvertedIndex index) {
		try {
			if (Files.isDirectory(directory)) {
				try (DirectoryStream<Path> listing = Files
						.newDirectoryStream(directory)) {
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
		} catch (IOException e) {
			System.out.println("Input file NOT found");
		}
	}

	/**
	 * Execute the minion for the file passed in
	 *
	 * @param file
	 * @param index
	 */
	public void parseFile(Path file, ThreadSafeInvertedIndex index) {
		minions.execute(new FileMinion(file, index));
	}

	/**
	 * Traverse the URL given and find up to 50 URL find in the URL.
	 *
	 * @param link
	 * @param index
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void traverseURL(String link, ThreadSafeInvertedIndex index)
			throws UnknownHostException, MalformedURLException, IOException {
		WebCrawler webCrawler = new WebCrawler(minions.size(), index);
		/* key is URL, value is the HTML page */
		try {
			webCrawler.traverse(link);

		} finally {
			webCrawler.addToIndex();
		}

	}
}
