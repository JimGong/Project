import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class InvertedIndex {

	private WordMap wordMap = new WordMap();
	private int position = 1;

	public void dirTraverse(File path) {

		if (path.isDirectory()) {
			File[] subDir = path.listFiles();

			for (int i = 0; i < subDir.length; i++) {
				if (subDir[i].isHidden() == false) {
					dirTraverse(subDir[i]);
				}
			}
		}
		if (path.isFile()) {
			if (path.getName().endsWith(".txt")) {
				System.out.println("TXT File Found ---- " + path.getName());
				bufferedReadLine(path);
			}
		}

	}

	// read input by bufferedReader
	private void bufferedReadLine(File dir) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dir));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] words = splitLine(line);
				for (String word : words) {
					wordMap.add(word, dir.getName(), position);
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

	private String clean(String str) {
		str = str.toLowerCase();
		str = str.replaceAll(CLEAN_REGEX, "");
		str = str.trim();
		return str;
	}

	private final String CLEAN_REGEX = "(?U)[^\\p{Alnum}\\p{Space}]+";

	private final String SPLIT_REGEX = "(?U)\\p{Space}+";

	public void printWordMap(Path output) {
		wordMap.printWordMap(output);
	}

}
