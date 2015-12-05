import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebCrawler {

	private HashSet<String> urlSet;
	private ThreadSafeInvertedIndex index;
	private final WorkQueue minions;
	private static final Logger logger = LogManager.getLogger();
	private final int MAX_CAPACITY = 50;
	private final ReadWriteLock lock;

	public WebCrawler(int numThreads, ThreadSafeInvertedIndex index) {
		urlSet = new HashSet<String>();
		lock = new ReadWriteLock();
		minions = new WorkQueue(numThreads);
		this.index = index;
	}

	public void traverse(String url) throws MalformedURLException {

		if ((urlSet.size() < MAX_CAPACITY) && (!urlSet.contains(url))) {
			minions.execute(new CrawlMinion(url, urlSet));
		}
		finish();
		logger.debug("total size:" + urlSet.size());
	}

	private class CrawlMinion implements Runnable {

		private String link;
		private HashSet<String> urlSet;

		public CrawlMinion(String link, HashSet<String> urlSet) {
			logger.debug("******** Minion created for {}", link);
			this.link = link;
			this.urlSet = urlSet;
		}

		@Override
		public void run() {
			logger.debug("urlsize: {}, {}", urlSet.size(),
					urlSet.size() < MAX_CAPACITY);
			if (urlSet.size() >= MAX_CAPACITY) {
				return;
			}

			try {

				lock.lockReadWrite();
				urlSet.add(link);
				logger.debug("size {}, {} added", urlSet.size(), link);
				lock.unlockReadWrite();

				/* get html from the link */
				String html = HTTPFetcher.fetchHTML(link);

				/*
				 * find url add to the ArrayList if urlSet size smaller than 50
				 */
				lock.lockReadWrite();
				ArrayList<String> innerURLs = LinkParser
						.listLinks(new URL(link), html);

				for (int i = 0; (i < innerURLs.size())
						&& (urlSet.size() < MAX_CAPACITY); i++) {
					String innerAbsoluteLink = innerURLs.get(i);
					minions.execute(new CrawlMinion(innerAbsoluteLink, urlSet));
				}
				lock.unlockReadWrite();

				html = HTMLCleaner.cleanHTML(html);
				String[] words = InvertedIndexBuilder
						.splitLine(html); /* split html into words */
				int position = 1;

				InvertedIndex local = new InvertedIndex();

				/* parse words into inverted index one at a time */
				for (String word : words) {
					local.add(word, link, position);
					position++;
				}

				index.addAll(local);

			} catch (IOException e) {
				System.err.println("invalid link......");
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
