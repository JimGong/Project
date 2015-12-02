import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebCrawler {

	// private LinkedHashSet<String> links;
	private ArrayList<String> links;
	private ThreadSafeInvertedIndex index;
	private final WorkQueue minions;
	private static final Logger logger = LogManager.getLogger();
	private final int MAX_CAPACITY = 50;
	private final ReadWriteLock lock;

	public WebCrawler(int numThreads, ThreadSafeInvertedIndex index) {

		links = new ArrayList<>();
		lock = new ReadWriteLock();
		minions = new WorkQueue(numThreads);
		this.index = index;
	}

	public void traverse(String url) throws MalformedURLException {
		logger.debug("adding {} into links", url);
		links.add(url);

		for (int i = 0; (i < MAX_CAPACITY) && (i < links.size()); i++) {
			String link = links.get(i);

			logger.debug("going to execute for link {}", link);
			minions.execute(new CrawlMinion(link, links));
			finish();
		}
		logger.debug("Done, links size: {}", links.size());
	}

	private class CrawlMinion implements Runnable {

		private String link;
		private ArrayList<String> links;

		public CrawlMinion(String link, ArrayList<String> links) {
			logger.debug("******** Minion created for {}", link);
			this.link = link;
			this.links = links;
		}

		@Override
		public void run() {
			try {
				/* get html all the time */
				String html = HTTPFetcher.fetchHTML(link);
				/*
				 * find all inner url, and convert to absolute url, add to the
				 * ArrayList if links size smaller than 50
				 */
				ArrayList<String> innerURLs = LinkParser.listLinks(html);
				// logger.debug("find {} innerURL in link {}", innerURLs.size(),
				// link);
				for (int i = 0; (i < innerURLs.size())
						&& (links.size() < MAX_CAPACITY); i++) {
					String innerURL = innerURLs.get(i);
					URL base = new URL(link);
					URL absolute = new URL(base, innerURL);
					logger.debug("link{}", absolute.toString());
					if ((!links.contains(absolute.toString()))
							&& (!absolute.toString().contains("#"))) {
						links.add(absolute.toString());
						logger.debug("added, {} ", absolute.toString());
					}
				}

				/* get clean html */
				// logger.debug("clean html");
				html = HTMLCleaner.cleanHTML(html);
				String[] words = InvertedIndexBuilder
						.splitLine(html); /* split html into words */
				// logger.debug("html splitted into words");
				int position = 1;

				/* parse words into inverted index one at a time */
				for (String word : words) {
					index.add(word, link, position);
					position++;
				}
				logger.debug("passed all words for this {} into index", link);

			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.debug("######## Minion finished {}", link);
		}
	}

	public void finish() {
		minions.finish();
	}

	public void shutdown() {
		finish();
		logger.debug("Shutting down");
		minions.shutdown();
	}

}
