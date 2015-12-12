import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class History extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.printf(
				"<body background=http://img0.gtsstatic.com/wallpapers/f94bda506ba71e59ee5ad53fff49729c_large.jpeg>");
		out.printf("<body>%n");
		new LoginBaseServlet().prepareResponse("History", response);

		String user = new LoginBaseServlet().getUsername(request);

		out.printf("<h1>Welcome, " + user + "</h1>");

		out.printf("<form method=\"get\" action=\"%s\">%n",
				request.getServletPath());
		out.printf(
				"<input type=\"submit\" name=\"showHistory\" value=\"Show history\">");
		out.printf(
				"<input type=\"submit\" name=\"cleanHistory\" value=\"Clean history\">");
		out.printf("</form>\n%n");
		String userchoice = request.getParameter("showHistory");

		// System.out.println("userchoice: " + userchoice);
		if (!(userchoice == null)) {
			printHistory(user, out);
		}
		userchoice = request.getParameter("cleanHistory");
		if (!(userchoice == null)) {
			cleanHistory(user, out);
		}

		out.printf("<a href='/'>Back to Search</a>");

	}

	private void printHistory(String username, PrintWriter out) {
		out.printf("<p>Your Search History:<p>");

		LoginBaseServlet.dbhandler.getSearchHistory(username, out);
	}

	private void cleanHistory(String username, PrintWriter out) {

		out.printf("<p>Your Search History has been cleared<p>");
		LoginBaseServlet.dbhandler.cleanSearchHistory(username);
	}

}
