// TODO Don't get lazy with the comments.

/**
 * Search Result for partial search
 */
public class SearchResult implements Comparable<SearchResult> {

	// TODO Private

	int frequency;
	int position;

	final String location; // TODO final

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

	/*
	 * TODO public void updateFrequency(int frequency) { // add this frequency
	 * to the old frequency }
	 *
	 * public void updatePosition(int position) { // only change if this is less
	 * than the old position }
	 *
	 * Make this more generalized, add some getters.
	 */
	public void updateFrequency(int frequency) {
		this.frequency += frequency;
	}

	public void updatePosition(int position) {
		this.position = position;
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
