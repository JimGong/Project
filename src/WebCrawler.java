import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebCrawler {

	private LinkedHashMap<String, String> links;
	private ThreadSafeInvertedIndex index;
	private final WorkQueue minions;
	private static final Logger logger = LogManager.getLogger();
	private final int MAX_CAPACITY = 50;
	private final ReadWriteLock lock;

	public WebCrawler(int numThreads, ThreadSafeInvertedIndex index) {

		links = new LinkedHashMap<String, String>();
		/* key is URL, value is the HTML page */
		lock = new ReadWriteLock();
		minions = new WorkQueue(numThreads);
		this.index = index;
	}

	public void traverse(String url) throws MalformedURLException {
		System.out.println("----------" + links.size() + " " + url);
		lock.lockReadOnly();
		int size = links.size();
		lock.unlockReadOnly();
		if ((size < MAX_CAPACITY)) {
			System.out.println(links.size() + " " + url);
			links.put(url, "");

			minions.execute(new CrawlMinion(url, links));
			finish();

			logger.debug("finding innerUrl in link {}, html is empty {}", url,
					links.get(url).isEmpty());

			ArrayList<String> innerURLs = LinkParser.listLinks(links.get(url));
			logger.debug("inner url size {}", innerURLs.size());
			if (!innerURLs.isEmpty()) {

				for (int i = 0; (i < innerURLs.size())
						&& (links.size() < MAX_CAPACITY); i++) {
					String innerLink = innerURLs.get(i);
					URL base = new URL(url);
					URL absoluteURL = new URL(base, innerLink);
					String absoluteLink = absoluteURL.toString();

					if ((!links.containsKey(absoluteLink))
							&& (!absoluteLink.contains("#"))) {
						lock.lockReadOnly();
						size = links.size();
						lock.unlockReadOnly();
						if (size < MAX_CAPACITY) {
							traverse(absoluteLink);
						}
						else {
							System.out.println("hit the max capacity, Done");
							return;
						}
					}
				}
			}
		}
	}

	private class CrawlMinion implements Runnable {

		private String link;
		private LinkedHashMap<String, String> links;

		public CrawlMinion(String link, LinkedHashMap<String, String> links) {
			logger.debug("******** Minion created for {}", link);
			this.link = link;
			this.links = links;
		}

		@Override
		public void run() {
			try {
				// logger.debug("getting html for {}", link);
				String html = HTTPFetcher.fetchHTML(link);
				logger.debug("the html is empty {}", html.isEmpty());
				// logger.debug("URL {} added to the map", link);
				lock.lockReadWrite();
				links.put(link, html);
				lock.unlockReadWrite();
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

	public void addToIndex() {
		logger.debug("adding result to index");
		finish();
		int count = 0;
		for (String url : links.keySet()) {
			if (count < MAX_CAPACITY) {
				logger.debug("count {} add url {} into index", count, url);
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
		logger.debug("done with adding");
	}
}
