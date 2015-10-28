import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Traverse the directory and build inverted index map
 */
public class InvertedIndexBuilder {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * Traverse the directory to build inverted index map
	 *
	 * @param directory
	 * @param index
	 */
	public static void traverseDirectory(Path directory, InvertedIndex index) {
		try {
			if (Files.isDirectory(directory)) {

				traverse(directory, index);

			}
			else {
				if (directory.getFileName().toString().toLowerCase()
						.endsWith(".txt")) {

					// parseFile(directory, index);
					parseFileThread thread = new parseFileThread(directory,
							index);
					thread.start();
					try {
						thread.join();
					} catch (InterruptedException e) {
						logger.debug(e.getMessage(), e);
					}

				}

			}
		} catch (IOException e) {
			System.out.println("Input file NOT found");
		}
	}

	/**
	 * Helper method for traverseDirectory. If path is a directory then keep
	 * going in. Else, check if it is a txt file.
	 *
	 * @param path
	 * @param index
	 * @throws IOException
	 */
	private static void traverse(Path path, InvertedIndex index)
			throws IOException {

		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {

			for (Path file : listing) {

				if (Files.isDirectory(file)) {
					traverseDirectory(file, index);
				}
				else {
					if (file.getFileName().toString().toLowerCase()
							.endsWith(".txt")) {
						// parseFile(file, index);
						parseFileThread thread = new parseFileThread(file,
								index);
						thread.start();
						try {
							thread.join();
						} catch (InterruptedException e) {
							logger.debug(e.getMessage(), e);
						}
					}
				}
			}
		}
	}

	/**
	 * Use bufferedReader to read line from a path
	 *
	 * @param dir
	 * @param index
	 * @throws IOException
	 */
	public static void parseFile(Path file, InvertedIndex index)
			throws IOException {

		int position = 1;

		try (BufferedReader reader = Files.newBufferedReader(file,
				Charset.forName("UTF-8"))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] words = splitLine(line);
				for (String word : words) {
					index.add(word, file.toFile().getPath(), position);
					position++;
				}

			}
		}
	}

	private static class parseFileThread extends Thread {

		private final Path file;
		private final InvertedIndex index;

		public parseFileThread(Path file, InvertedIndex index) {
			this.file = file;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				InvertedIndexBuilder.parseFile(file, index);
			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
			}
		}
	}

	/**
	 * First cleans text. If the result is non-empty, splits the cleaned text
	 * into words by whitespace. The result will be an array of words in all
	 * lowercase without any special characters, or an empty array if the
	 * cleaned text was empty.
	 *
	 * @param text
	 *            input to clean and split into words
	 * @return array of words (or an empty array if cleaned text is empty)
	 *
	 * @see #clean(String)
	 * @see #SPLIT_REGEX
	 */
	public static String[] splitLine(String text) {
		String[] words = null;
		text = clean(text);

		if (text.length() != 0) {
			words = text.split(SPLIT_REGEX);
		}
		else {
			words = new String[] {};
		}
		return words;
	}

	/**
	 * Cleans a word by converting it to lowercase and removing any whitespace
	 * at the start or end of the word.
	 *
	 * @param word
	 *            word to clean
	 * @return cleaned word
	 */
	public static String clean(String str) {
		str = str.toLowerCase();
		str = str.replaceAll(CLEAN_REGEX, "");
		str = str.trim();
		return str;
	}

	/** Regular expression for removing special characters. */
	private static final String CLEAN_REGEX = "(?U)[^\\p{Alnum}\\p{Space}]+";

	/** Regular expression for splitting text into words by whitespace. */
	private static final String SPLIT_REGEX = "(?U)\\p{Space}+";

}
