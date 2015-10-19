// TODO Don't get lazy with the comments.

/**
 * A custom class for storing basic search information. It will sort first
 * according to its frequency, then position and location.
 */
public class SearchResult implements Comparable<SearchResult> {

	/** count */
	private int frequency;
	/** index */
	private int position;
	/** where */
	private final String location;

	/**
	 * Constructor
	 *
	 * @param frequency
	 * @param position
	 * @param location
	 */
	public SearchResult(int frequency, int position, String location) {
		super();
		this.frequency = frequency;
		this.position = position;
		this.location = location;
	}

	public void updateFrequency(int frequency) {
		this.frequency += frequency;
	}

	public void updatePosition(int position) {
		if (this.position > position) {
			this.position = position;
		}
	}

	public int getFrequency() {
		return frequency;
	}

	public int getPosition() {
		return position;
	}

	public String getLocation() {
		return location;
	}

	/**
	 * override compareTo method
	 */
	@Override
	public int compareTo(SearchResult o) {

		if (this.frequency != o.frequency) {
			return Integer.compare(o.frequency, this.frequency);
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
