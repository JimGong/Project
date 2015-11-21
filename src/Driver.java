import java.io.IOException;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

		/*
		 * InvertedIndex index = null; PartialSearchBuilderInterface builder =
		 * null;
		 *
		 * if (multithreading) { ThreadSafeInvertedIndex threadSafe = new
		 * ThreadSafeInvertedIndex(); index = threadSafe;
		 *
		 * builder = new MultiThreaded....
		 *
		 * make sure things get shutdown } else { index = new InvertedIndex();
		 * etc. }
		 *
		 * if (print index) { index.print(); }
		 *
		 * if (print search) { search.print(); }
		 */

		Logger logger = LogManager.getLogger();
		ArgumentParser parser = new ArgumentParser(args);
		/**
		 * start of new version
		 */

		InvertedIndex index = null;
		PartialSearchBuilderInterface search = null;
		MultiThreadInvertedIndexBuilder multiThreadInvertedIndexBuilder = null;
		if (!parser.hasFlag(THREAD_FLAG)) {
			index = new InvertedIndex();
			search = new PartialSearchBuilder(index);
		} /* single thread version */
		else {
			ThreadSafeInvertedIndex threadSafeIndex = new ThreadSafeInvertedIndex();
			index = threadSafeIndex;

			int numThreads = THREAD_DEFAULT;;
			try {
				if (parser.hasFlag(THREAD_FLAG)) {
					numThreads = Integer.parseInt(parser.getValue(THREAD_FLAG));
					if (numThreads <= 0) {
						numThreads = THREAD_DEFAULT;
					}
				}
			} catch (NumberFormatException e) {
				System.err.println("Wrong number of thread.");
			}
			multiThreadInvertedIndexBuilder = new MultiThreadInvertedIndexBuilder(
					numThreads);
			search = new ThreadSafePartialSearchBuilder(numThreads, index);
		} /* multi thread version */

		/* build inverted index single thread version */
		try {
			logger.debug("traversing directory");
			if (!parser.hasFlag(THREAD_FLAG)) {
				InvertedIndexBuilder.traverseDirectory(
						Paths.get((parser.getValue(INPUT_FLAG))), index);
			}
			else {
				multiThreadInvertedIndexBuilder.traverseDirectory(
						Paths.get((parser.getValue(INPUT_FLAG))),
						(ThreadSafeInvertedIndex) index);
				logger.debug("calling shut down");
				logger.debug("index work queue shutted down");
				multiThreadInvertedIndexBuilder.shutdown();
			}
			logger.debug("Done with traverseDirectory");
		} catch (Exception e) {
			System.err.println("No arguments");
			// e.printStackTrace();
		} /* build inverted index */

		try {
			logger.debug("going to print index");
			if (parser.hasFlag(INDEX_FLAG)) {
				logger.debug("has index flag");
				if (!parser.hasValue(INDEX_FLAG)) {
					index.print(Paths.get(INDEX_DEFAULT));
				}
				else {
					index.print(Paths.get(parser.getValue(INDEX_FLAG)));
				}
			}
			else {
				logger.debug("doesnt has index flag");
			}
		} catch (IOException e) {
			System.err.println("No file can be printed. Try it again");
		} /* print inverted index */

		/* partial search */
		try {
			if (parser.hasFlag(QUERIES_FLAG) && parser.hasValue(QUERIES_FLAG)) {
				search.parseFile(Paths.get(parser.getValue(QUERIES_FLAG)));
			}
			logger.debug("Done with parsing queries");

			if (parser.hasFlag(THREAD_FLAG)) {
				((ThreadSafePartialSearchBuilder) search).shutdown();
			} /* shut down */
		} catch (IOException e) {
			System.err.println("No queries file found");
		} /* partial search */

		try {
			if (parser.hasFlag(RESULTS_FLAG)) {
				logger.debug("try to print search result");
				if (parser.hasValue(RESULTS_FLAG)) {
					search.print(Paths.get(parser.getValue(RESULTS_FLAG)));
				}
				else {
					search.print(Paths.get(RESULTS_DEFAULT));
				}
			}
		} catch (IOException e) {
			System.out.println("No output file for search");
		} /* print partial search */
		/**
		 * end of new version
		 */

		// /**
		// * start of old version
		// */
		// if (!parser.hasFlag(THREAD_FLAG)) {
		//
		// InvertedIndex index = new InvertedIndex();
		//
		// PartialSearchBuilder search = new PartialSearchBuilder(index);
		//
		// try {
		// InvertedIndexBuilder.traverseDirectory(
		// Paths.get((parser.getValue(INPUT_FLAG))), index);
		// } catch (Exception e) {
		// System.err.println("No arguments");
		// }
		// try {
		// if (parser.hasFlag(Driver.INDEX_FLAG)) {
		// if (parser.getValue(Driver.INDEX_FLAG) == null) {
		// index.print(Paths.get(INDEX_DEFAULT));
		// }
		// else {
		// index.print(Paths.get(parser.getValue(INDEX_FLAG)));
		// }
		// }
		// } catch (IOException e) {
		// System.err.println("No file can be printed. Try it again");
		// }
		// // project 2 partial search
		// try {
		// if (parser.hasFlag(QUERIES_FLAG)
		// && parser.hasValue(QUERIES_FLAG)) {
		// search.parseFile(Paths.get(parser.getValue(QUERIES_FLAG)));
		// }
		// } catch (IOException e) {
		// System.err.println("No queries file found");
		// }
		// try {
		// if (parser.hasFlag(RESULTS_FLAG)) {
		// if (parser.hasValue(RESULTS_FLAG)) {
		// search.print(Paths.get(parser.getValue(RESULTS_FLAG)));
		// }
		// else {
		// search.print(Paths.get(RESULTS_DEFAULT));
		// }
		// }
		// } catch (IOException e) {
		// System.out.println("No output file for search");
		// }
		// }
		// else {
		// /***************************
		// * Project 3
		// ****************************************/
		//
		// // Logger logger = LogManager.getLogger();
		//
		// ThreadSafeInvertedIndex index = new ThreadSafeInvertedIndex();
		// int numThreads = THREAD_DEFAULT;;
		//
		// try {
		// if (parser.hasFlag(THREAD_FLAG)) {
		// numThreads = Integer.parseInt(parser.getValue(THREAD_FLAG));
		// if (numThreads <= 0) {
		// numThreads = THREAD_DEFAULT;
		// }
		// }
		// } catch (NumberFormatException e) {
		// System.err.println("Wrong number of thread.");
		// // e.printStackTrace();
		// }
		//
		// logger.debug("num thread: " + numThreads);
		//
		// ThreadSafePartialSearchBuilder search = new
		// ThreadSafePartialSearchBuilder(
		// numThreads, index);
		//
		// MultiThreadInvertedIndexBuilder invertedIndexBuilder = new
		// MultiThreadInvertedIndexBuilder(
		// numThreads);
		//
		// try {
		// logger.debug("traversing directory");
		// invertedIndexBuilder.traverseDirectory(
		// Paths.get((parser.getValue(INPUT_FLAG))), index);
		//
		// logger.debug("calling shut down");
		// invertedIndexBuilder.shutdown();
		// logger.debug("index work queue shutted down");
		// logger.debug("Done with traverseDirectory");
		// } catch (Exception e) {
		// System.err.println("No arguments");
		// // e.printStackTrace();
		// } /* build inverted index */
		// try {
		// logger.debug("going to print index");
		// if (parser.hasFlag(INDEX_FLAG)) {
		// logger.debug("has index flag");
		// if (!parser.hasValue(INDEX_FLAG)) {
		// index.print(Paths.get(INDEX_DEFAULT));
		// }
		// else {
		// index.print(Paths.get(parser.getValue(INDEX_FLAG)));
		// }
		// }
		// else {
		// logger.debug("doesnt has index flag");
		// }
		//
		// } catch (IOException e) {
		// System.err.println("No file can be printed. Try it again");
		// } /* print inverted index */
		//
		// try {
		// if (parser.hasFlag(QUERIES_FLAG)
		// && parser.hasValue(QUERIES_FLAG)) {
		// search.parseFile(Paths.get(parser.getValue(QUERIES_FLAG)));
		// }
		// logger.debug("Done with parsing queries");
		// search.shutdown();
		// } catch (IOException e) {
		// System.err.println("No queries file found");
		// } /* partial search */
		//
		// try {
		// if (parser.hasFlag(RESULTS_FLAG)) {
		// logger.debug("try to print search result");
		// if (parser.hasValue(RESULTS_FLAG)) {
		// search.print(Paths.get(parser.getValue(RESULTS_FLAG)));
		// }
		// else {
		// search.print(Paths.get(RESULTS_DEFAULT));
		// }
		// }
		// } catch (IOException e) {
		// System.out.println("No output file for search");
		// } /* print partial search */
		// /**
		// * end of old version
		// */
		// }
	}
}
