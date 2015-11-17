import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiThreadInvertedIndexBuilder {

	private static final Logger logger = LogManager.getLogger();

	private final WorkQueue minions;

	public MultiThreadInvertedIndexBuilder(int numThreads) {
		minions = new WorkQueue(numThreads);
	}

	public void finish() {
		try {
			while (minions.getPending() > 0) {
				logger.debug("Waiting until finished");
				synchronized (minions) {
					minions.wait();
				}
				logger.debug("waiting");
			}
		} catch (InterruptedException e) {
			logger.debug("Finish interrupted", e);
		}
	}

	public void shutdown() {
		finish();
		logger.debug("Shutting down");
		minions.shutdown();
	}

	private class FileMinion implements Runnable {

		private Path file;
		private ThreadSafeInvertedIndex index;

		public FileMinion(Path file, ThreadSafeInvertedIndex index) {
			logger.debug("******** Minion created for {}", file);
			this.file = file;
			this.index = index;
			minions.increasementPending();
		}

		@Override
		public void run() {
			try {
				// TODO This will improve efficiency, do not over synchronize!
				InvertedIndex local = new InvertedIndex();
				InvertedIndexBuilder.parseFile(file, local);
				index.addAll(local);
			} catch (IOException e) {
				logger.warn("Unable to parse {}", file);
				logger.catching(Level.DEBUG, e);
			} finally {
				minions.decreasementPending();
				synchronized (minions) {
					if (minions.getPending() <= 0) {
						minions.notifyAll();
					}
				}
			}
			logger.debug("######## Minion finished {}", file);
		}
	}

	public void traverseDirectory(Path directory,
			ThreadSafeInvertedIndex index) {
		try {
			if (Files.isDirectory(directory)) {
				traverse(directory, index);
			}
			else {
				if (directory.getFileName().toString().toLowerCase()
						.endsWith(".txt")) {
					minions.execute(new FileMinion(directory, index));
				}
			}
		} catch (IOException e) {
			System.out.println("Input file NOT found");
		}
	}

	private void traverse(Path path, ThreadSafeInvertedIndex index)
			throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {

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
}
