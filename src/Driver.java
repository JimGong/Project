import java.util.Arrays;

/**
 * This software driver class provides a consistent entry point for the search
 * engine. Based on the arguments provided to {@link #main(String[])}, it
 * creates the necessary objects and calls the necessary methods to build an
 * inverted index, process search queries, configure multithreading, and launch
 * a web server (if appropriate).
 */
public class Driver {

    /**
     * Flag used to indicate the following value is an input directory of text
     * files to use when building the inverted index.
     * 
     * @see "Projects 1 to 5"
     */
    public static final String INPUT_FLAG = "-input";

    /**
     * Flag used to indicate the following value is the path to use when
     * outputting the inverted index to a JSON file. If no value is provided,
     * then {@link #INDEX_DEFAULT} should be used. If this flag is not provided,
     * then the inverted index should not be output to a file.
     * 
     * @see "Projects 1 to 5"
     */
    public static final String INDEX_FLAG = "-index";

    /**
     * Flag used to indicate the following value is a text file of search
     * queries.
     * 
     * @see "Projects 2 to 5"
     */
    public static final String QUERIES_FLAG = "-query";

    /**
     * Flag used to indicate the following value is the path to use when
     * outputting the search results to a JSON file. If no value is provided,
     * then {@link #RESULTS_DEFAULT} should be used. If this flag is not
     * provided, then the search results should not be output to a file.
     * 
     * @see "Projects 2 to 5"
     */
    public static final String RESULTS_FLAG = "-results";

    /**
     * Flag used to indicate the following value is the number of threads to use
     * when configuring multithreading. If no value is provided, then
     * {@link #THREAD_DEFAULT} should be used. If this flag is not provided,
     * then multithreading should NOT be used.
     * 
     * @see "Projects 3 to 5"
     */
    public static final String THREAD_FLAG = "-threads";

    /**
     * Flag used to indicate the following value is the seed URL to use when
     * building the inverted index.
     * 
     * @see "Projects 4 to 5"
     */
    public static final String SEED_FLAG = "-seed";

    /**
     * Flag used to indicate the following value is the port number to use when
     * starting a web server. If no value is provided, then
     * {@link #PORT_DEFAULT} should be used. If this flag is not provided, then
     * a web server should not be started.
     */
    public static final String PORT_FLAG = "-port";

    /**
     * Default to use when the value for the {@link #INDEX_FLAG} is missing.
     */
    public static final String INDEX_DEFAULT = "index.json";

    /**
     * Default to use when the value for the {@link #RESULTS_FLAG} is missing.
     */
    public static final String RESULTS_DEFAULT = "results.json";

    /**
     * Default to use when the value for the {@link #THREAD_FLAG} is missing.
     */
    public static final int THREAD_DEFAULT = 5;
    
    /**
     * Default to use when the value for the {@link #PORT_FLAG} is missing.
     */
    public static final int PORT_DEFAULT = 8080;

    /**
     * Parses the provided arguments and, if appropriate, will build an inverted
     * index from a directory or seed URL, process search queries, configure
     * multithreading, and launch a web server.
     * 
     * @param args
     *            set of flag and value pairs
     */
    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));

        // TODO Fill this in and add additional methods and classes as
        // necessary.
    }
}
