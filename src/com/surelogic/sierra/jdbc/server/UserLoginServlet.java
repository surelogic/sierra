package com.surelogic.sierra.jdbc.server;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.surelogic.sierra.jdbc.user.ServerUserManager;
import com.surelogic.sierra.jdbc.user.User;

/**
 * UserLoginServlet associates a Sierra User with a given session. It takes
 * three request parameters:
 * <dl>
 * <dt>SierraAuthName</dt>
 * <dd>A user name</dd>
 * <dt>SierraAuthPass</dt>
 * <dd>The user's password.</dd>
 * <dt>SierraAuthUrl</dt>
 * <dd>A url to redirect to upon successful authentication.
 * </dl>
 * 
 * @author nathan
 * 
 */
public class UserLoginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3047892708417176135L;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		final HttpSession session = request.getSession();
		final String userName = request.getParameter(SecurityHelper.AUTH_NAME);
		final String password = request.getParameter(SecurityHelper.AUTH_PASS);
		final String context = request
				.getParameter(SecurityHelper.AUTH_REDIRECT);
		if (userName != null && password != null) {
			final User u = ServerConnection
					.withReadOnly(new UserTransaction<User>() {

						public String getUserName() {
							return userName;
						}

						public User perform(Connection conn, Server server)
								throws Exception {
							return ServerUserManager.getInstance(conn).login(
									userName, password);
						}
					});
			if (u != null) {
				session.setAttribute(SecurityHelper.USER, u);
				getServletContext().getRequestDispatcher(context).forward(
						request, response);
			} else {
				SecurityHelper.writeLoginForm(response.getOutputStream(),
						context, true);
			}
		}
	}

}
