import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WordMap {

	private Map<String, ArrayList<WordLocation>> wordMap;

	public WordMap() {
		wordMap = new HashMap();
	}

	private boolean hasKey(String key) {
		if (wordMap.containsKey(key)) {
			return true;
		}
		return false;
	}

	private boolean hasFile(String key, String file) {
		ArrayList<WordLocation> myVal = wordMap.get(key);
		boolean found = false;
		for (int i = 0; i < myVal.size(); i++) {
			String fileName = myVal.get(i).fileName;
			if (fileName.equals(file)) {
				found = true;
				break;
			}
		}
		return found;
	}

	public void buildMap(String key, String fileName, int position) {
		if (hasKey(key) == false) {
			System.out.println("---- has no key ---- " + key);
			ArrayList<WordLocation> newVal = new ArrayList<WordLocation>();
			newVal.add(new WordLocation(fileName, position));
			wordMap.put(key, newVal);
		}
		else if (hasKey(key) && hasFile(key, fileName)) {
			System.out.println("---- has key ---- " + key
					+ " && ---- has file ----" + fileName);
			ArrayList<WordLocation> myVal = wordMap.get(key);

			for (int i = 0; i < myVal.size(); i++) {
				String file = myVal.get(i).fileName;
				System.out.println("fileName: " + file + "target fileName: "
						+ fileName + "  ==  " + file.equals(fileName));
				if (file.equals(fileName)) {
					myVal.get(i).positions.add(position);
				}
			}
		}
		else if (hasKey(key) && (hasFile(key, fileName) == false)) {
			System.out.println("---- has key ---- " + key
					+ " && ---- does not has file ----" + fileName);
			ArrayList<WordLocation> myVal = wordMap.get(key);
			myVal.add(new WordLocation(fileName, position));
		}

		sortArrayList();

	}

	public void printWordMap() {
		for (String key : wordMap.keySet()) {
			ArrayList<WordLocation> correspondingVal = wordMap.get(key);
			System.out.println("\n" + key);
			for (int i = 0; i < correspondingVal.size(); i++) {
				WordLocation myLocation = correspondingVal.get(i);
				System.out.println(myLocation);
			}
		}
	}

	public void writeWordMap(File outputPath) {
		try {
			FileWriter fw = new FileWriter(outputPath);
			BufferedWriter bw = new BufferedWriter(fw);

			for (String key : wordMap.keySet()) {
				ArrayList<WordLocation> correspondingVal = wordMap.get(key);
				// System.out.println("\n" + key);
				bw.write(key + "\n");
				for (int i = 0; i < correspondingVal.size(); i++) {
					WordLocation myLocation = correspondingVal.get(i);
					// System.out.println(myLocation);
					bw.write(myLocation.fileName);
					for (int position : myLocation.positions) {
						bw.write(", " + position);
					}
					bw.write("\n");

				}
				bw.write("\n");
			}
			bw.close();

		} catch (IOException e) {
			System.err.println("IOException!");
			e.printStackTrace();
		}
	}

	private void sortArrayList() {
		// sort arraylist by fileName
		for (String key : wordMap.keySet()) {
			ArrayList<WordLocation> myVal = wordMap.get(key);
			Collections.sort(myVal, WordLocation.FileNameComparator);
		}
	}
}
