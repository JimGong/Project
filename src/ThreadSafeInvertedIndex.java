import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

// TODO A little bit of Javadoc, you do not need to javadoc the overridden methods.

/**
 * TODO
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	/** TODO */
	private ReadWriteLock lock;

	/** TODO */
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
	public void merge(
			TreeMap<String, TreeMap<String, TreeSet<Integer>>> localIndex) {
		lock.lockReadWrite();
		try {
			super.merge(localIndex);
		} finally {
			lock.unlockReadWrite();
		}
	}
}
