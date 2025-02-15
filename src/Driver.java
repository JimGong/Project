import java.io.IOException;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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

		Logger logger = LogManager.getLogger();
		ArgumentParser parser = new ArgumentParser(args);

		InvertedIndex index = null;
		PartialSearchBuilderInterface search = null;

		if ((!parser.hasFlag(THREAD_FLAG)) && (!parser.hasFlag(SEED_FLAG))) {
			index = new InvertedIndex();
			search = new PartialSearchBuilder(index);

			try {
				InvertedIndexBuilder.traverseDirectory(
						Paths.get((parser.getValue(INPUT_FLAG))), index);
			} catch (Exception e) {
				System.err.println("No arguments");
			}

		} /* single thread version build inverted index */
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
			MultiThreadInvertedIndexBuilder multiThreadInvertedIndexBuilder = new MultiThreadInvertedIndexBuilder(
					numThreads);
			if (!parser.hasFlag(SEED_FLAG)) {
				try {
					logger.debug("traversing directory");
					multiThreadInvertedIndexBuilder.traverseDirectory(
							Paths.get((parser.getValue(INPUT_FLAG))),
							(ThreadSafeInvertedIndex) index);
				} catch (Exception e) {
					System.err.println("No arguements");
				} /* File */
			}
			else {
				try {

					logger.debug("$$$$$$$$$$$$$$$$$$ traversing URL");
					WebCrawler webCrawler = new WebCrawler(numThreads,
							(ThreadSafeInvertedIndex) index);
					webCrawler.traverse(parser.getValue(SEED_FLAG));

					logger.debug("$$$$$$$$$$$$$$$$$$ done with traversing URL");
				} catch (IOException e) {
					System.err.println("Invalid link.");
					e.printStackTrace();
				} /* URL */
			}
			logger.debug("calling shut down");
			multiThreadInvertedIndexBuilder.shutdown();
			logger.debug("Done with InvertedIndex");
			search = new ThreadSafePartialSearchBuilder(numThreads, index,
					true);

			/** project 5 */
			System.out.println("Starting server");
			if (parser.hasFlag(PORT_FLAG)) {
				/* test database connection first */

				try {
					DatabaseConnector test = new DatabaseConnector(
							"database.properties");
					System.out.println("Connecting to " + test.uri);

					if (test.testConnection()) {
						System.out
								.println("Connection to database established.");
					}
					else {
						System.err.println(
								"Unable to connect properly to database. Turn down server");
						System.exit(0);
					}
				} catch (Exception e) {
					System.err
							.println("Unable to connect properly to database.");
					System.err.println(e.getMessage());
				}
				/* finish testing database connection */

				Server server = null;
				if (parser.hasValue(PORT_FLAG)) {
					server = new Server(
							Integer.parseInt(parser.getValue(PORT_FLAG)));
				}
				else {
					server = new Server(PORT_DEFAULT);
				}

				ServletContextHandler handler = new ServletContextHandler(
						ServletContextHandler.SESSIONS);

				handler.setContextPath("/");
				handler.addServlet(
						new ServletHolder(new SearchServlet(index, numThreads)),
						"/");
				handler.addServlet(LoginUserServlet.class, "/login");
				handler.addServlet(LoginRegisterServlet.class, "/register");
				handler.addServlet(History.class, "/history");
				handler.addServlet(PasswordServlet.class, "/reset_password");
				handler.addServlet(VisitedURLServlet.class, "/visited");
				handler.addServlet(
						new ServletHolder(
								new NewCrawlServlet(index, numThreads)),
						"/new_crawl");
				handler.addServlet(FavouriteServlet.class, "/favourite");
				handler.addServlet(CookieConfigServlet.class, "/config");
				handler.addServlet(FavouriteResultServlet.class,
						"/favourite_result");
				/* setup jetty server */
				server.setHandler(handler);
				try {
					server.start();

					server.join();

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			else {
				System.err.println("Can not set up the right server");
			}
			/** end of project 5 */

		} /* multi thread version */

		/* partial search */
		try {
			if (parser.hasFlag(QUERIES_FLAG) && parser.hasValue(QUERIES_FLAG)) {
				search.parseFile(Paths.get(parser.getValue(QUERIES_FLAG)));
			}
			logger.debug("Done with parsing queries");

			if (parser.hasFlag(THREAD_FLAG)) {
				((ThreadSafePartialSearchBuilder) search).shutdown();
			} /* shut down for multithread version */
		} catch (IOException e) {
			System.err.println("No queries file found");
		} /* partial search */

		/* print inverted index */
		try {
			logger.debug("going to print index");
			if (parser.hasFlag(INDEX_FLAG)) {
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
			logger.debug("done with print index");
		} catch (IOException e) {
			System.err.println("No file can be printed. Try it again");
		} /* print inverted index */

		/* print partial search */
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

	}
}
