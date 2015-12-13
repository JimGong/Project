import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class FavouriteServlet extends HttpServlet {

	private String url;
	private String username;

	public FavouriteServlet(String url, String username) {
		super();
		this.url = url;
		this.username = username;

	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		LoginBaseServlet.dbhandler.addFavourite(username, url);
		response.sendRedirect("/");

	}

}
