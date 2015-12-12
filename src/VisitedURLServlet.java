import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class VisitedURLServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String url = request.getQueryString();
		url = url.replace("url=", "");
		out.printf("<p>url: " + url + "<p>");

		LoginBaseServlet.dbhandler.updateURLVisitedTime(url);

		// stores for that user that the link was visited
		System.out.println("redirect to url: " + url);
		response.sendRedirect(url);

	}

}
