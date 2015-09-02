import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class InvertedIndex {

	WordMap wordMap = new WordMap();
	int position = 1;

	File inputDir;
	File outputPath;

	public void start(String[] args) {
		boolean foundInputDir = false;
		boolean foundOutputPath = false;
		for (int i = 0; i < (args.length - 1); i++) {
			System.out.println("i: " + i + " -- " + args[i]);
			if (args[i].equals("-d")) {
				System.out.println("-d found");
				inputDir = new File(args[i + 1]);
				foundInputDir = true;
			}
			if (args[i].equals("-i")) {
				outputPath = new File(args[i + 1]);
				foundOutputPath = true;
			}
		}

		if (foundInputDir == false) {
			System.out.println("You should input a directory! Try it again.");
			System.exit(0);
		}
		if (foundOutputPath == false) {
			outputPath = new File("index.txt");
		}
		System.out.println("input Dir: " + inputDir.getPath());
		System.out.println("output Path: " + outputPath.getPath());

		dirTraverse(inputDir);
		writeOutput(outputPath);
	}

	private boolean isTxtFile(String fileName) {
		if (fileName.endsWith(".txt")) {
			return true;
		}
		else {
			return false;
		}

	}

	private String readline(File dir) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dir));
			String line = null;

			while ((line = br.readLine()) != null) {
				// System.out.println("**** " + line);
				// System.out.println("\n#### split it into words ####\n");
				String[] words = splitLine(line);
				for (int i = 0; i < words.length; i++) {
					wordMap.buildMap(words[i], dir.getPath(), position);
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

	private String[] splitLine(String line) {
		String[] words = line.split(" ");

		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].trim();
			words[i] = words[i].toLowerCase();
			words[i] = words[i].replaceAll("\\s", " ");
			words[i] = words[i].replaceAll("\\W", "");
		}
		// for (int i = 0; i < words.length; i++) {
		// System.out.println(words[i]);
		// }
		// System.out.println("#### done ####");
		return words;
	}

	private void dirTraverse(File dir) {

		// System.out.println("\ncurrent at: " + dir.getPath());
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
				// System.err.println("--------- " + dir.getName());
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

	private void writeOutput(File outputPath) {
		wordMap.writeWordMap(outputPath);
		System.err.println("---- Output Written ----");
	}

}
