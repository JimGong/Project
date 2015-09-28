import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Traverse the directory and build inverted index map
 */
public class InvertedIndexBuilder {

	/**
	 * Traverse the directory to build inverted index map
	 * 
	 * @param directory
	 * @param index
	 */
	public static void traverseDirectory(Path directory, InvertedIndex index) {
		if (Files.isDirectory(directory)) {
			try {
				traverse(directory, index);
			} catch (IOException e) {
				System.err.println("Input file NOT found");
			}
		}
		else {
			if (directory.getFileName().toString().toLowerCase()
					.endsWith(".txt")) {

				try {
					bufferedReadLine(directory, index);
				} catch (IOException e) {
					System.out.println("Input file NOT found");
				}
			}

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

		DirectoryStream<Path> listing = Files.newDirectoryStream(path);

		for (Path file : listing) {

			if (Files.isDirectory(file)) {
				traverseDirectory(file, index);
			}
			else {
				if (file.getFileName().toString().toLowerCase()
						.endsWith(".txt")) {
					// System.out
					// .println("File: " + file.getFileName() + " found");
					bufferedReadLine(file, index);
				}
			}
		}
		listing.close();
	}

	/**
	 * Use bufferedReader to read line from a path
	 *
	 * @param dir
	 * @param index
	 * @throws IOException
	 */
	private static void bufferedReadLine(Path dir, InvertedIndex index)
			throws IOException {

		int position = 1;
		BufferedReader br = null;

		br = new BufferedReader(new FileReader(dir.toFile()));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] words = splitLine(line);
			for (String word : words) {
				index.add(word, dir.toFile().getPath(), position);
				position++;
			}

		}
		br.close();

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
	private static String[] splitLine(String text) {
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
	private static String clean(String str) {
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
