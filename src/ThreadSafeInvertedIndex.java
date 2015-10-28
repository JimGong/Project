import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ThreadSafeInvertedIndex extends InvertedIndex {

	private ReadWriteLock lock;

	public ThreadSafeInvertedIndex() {
		super();
	}

	@Override
	public void add(String word, String path, int position) {
		synchronized (this) {
			super.add(word, path, position);

		}

	}

	@Override
	public boolean hasWord(String word) {
		synchronized (this) {
			return super.hasWord(word);

		}

	}

	@Override
	public boolean hasPath(String word, String path) {
		synchronized (this) {
			return super.hasPath(word, path);

		}

	}

	@Override
	public boolean hasPosition(String word, String path, int position) {
		synchronized (this) {
			return super.hasPosition(word, path, position);

		}

	}

	@Override
	public void print(Path output) throws IOException {
		synchronized (this) {
			super.print(output);

		}

	}

	@Override
	public List<SearchResult> partialSearch(String[] queryWords) {
		synchronized (this) {
			return super.partialSearch(queryWords);

		}

	}

	@Override
	public String toString() {
		synchronized (this) {
			return super.toString();

		}

	}
}
