import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {

	private static final String TITLE = "Search Engine";
	private CookieBaseServlet cookieBaseServlet;
	private ThreadSafeInvertedIndex index;
	ThreadSafePartialSearchBuilder search;

	public String VISIT_DATE = "Visited";
	public String VISIT_COUNT = "Count";
	public String SEARCH_HISTORY = "History";

	private final int numThread;

	public SearchServlet(InvertedIndex index, int numThread) {

		super();
		cookieBaseServlet = new CookieBaseServlet();
		this.index = (ThreadSafeInvertedIndex) index;
		this.numThread = numThread;
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

		// LoginBaseServlet.dbhandler.updateLastLoginTime(user);

		System.out.println("user: " + user);

		out.printf("<div align=right> <p>Welcome, <a href=/login>"
				+ (user == null ? "Login" : user) + "</a></p>%n");

		if (!(user == null)) {
			out.printf("<p>Not " + user
					+ "? <a href='/login'>Sign Out</a><p></div>%n");
		}
		out.printf(
				"<center><img src=http://simpleicon.com/wp-content/uploads/smile.png width=\"150\" height=\"150\">%n");
		out.printf("<h1>Search Engine</h1>%n");

		printForm(request, response);/* build text box */

		out.printf("<p>");

		/* Update visit count as necessary and output information. */

		LoginBaseServlet.dbhandler.getLastLoginTime(user, out);
		LoginBaseServlet.dbhandler.getLoggedInUser(out);
		out.printf("</p>%n");
		out.printf("<p>Your suggested query is: <p>");

		LoginBaseServlet.dbhandler.getSuggestedQuery(
				new LoginBaseServlet().getUsername(request), out);
		/*
		 * Checks if the browser indicates visits should not be tracked. This is
		 * not a standard header! Try this in Safari private browsing mode.
		 */
		if (request.getIntHeader("DNT") != 1) {
			// response.addCookie(new Cookie(VISIT_DATE, getDate()));
			// response.addCookie(new Cookie(VISIT_COUNT, visitCount));
		}
		else {
			cookieBaseServlet.clearCookies(request, response);
			out.printf("<p>Your visits will not be tracked.</p>");
		}
		/* end of cookie stuff */
		out.printf("<font size='3'><p>It is %s.</p></font></center>%n",
				getDate());

		out.printf(
				"<font size='2'><p>This request was handled by thread %s.</p></font>%n",
				Thread.currentThread().getName());

		out.printf("</body>%n");
		out.printf("</html>%n");

		response.setStatus(HttpServletResponse.SC_OK);
	}

	private static void printForm(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		out.printf("<form method=\"post\" action=\"%s\">%n", "/");

		out.printf("<table cellspacing=\"0\" cellpadding=\"2\"%n");
		out.printf("<tr>%n");
		out.printf("\t<td nowrap></td>%n");
		out.printf("\t<td>%n");
		out.printf(
				"\t\t<input type=\"text\" name=\"search\" maxlength=\"70\" size=\"80\"><br>%n");
		out.printf(
				"<center><input type=\"checkbox\" name=\"privateSearch\" value=\"privateSearch\">Private Search");
		out.printf(
				"<input type=\"checkbox\" name=\"partialSearch\" value=\"partialSearch\">Partial Search</center><br>%n");
		out.printf("\t</td>%n");
		out.printf("</tr>%n");

		out.printf("</table>%n");
		out.printf("<p><input type=\"submit\" value=\"Search\">");

		out.printf("</form> \n%n");

		out.printf("<form method=\"get\" action=\"%s\">%n",
				request.getServletPath());
		out.printf("<button formaction=/history>History</button></p>");
		out.printf(
				"<button formaction=/favourite_result>Favourite</button></p>");
		out.printf(
				"<button formaction=/new_crawl>Want a bigger database?Add new crawl</button></p>");

		out.printf("</form> \n%n");
	}

	public static String getDate() {
		String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String query = request.getParameter("search");

		PrintWriter out = response.getWriter();

		if ((query != null) && (!query.isEmpty())) {

			search(request, response, out);

		}
		else {
			response.sendRedirect("/");
		}

	}

	private void search(HttpServletRequest request,
			HttpServletResponse response, PrintWriter out) throws IOException {
		out.printf(
				"<body background=http://img0.gtsstatic.com/wallpapers/f94bda506ba71e59ee5ad53fff49729c_large.jpeg>%n");

		/* get input */
		String query = request.getParameter("search");

		query = ((query == null) || query.equals("")) ? "" : query;

		query = StringEscapeUtils.escapeHtml4(query);

		String partialSearch = request.getParameter("partialSearch");

		boolean isPartialSearch;
		if (partialSearch == null) {
			isPartialSearch = false;
		}
		else {
			isPartialSearch = true;
		}

		search = new ThreadSafePartialSearchBuilder(numThread, this.index,
				isPartialSearch);
		/* show search result */
		if ((!query.equals(null)) && (!query.isEmpty())) {
			long startTime = System.nanoTime();
			search.parseLine(query);
			search.finish();
			long endTime = System.nanoTime();
			out.printf(
					"<h1><img src=http://simpleicon.com/wp-content/uploads/smile.png width=\"60\" height=\"60\">%n");

			out.printf("Search Result for %s</h1>%n", query);
			long duration = (endTime - startTime);
			double seconds = duration / 1000000000.0;

			Map<String, List<SearchResult>> result = search.getResult();

			/* format */
			out.printf("%n<style type='text/css'>");
			out.printf("d.pos_right{");
			out.printf("position:relative; left:70px }");
			out.printf("</style>%n%n");
			out.printf("<d class='pos_right'>");
			/* end of format */

			for (String url : result.keySet()) {
				List<SearchResult> list = result.get(url);

				if (list.size() == 0) {
					out.printf("<p>No result found<p>%n");
				}
				else {
					out.printf(
							"<p>%n<font size='3' color='darkgray'>%nAbout %s results (%s seconds)%n</font><p>%n%n",
							list.size(), seconds);
				}

				for (SearchResult a : list) {

					LoginBaseServlet.dbhandler.getTitle(a.getLocation(), out);
					out.printf("<p><a href=/visited?url=" + a.getLocation()
							+ ">" + a.getLocation() + "</a>" + "%n");
					printAddFav(request, response, a.getLocation());

					LoginBaseServlet.dbhandler.getSnippet(a.getLocation(), out);
					LoginBaseServlet.dbhandler
							.getURLVisitedTime(a.getLocation(), out);
					out.printf("<br>");
				}
			}
			System.out.println("done with printing to website");
			/* write search */
		}
		out.printf("</d>%n");
		/* end printing search result */

		String privateSearch = request.getParameter("privateSearch");
		privateSearch = privateSearch == null ? "" : privateSearch;
		if (privateSearch.equals("privateSearch")) {
			out.printf(
					"<p>You are in private search model. Search History won't be record<p>");
		}
		else {
			LoginBaseServlet.dbhandler.addSearchHistory(
					new LoginBaseServlet().getUsername(request), query);
		}

		out.printf("<a href='/'>Back to Search</a>");

	}

	private void printAddFav(HttpServletRequest request,
			HttpServletResponse response, String url) throws IOException {
		PrintWriter out = response.getWriter();

		out.printf("<form method=\"post\" action=\"''\">%n");

		out.printf("<table cellspacing=\"0\" cellpadding=\"2\"%n");
		out.printf("<tr>%n");
		out.printf("\t<td nowrap></td>%n");
		out.printf("\t<td>%n");

		boolean isFavourite = LoginBaseServlet.dbhandler
				.getFavourite(new LoginBaseServlet().getUsername(request), url);

		if (isFavourite) {
			out.printf("<p>You have already added as favourite<p>%n");
		}
		else {
			out.printf("<a href=/favourite?url=" + url + ">"
					+ "add to favourite" + "</a>" + "<p>%n");
		}

		out.printf("\t</td>%n");
		out.printf("</tr></table></form>\n%n");

	}
}
