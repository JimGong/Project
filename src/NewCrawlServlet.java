import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;

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
		out.printf("<p>Enter the new crawl if you want<p>");

		printForm(request, response);
		String newcrawl = request.getParameter("newcrawl");
		addNewDatabase(newcrawl);
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
				"<p><input type=\"submit\" value=\"add new crawl to the database\">");

		out.printf("<a href='/'>Back to Search</a>");
		String newcrawl = request.getParameter("newcrawl");
		System.out.println(newcrawl);
	}

	private void addNewDatabase(String seed) throws MalformedURLException {
		ThreadSafeInvertedIndex newIndex = new ThreadSafeInvertedIndex();
		System.out.println("adding new ");
		WebCrawler webcrawler = new WebCrawler(numThread, newIndex);
		webcrawler.traverse(seed);

		index.addAll(newIndex);
	}

}
