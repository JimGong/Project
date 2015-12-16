import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SearchResultServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String query = request.getParameter("search");

		PrintWriter out = response.getWriter();
		System.out.println("###### " + query + " " + (query == null));
		if ((query != null) && (!query.isEmpty())) {

			out.printf("your search result shit will be shown on here");

		}
		else {
			response.sendRedirect("/");
		}

	}

}
