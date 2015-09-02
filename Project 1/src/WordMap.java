import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class WordMap {

	private Map<String, ArrayList<WordLocation>> wordMap;

	public WordMap() {
		wordMap = new TreeMap();
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
			ArrayList<WordLocation> newVal = new ArrayList<WordLocation>();
			newVal.add(new WordLocation(fileName, position));
			wordMap.put(key, newVal);
		}
		else if (hasKey(key) && hasFile(key, fileName)) {
			ArrayList<WordLocation> myVal = wordMap.get(key);

			for (int i = 0; i < myVal.size(); i++) {
				String file = myVal.get(i).fileName;
				if (file.equals(fileName)) {
					myVal.get(i).positions.add(position);
				}
			}
		}
		else if (hasKey(key) && (hasFile(key, fileName) == false)) {
			ArrayList<WordLocation> myVal = wordMap.get(key);
			myVal.add(new WordLocation(fileName, position));
		}

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

}
