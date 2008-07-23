package com.surelogic.sierra.jdbc.server;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.surelogic.common.base64.Base64;
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
 * <dt>SierraAuthRedirect</dt>
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
		final String password64 = request
				.getParameter(SecurityHelper.AUTH_PASS);
		final String password = new String(Base64.decode(password64), "UTF-8");
		String context = request.getParameter(SecurityHelper.AUTH_REDIRECT);
		if (context == null) {
			context = "/";
		}
		if (userName != null && password != null) {
			final User u = ConnectionFactory
					.withReadOnly(new ServerTransaction<User>() {

						public User perform(Connection conn, Server server)
								throws Exception {
							return ServerUserManager.getInstance(conn).login(
									userName, password);
						}
					});
			if (u != null) {
				session.setAttribute(SecurityHelper.USER, u);
			}
			SecurityHelper.writeRedirect(response.getOutputStream(), context);
		}
	}

}
