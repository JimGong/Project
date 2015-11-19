import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Create a thread-safe version of inverted index to store word, path and
 * position
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/**
	 * Create a custom read write lock
	 */
	private ReadWriteLock lock;
	private static final Logger logger = LogManager.getLogger();

	/**
	 * Constructor and initialize the custom read write lock
	 */
	public ThreadSafeInvertedIndex() {
		super();
		lock = new ReadWriteLock();
	}

	@Override
	public void add(String word, String path, int position) {
		lock.lockReadWrite();
		try {
			super.add(word, path, position);
		} finally {
			lock.unlockReadWrite();
		}
	}

	@Override
	public boolean hasWord(String word) {
		lock.lockReadOnly();
		try {
			return super.hasWord(word);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public boolean hasPath(String word, String path) {
		lock.lockReadOnly();
		try {
			return super.hasPath(word, path);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public boolean hasPosition(String word, String path, int position) {
		lock.lockReadOnly();
		try {
			return super.hasPosition(word, path, position);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public void print(Path output) throws IOException {
		lock.lockReadOnly();
		try {
			super.print(output);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public List<SearchResult> partialSearch(String[] queryWords) {
		lock.lockReadOnly();
		try {
			return super.partialSearch(queryWords);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public String toString() {
		lock.lockReadOnly();
		try {
			return super.toString();
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public void addAll(InvertedIndex local) {
		logger.debug("going lock");
		lock.lockReadWrite();
		logger.debug("locked");
		try {
			super.addAll(local);

		} finally {
			logger.debug("going to unlock");
			lock.unlockReadWrite();
		}
	}

}
