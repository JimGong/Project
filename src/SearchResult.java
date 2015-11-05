
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
	 * Initializes this search result.
	 *
	 * @param frequency
	 *            number of times a query was found
	 * @param position
	 *            earliest position a query was found
	 * @param location
	 *            location where a query was found
	 */
	public SearchResult(int frequency, int position, String location) {
		super();
		this.frequency = frequency;
		this.position = position;
		this.location = location;
	}

	/**
	 * Update the frequency when new word found
	 *
	 * @param frequency
	 *            new frequency found in the file
	 */
	public void updateFrequency(int frequency) {
		this.frequency += frequency;
	}

	/**
	 * Update the position when new first occurrence position is smaller than
	 * the current one.
	 *
	 * @param position
	 *            new position find in the file
	 */
	public void updatePosition(int position) {
		if (this.position > position) {
			this.position = position;
		}
	}

	/**
	 * Get the frequency of the current search result
	 *
	 * @return frequency
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Get the position of the current search result
	 *
	 * @return
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Get the location of the current search result
	 *
	 * @return
	 */
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
