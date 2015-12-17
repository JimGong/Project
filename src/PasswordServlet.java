import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class PasswordServlet extends LoginBaseServlet {

	protected static final LoginDatabaseHandler dbhandler = LoginDatabaseHandler
			.getInstance();

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		prepareResponse("Change Password", response);

		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");

		if (error != null) {
			String errorMessage = getStatusMessage(error);
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		printForm(out);

		finishResponse(response);
	}

	private void printForm(PrintWriter out) {
		assert out != null;

		out.println("<form  method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Usename:</td>");
		out.println(
				"\t\t<td><input type=\"text\" name=\"user\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>New Password:</td>");
		out.println(
				"\t\t<td><input type=\"password\" name=\"newpassword\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Change Password\"></p>");
		out.println("</form>");

	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String user = request.getParameter("user");
		String newPassword = request.getParameter("newpassword");
		dbhandler.updatePassword(user, newPassword);
		response.sendRedirect("/login?new_password");
	}
}
