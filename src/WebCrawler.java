import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebCrawler {

	/**
	 * Create a HashSet of URL String
	 */
	private HashSet<String> urlSet;
	/**
	 * Create ThreadSafeInverted index
	 */
	private ThreadSafeInvertedIndex index;
	/**
	 * Create a WorkQueue
	 */
	private final WorkQueue minions;
	private static final Logger logger = LogManager.getLogger();
	/**
	 * Set the max capacity of the url set
	 */
	private final int MAX_CAPACITY = 50;
	/**
	 * Create a read-write lock
	 */
	private final ReadWriteLock lock;

	/**
	 * Initiate the WorkQueue and set the numThread
	 *
	 * @param numThreads
	 * @param index
	 */
	public WebCrawler(int numThreads, ThreadSafeInvertedIndex index) {
		urlSet = new HashSet<String>();
		lock = new ReadWriteLock();
		minions = new WorkQueue(numThreads);
		this.index = index;
	}

	/**
	 * Traverse the URL to build inverted index map
	 *
	 * @param url
	 * @throws MalformedURLException
	 */
	public void traverse(String url) throws MalformedURLException {
		lock.lockReadWrite();
		urlHelper(url);
		lock.unlockReadWrite();

		finish();
		logger.debug("total size:" + urlSet.size());
	}

	/**
	 * Add to the url set and execute the minion for the url passed in
	 *
	 * @param url
	 */
	private void urlHelper(String url) {
		if ((urlSet.size() < MAX_CAPACITY) && (!urlSet.contains(url))) {
			urlSet.add(url);
			minions.execute(new CrawlMinion(url));
		}
	}

	/**
	 * Handles pre-url parsing. If a url is encountered, a new
	 * {@link CrawlMinion} is created to handle that url
	 *
	 *
	 */
	private class CrawlMinion implements Runnable {

		private String link;

		public CrawlMinion(String link) {
			logger.debug("******** Minion created for {}", link);
			this.link = link;

		}

		@Override
		public void run() {
			logger.debug("urlsize: {}, {}", urlSet.size(),
					urlSet.size() < MAX_CAPACITY);

			try {

				/* get html from the link */
				String html = HTTPFetcher.fetchHTML(link);
				String body = HTMLCleaner.cleanHTML(html);
				String title = getTitle(html);

				/*
				 * find url add to the ArrayList if urlSet size smaller than 50
				 */
				lock.lockReadWrite();
				ArrayList<String> innerURLs = LinkParser
						.listLinks(new URL(link), html);

				for (int i = 0; (i < innerURLs.size())
						&& (urlSet.size() < MAX_CAPACITY); i++) {
					String innerAbsoluteLink = innerURLs.get(i);
					urlHelper(innerAbsoluteLink);
				}
				lock.unlockReadWrite();

				html = HTMLCleaner.cleanHTML(html);

				/* page snipper */
				BufferedReader reader = new BufferedReader(
						new StringReader(body));
				StringBuffer firstSentense = new StringBuffer();
				String line;
				while (((line = reader.readLine()) != null)
						&& (firstSentense.length() < 250)) {
					line = line.trim();
					if (!line.isEmpty()) {
						if (line.contains("?") || line.contains(":")
								|| line.contains(";") || line.contains(".")) {
							int index = line.indexOf(".");
							if (index == -1) {
								index = line.indexOf(":");
							}
							else if (index == -1) {
								index = line.indexOf("?");
							}
							else if (index == -1) {
								index = line.indexOf(";");
							}
							if (index >= 0) {
								line = line.substring(0, index);
								firstSentense.append(line.trim() + "<br>");
							}
							break;
						}
						else {
							firstSentense.append(line.trim() + "<br>");
						}
					}
				}

				boolean urlExisted = LoginBaseServlet.dbhandler
						.urlExisted(link);
				if (!urlExisted) {
					LoginBaseServlet.dbhandler.addURL(link, title,
							firstSentense.toString().trim());
				}
				else {
					LoginBaseServlet.dbhandler.updateSnippet(line,
							firstSentense.toString().trim());
				}
				/* page snipper */

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

	private String getTitle(String dirtyHTML) {
		dirtyHTML = dirtyHTML.replaceAll("\\s+", " ");
		Pattern p = Pattern.compile("<title>(.*?)</title>");
		Matcher m = p.matcher(dirtyHTML);
		while (m.find() == true) {
			// System.out.println("$$$$$$$$$$$$ " + m.group(1));
			return m.group(1);
		}
		return "";

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
