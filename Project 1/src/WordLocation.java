import java.util.ArrayList;

public class WordLocation {

	String fileName;
	ArrayList<Integer> positions = new ArrayList<Integer>();

	public WordLocation(String fileName, int position) {
		this.fileName = fileName;
		this.positions.add(position);
	}

	@Override
	public String toString() {
		return fileName + ", " + positions;
	}

}
