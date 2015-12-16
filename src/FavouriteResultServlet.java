import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class FavouriteResultServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.printf(
				"<body background=http://img0.gtsstatic.com/wallpapers/f94bda506ba71e59ee5ad53fff49729c_large.jpeg>");
		out.printf("<body>%n");

		new LoginBaseServlet().prepareResponse("FavouriteResult", response);

		String user = new LoginBaseServlet().getUsername(request);

		out.printf("<h1>Welcome, " + user + "</h1>");

		out.printf("<form method=\"get\" action=\"%s\">%n",
				request.getServletPath());
		out.printf(
				"<input type=\"submit\" name=\"showfav\" value=\"Show favourite\">");
		out.printf(
				"<input type=\"submit\" name=\"cleanfav\" value=\"Clean favourite\">");
		out.printf("</form>\n%n");

		String userchoice = request.getParameter("showfav");
		if (!(userchoice == null)) {
			printFavouriteResult(user, out);;
		}

		userchoice = request.getParameter("cleanfav");
		if (!(userchoice == null)) {
			cleanFavouriteResult(user, out);
		}

		out.printf("<a href='/'>Back to Search</a>");
	}

	private void printFavouriteResult(String username, PrintWriter out) {
		out.printf("<p>Your Favourite Result:<p>");
		LoginBaseServlet.dbhandler.getFavouriteResult(username, out);
	}

	private void cleanFavouriteResult(String username, PrintWriter out) {
		out.printf("<p>Your Favourite Result has been cleared<p>");
		LoginBaseServlet.dbhandler.cleanFavourite(username);
	}
}
