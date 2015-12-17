import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class NewCrawlServlet extends HttpServlet {

	private ThreadSafeInvertedIndex index;
	int numThread;

	public NewCrawlServlet(InvertedIndex index, int numThread) {
		super();
		this.index = (ThreadSafeInvertedIndex) index;
		this.numThread = numThread;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.printf("<html>%n");
		out.printf("<head><title>%s</title></head>%n", "New Crawl");
		out.printf(
				"<body background=http://img0.gtsstatic.com/wallpapers/f94bda506ba71e59ee5ad53fff49729c_large.jpeg>");
		out.printf("<body>%n");
		String error = request.getParameter("error");
		if (error != null) {
			out.printf("<p style=\"color: red;\">" + "Invalid link" + "</p>");
		}

		String status = request.getParameter("ok");
		if (status != null) {
			out.printf(
					"<p style=\"color: green;\">Your new crawl has been added</p>");
		}

		out.printf("<p>Enter the new crawl if you want<p>");
		printForm(request, response);
		String newcrawl = request.getParameter("newcrawl");

		String userchoice = request.getParameter("addcrawl");
		if (userchoice != null) {
			if ((newcrawl == null) || newcrawl.trim().isEmpty()) {
				response.sendRedirect("/new_crawl?error=invalid_link");
			}
			else {
				addNewDatabase(newcrawl, request, response);
				response.sendRedirect("/new_crawl?ok");
			}
		}
		// addNewDatabase(newcrawl, request, response);

		out.printf("</body>%n");
		out.printf("</html>%n");

	}

	private void printForm(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();

		out.printf("<form method=\"get\" action=\"%s\">%n",
				request.getServletPath());
		out.printf(
				"<input type=\"text\" name=\"newcrawl\" maxlength=\"200\" size=\"100\">");
		out.printf(
				"<p><input type=\"submit\" name=\"addcrawl\" value=\"add new crawl to the database\">");

		out.printf("<h2><a href='/'>Back to Search</a><h2>");
	}

	private void addNewDatabase(String seed, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		ThreadSafeInvertedIndex newIndex = new ThreadSafeInvertedIndex();
		WebCrawler webcrawler = new WebCrawler(numThread, newIndex);
		webcrawler.traverse(seed);

		index.addAll(newIndex);
	}

}
