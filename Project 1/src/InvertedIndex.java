import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class InvertedIndex {

	WordMap wordMap = new WordMap();
	int position = 1;

	public boolean isTxtFile(String fileName) {
		if (fileName.endsWith(".txt")) {
			return true;
		}
		else {
			return false;
		}

	}

	public String readline(File dir) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dir));
			String line = null;

			while ((line = br.readLine()) != null) {
				// System.out.println("**** " + line);
				// System.out.println("\n#### split it into words ####\n");
				String[] words = splitLine(line);
				for (int i = 0; i < words.length; i++) {
					wordMap.buildMap(words[i], dir.getName(), position);
					position++;
				}

			}

		} catch (FileNotFoundException e) {
			System.err.println("File not Found!");
			System.exit(0);
		} catch (IOException e) {
			System.err.println("IOEception!");
			e.printStackTrace();
			System.exit(0);
		}

		return "";
	}

	public String[] splitLine(String line) {
		String[] words = line.split(" ");

		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].trim();
			words[i] = words[i].toLowerCase();
		}
		// for (int i = 0; i < words.length; i++) {
		// System.out.println(words[i]);
		// }
		// System.out.println("#### done ####");
		return words;
	}

	public void dirTraverse(File dir) {

		System.out.println("\ncurrent at: " + dir.getPath());
		if (dir.isDirectory()) {
			File[] subDir = dir.listFiles();

			if (subDir.length > 0) {
				for (int i = 0; i < subDir.length; i++) {
				}
				for (int i = 0; (i < subDir.length); i++) {
					// ignore hidden
					if (subDir[i].isHidden() == false) {

						dirTraverse(subDir[i]);
					}
				}
			}
		}
		if (dir.isFile()) {
			if (isTxtFile(dir.getName())) {
				System.err.println("--------- " + dir.getName());
				// should read file
				readline(dir);
			}
		}

	}

	public void printWordMap() {
		System.err.println("\n---- Printing Map ----");
		wordMap.printWordMap();

		System.err.println("---- DONE ----");
	}

	public static void main(String args[]) {
		InvertedIndex i = new InvertedIndex();
		File dir = new File("/Users/JiaMinGong/Desktop/TestFolder");
		i.dirTraverse(dir);

		i.printWordMap();
	}

}
