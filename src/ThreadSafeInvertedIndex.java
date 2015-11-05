import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ThreadSafeInvertedIndex extends InvertedIndex {

	private ReadWriteLock lock;

	public ThreadSafeInvertedIndex() {
		super();
	}

	@Override
	public synchronized void add(String word, String path, int position) {
		super.add(word, path, position);
	}

	@Override
	public synchronized boolean hasWord(String word) {
		return super.hasWord(word);
	}

	@Override
	public synchronized boolean hasPath(String word, String path) {
		return super.hasPath(word, path);
	}

	@Override
	public synchronized boolean hasPosition(String word, String path,
			int position) {
		return super.hasPosition(word, path, position);
	}

	@Override
	public synchronized void print(Path output) throws IOException {
		super.print(output);
	}

	@Override
	public synchronized List<SearchResult> partialSearch(String[] queryWords) {
		return super.partialSearch(queryWords);
	}

	@Override
	public synchronized String toString() {
		return super.toString();
	}
}
