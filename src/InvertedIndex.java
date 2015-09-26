import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

// TODO Rename this to InvertedIndexBuilder
public class InvertedIndex {

	// TODO Remove all members, use local variables where posisble
	private ArgumentParser parser;
	private WordMap wordMap = new WordMap();
	private int position;
	private ArrayList<File> fileList = new ArrayList<>();

	public void start(String[] args) {
		// get correct input;
		parser = new ArgumentParser(args);
		// String inputFile = parser.getValue(Driver.INPUT_FLAG);

		try {
			dirTraverse(new File(parser.getValue(Driver.INPUT_FLAG)));
		} catch (Exception e) {
			System.out.println("Exception caught in getting input");
			return;
		}

		if (parser.hasFlag(Driver.INDEX_FLAG)) {
			if (parser.getValue(Driver.INDEX_FLAG) == null) {
				printWordMap(new File(Driver.INDEX_DEFAULT));
			}
			else {
				printWordMap(new File(parser.getValue(Driver.INDEX_FLAG)));
			}
		}

	}

	// TODO public static void traverseDirectory(Path directory, InvertedIndex index)
	public void dirTraverse(File path) {
		// TODO Use Java NIO (versions 7 and 8), not File with is Java 6
		// TODO See lectures https://github.com/cs212/lectures/tree/fall2015/Files and Exceptions
		if (path.isDirectory()) {
			File[] subDir = path.listFiles();

			for (int i = 0; i < subDir.length; i++) {
				if (subDir[i].isHidden() == false) {
					dirTraverse(subDir[i]);
				}
			}
		}
		if (path.isFile()) {
			if (path.getName().toLowerCase().endsWith(".txt")) {

				fileList.add(path);
				// System.out.println("TXT File Found ---- " + path.getName());
				position = 1;
				bufferedReadLine(path);
			}
		}

	}
	
	// TODO No exception handling in private methods, let the public methods handle it

	// read input by bufferedReader
	private void bufferedReadLine(File dir) {
		// TODO int position = 1 (as a local variable)
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dir));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] words = splitLine(line);
				for (String word : words) {
					wordMap.add(word, dir.getPath(), position);
					position++;
				}

				//
			}
		} catch (FileNotFoundException e) {
			System.out.println("#### File not found ####");
			// e.printStackTrace();
		} catch (IOException e) {
			System.out.println("#### IOEception ####");
			// e.printStackTrace();
		}

	}

	// TODO public static String[] split(String line)...
	private String[] splitLine(String str) {
		String[] words = null;
		str = clean(str);

		if (str.length() != 0) {
			words = str.split(SPLIT_REGEX);
		}
		else {
			words = new String[] {};
		}
		return words;
	}

	// TODO Same as above
	private String clean(String str) {
		str = str.toLowerCase();
		str = str.replaceAll(CLEAN_REGEX, "");
		str = str.trim();
		return str;
	}

	private final String CLEAN_REGEX = "(?U)[^\\p{Alnum}\\p{Space}]+";

	private final String SPLIT_REGEX = "(?U)\\p{Space}+";

	// TODO Remove these methods after this point
	public ArrayList<File> getFileLists() {
		Collections.sort(fileList);
		return (ArrayList<File>) Collections.unmodifiableList(fileList);
	}

	public void printWordMap(File output) {
		wordMap.printWordMap(output.toPath());
	}

}
