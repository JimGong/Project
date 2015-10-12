
public class SearchResult implements Comparable<SearchResult> {

	int frequency;
	int position;
	String location;

	public SearchResult(int frequency, int position, String location) {
		super();
		this.frequency = frequency;
		this.position = position;
		this.location = location;
	}

	@Override
	public int compareTo(SearchResult o) {

		if (this.frequency != o.frequency) {
			return Integer.compare(this.frequency, o.frequency);
		}
		else {
			if (this.position != o.position) {
				return Integer.compare(this.position, o.position);
			}
			else {
				return this.location.compareToIgnoreCase(o.location);
			}
		}
	}

	@Override
	public String toString() {
		return "\nwhere: " + location + "\ncount: " + frequency + "\nindex: "
				+ position;
	}
}
