import java.util.ArrayList;
import java.util.Comparator;

public class WordLocation {

	String fileName;
	ArrayList<Integer> positions = new ArrayList<Integer>();

	public WordLocation(String fileName, int position) {
		this.fileName = fileName;
		this.positions.add(position);
	}

	@Override
	public String toString() {
		String output = '"' + fileName + '"';
		for (int position : positions) {
			output += ", " + position;
			// System.out.println("%%& " + output);
		}
		output += "\n";
		// System.out.println("*** " + output);
		return output;
	}

	public static Comparator<WordLocation> FileNameComparator = new Comparator<WordLocation>() {

		@Override
		public int compare(WordLocation o1, WordLocation o2) {

			// return o1.fileName.compareToIgnoreCase(o2.fileName);
			return o2.fileName.compareToIgnoreCase(o1.fileName);

		}
	};
}
