import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {

	private static final String TITLE = "Search Engine";
	private LinkedList<String> searchHistory;
	private CookieBaseServlet cookieBaseServlet;
	private ThreadSafeInvertedIndex index;
	ThreadSafePartialSearchBuilder search;

	public String VISIT_DATE = "Visited";
	public String VISIT_COUNT = "Count";
	public String SEARCH_HISTORY = "History";

	public SearchServlet(InvertedIndex index, int numThread) {

		super();
		searchHistory = new LinkedList<>();
		cookieBaseServlet = new CookieBaseServlet();
		this.index = (ThreadSafeInvertedIndex) index;
		search = new ThreadSafePartialSearchBuilder(numThread, this.index);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (request.getRequestURI().endsWith("favicon.ico")) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		System.out.println(Thread.currentThread().getName() + ": "
				+ request.getRequestURI());

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();
		out.printf("<html>%n");
		out.printf("<head><title>%s</title></head>%n", TITLE);
		out.printf(
				"<body background=http://img0.gtsstatic.com/wallpapers/f94bda506ba71e59ee5ad53fff49729c_large.jpeg>");
		out.printf("<body>%n");

		/* get user name */
		String user = new LoginBaseServlet().getUsername(request);

		if (user == null) {
			user = "Login";
		}
		System.out.println("user: " + user);
		out.printf("<div align=right> <p>Welcome, <a href=/login>" + user
				+ "</a></p>%n");

		if (!user.equals("Login")) {
			out.printf("<p>Not " + user
					+ "? <a href='/login'>Sign Out</a><p></div>%n");
			user = "Login";
		}
		out.printf(
				"<center><img src=http://simpleicon.com/wp-content/uploads/smile.png width=\"150\" height=\"150\">%n");
		out.printf("<h1>Search Engine</h1>%n");

		printForm(request, response);/* build text box */

		/* get input */

		String query = request.getParameter("search");

		query = ((query == null) || query.equals("")) ? "" : query;

		query = StringEscapeUtils.escapeHtml4(query);
		search = new ThreadSafePartialSearchBuilder(5, this.index);
		/* show search result */
		if ((!query.equals(null)) && (!query.isEmpty())) {
			System.out.println("------user input: " + query);
			synchronized (searchHistory) {
				// System.out.println("before add " + query + " into the hist: "
				// + searchHistory.toString());
				searchHistory.addLast(query);
				// System.out.println(query + " added, new hist: "
				// + searchHistory.toString());

				while (searchHistory.size() > 5) {
					// System.out.println("remove the first");
					searchHistory.removeFirst();
				}
			}
			// System.out.println(
			// "before adding to cookie: " + searchHistory.toString());
			// response.addCookie(new Cookie(SEARCH_HISTORY, searchHistory
			// .toString().replace("[", "").replace("]", "")));
			// System.out.println("searching " + query + " in inverted index");
			search.parseLine(query);
			search.finish();

			Map<String, List<SearchResult>> result = search.getResult();

			for (String url : result.keySet()) {
				List<SearchResult> list = result.get(url);

				if (list.size() == 0) {
					out.printf("<p>No result found<p>%n");
				}

				for (SearchResult a : list) {
					out.printf("<p><a href=" + a.getLocation() + ">"
							+ a.getLocation() + "</a><p>%n");
					out.printf("\n");
				}
			}
			System.out.println("done with printing to website");
			/* write search */
		}
		/* end printing search result */

		/* show history */
		synchronized (searchHistory) {
			if (!searchHistory.isEmpty()) {
				out.printf("<p>Search History<p>%n");
				for (String history : searchHistory) {
					out.printf("<p>%s</p>%n%n", history);
				}
			}
		}
		/* end of printing history */

		/* cookie */
		Map<String, String> cookies = cookieBaseServlet.getCookieMap(request);
		String visitDate = cookies.get(VISIT_DATE);
		String visitCount = cookies.get(VISIT_COUNT);
		// String history = cookies.get(SEARCH_HISTORY);

		out.printf("<p>");

		// if ((!query.equals(null)) && (!query.isEmpty())) {
		// System.out.println("old history: " + history);
		// if (history == null) {
		// System.out.println("1");
		// history = query;
		// }
		// else {
		// System.out.println("2");
		// history = history + ", " + query;
		//
		// }
		// System.out.println("new history: " + history);
		// }
		//
		// if ((history != null) && !history.equals("")) {
		// out.printf("<p>Search History<p>%n");
		// out.printf("<p>%s</p>%n%n", history);
		// }

		/* Update visit count as necessary and output information. */

		if ((visitDate == null) || (visitCount == null)) {
			visitCount = "0";

			out.printf("You have never been to this webpage before! ");
			out.printf("Thank you for visiting.");
		}
		else {
			visitCount = Integer.toString(Integer.parseInt(visitCount) + 1);
			out.printf("You have visited this website %s times. ", visitCount);
			out.printf("Your last visit was on %s.", visitDate);
		}

		out.printf("</p>%n");
		/*
		 * Checks if the browser indicates visits should not be tracked. This is
		 * not a standard header! Try this in Safari private browsing mode.
		 */
		if (request.getIntHeader("DNT") != 1) {
			response.addCookie(new Cookie(VISIT_DATE, getDate()));
			response.addCookie(new Cookie(VISIT_COUNT, visitCount));
			// response.addCookie(new Cookie(SEARCH_HISTORY, query));
		}
		else {
			cookieBaseServlet.clearCookies(request, response);
			out.printf("<p>Your visits will not be tracked.</p>");
		}
		/* end of cookie stuff */

		out.printf(
				"<font size='2'><p>This request was handled by thread %s.</p></font>%n",
				Thread.currentThread().getName());

		out.printf("<font size='3'><p>It is %s.</p></font></center>%n",
				getDate());

		out.printf("</body>%n");
		out.printf("</html>%n");

		response.setStatus(HttpServletResponse.SC_OK);
	}

	private static void printForm(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		out.printf("<form method=\"get\" action=\"%s\">%n",
				request.getServletPath());
		out.printf("<table cellspacing=\"0\" cellpadding=\"2\"%n");
		out.printf("<tr>%n");
		out.printf("\t<td nowrap></td>%n");
		out.printf("\t<td>%n");
		out.printf(
				"\t\t<input type=\"text\" name=\"search\" maxlength=\"70\" size=\"80\">%n");
		out.printf("\t</td>%n");
		out.printf("</tr>%n");

		out.printf("</table>%n");
		out.printf("<p><input type=\"submit\" value=\"Search\"></p>");

		out.printf("</form>\n%n");
	}

	public static String getDate() {
		String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}

	public void cleanHistory() {
		searchHistory.clear();
	}

	/* cookie stuff */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String username = new LoginBaseServlet().getUsername(request);
		String query = request.getParameter("search");

		LoginBaseServlet.dbhandler.addSearchHistory(username, query);
	}
}
