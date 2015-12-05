import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebCrawler {

	// private LinkedHashSet<String> links;
	private ArrayList<String> links; // TODO Should be able to use a hashset!
	
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

	/* TODO Different approach
	public void traverse(String url) {
		lock.lockReadWrite();
		
		if (set.size() < 50 && !set.contains(url)) {
			set.add(url);
			minions.execute(new worker...)
		}
		
		lock.unlock...
	}
	*/

	public void traverse(String url) throws MalformedURLException {
		logger.debug("adding {} into links", url);
		links.add(url); // TODO Unprotected access, which only works because you are basically single-threading

		// TODO Super inefficient, basically single-threading this
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
				
				/* TODO
				Lock once
				
				Loop through links... for all unique links create a new worker

				Unlock
				*/
				
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

				// TODO Add to local first so you don't cause constant locking

				/* parse words into inverted index one at a time */
				for (String word : words) {
					index.add(word, link, position);
					position++;
				}
				
				// TODO Addall to main index
				
				logger.debug("passed all words for this {} into index", link);

			} catch (IOException e) {
				// TODO Stack trace!
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
