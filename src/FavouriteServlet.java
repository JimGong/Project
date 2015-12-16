import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class FavouriteServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String username = new LoginBaseServlet().getUsername(request);
		String url = request.getQueryString().substring(4);
		System.out
				.println("(favourite) user name: " + username + " url: " + url);

		LoginBaseServlet.dbhandler.addFavourite(username, url);
		System.out.println("done adding to favourite");

		PrintWriter out = response.getWriter();
		out.printf("<html>%n");

		out.printf("<p>%s, %s has been added as favourite<p>", username, url);
		out.printf(
				"<a href='javascript:history.go(-1)'>back to search result</a>");

		out.printf(
				"<body background=http://img0.gtsstatic.com/wallpapers/f94bda506ba71e59ee5ad53fff49729c_large.jpeg>");

		out.printf("</html>%n");
	}

}
