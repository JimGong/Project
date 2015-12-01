import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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

		LinkedHashMap<String, String> links = new LinkedHashMap<String, String>();
		/* key is URL, value is the clean HTML page */

		traverse(link, link, links, index);

		addToIndex(links, index);
	}

	public void traverse(String absolute, String root,
			LinkedHashMap<String, String> links, ThreadSafeInvertedIndex index)
					throws UnknownHostException, MalformedURLException,
					IOException {
		/* recursive call itself until the map hit the capacity of 50 */
		if (links.size() < 50) {
			addToMap(absolute, links);
			logger.debug("finding innerUrl in {}", absolute);
			ArrayList<String> innerURLs = LinkParser
					.listLinks(links.get(absolute));
			logger.debug("inner url size {}", innerURLs.size());
			if (!innerURLs.isEmpty()) {
				for (int i = 0; i < innerURLs.size(); i++) {
					String innerLink = innerURLs.get(i);
					URL base = new URL(absolute);
					URL absoluteURL = new URL(base, innerLink);
					String absoluteLink = absoluteURL.toString();
					if (absoluteLink.contains("#")) {
						logger.debug("inner link found {}", absoluteLink);
					}
					if ((!links.containsKey(absoluteLink))
							&& (!absoluteLink.contains("#"))) {
						logger.debug("keep traversing for {}", absoluteLink);
						traverse(absoluteLink, root, links, index);
					}
					logger.debug("map already containKey {}", absoluteLink);
				}
			}
		}
	}

	public void addToIndex(LinkedHashMap<String, String> links,
			ThreadSafeInvertedIndex index) {
		int count = 0;
		for (String url : links.keySet()) {
			if (count < 50) {
				logger.debug("add url {} into index", url);
				String html = links.get(url);
				String cleanHtml = HTMLCleaner.cleanHTML(html);
				String[] words = InvertedIndexBuilder.splitLine(cleanHtml);
				int position = 1;
				for (String word : words) {
					index.add(word, url, position);
					position++;
				}
				count++;
			}
		}
	}

	public void addToMap(String link, Map<String, String> links)
			throws UnknownHostException, MalformedURLException, IOException {
		String html = HTTPFetcher.fetchHTML(link);
		logger.debug("URL {} added to the map", link);
		links.put(link, html);
	}

	private class URLMinion implements Runnable

	{

		private String link;
		private ThreadSafeInvertedIndex index;

		public URLMinion(String link, ThreadSafeInvertedIndex index) {
			logger.debug("******** Minion created for {}", link);
			this.link = link;
			this.index = index;
		}

		@Override
		public void run() {

		}
	}

}
