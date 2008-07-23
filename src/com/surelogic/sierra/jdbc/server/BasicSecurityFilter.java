package com.surelogic.sierra.jdbc.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sun.misc.BASE64Decoder;

import com.surelogic.sierra.jdbc.user.ServerUserManager;
import com.surelogic.sierra.jdbc.user.User;

/**
 * This filter accepts basic authentication credentials from the client, and
 * validates them against the server database. If the credentials are not valid,
 * a <code>401 - UNAUTHORIZED</code> response is sent. Otherwise, the filter
 * sets a request attribute named <code>SierraUser</code> to contain a valid
 * {@link User}.
 * 
 * @author nathan
 * 
 */
public class BasicSecurityFilter implements Filter {

	public void destroy() {
		// Do Nothing
	}

	public void doFilter(final ServletRequest request,
			final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		doFilter((HttpServletRequest) request, (HttpServletResponse) response,
				chain);
	}

	private void doFilter(final HttpServletRequest request,
			final HttpServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		User user = null;

		// Get the Authorization header, if one was supplied

		final String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			final StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				final String basic = st.nextToken();

				// We only handle HTTP Basic authentication

				if (basic.equalsIgnoreCase("Basic")) {
					final String credentials = st.nextToken();

					// This example uses sun.misc.* classes.
					// You will need to provide your own
					// if you are not comfortable with that.

					final BASE64Decoder decoder = new BASE64Decoder();
					final String userPass = new String(decoder
							.decodeBuffer(credentials));

					// The decoded string is in the form
					// "userID:password".

					final int p = userPass.indexOf(':');
					if (p != -1) {
						final String userID = userPass.substring(0, p);
						final String password = userPass.substring(p + 1);

						// Validate user ID and password
						// and set valid true true if valid.
						// In this example, we simply check
						// that neither field is blank

						user = ConnectionFactory
								.withReadOnly(new ServerTransaction<User>() {

									public User perform(final Connection conn,
											final Server server)
											throws SQLException {
										return ServerUserManager.getInstance(
												conn).login(userID, password);
									}
								});
					}
				}
			}
		}

		// If the user was not validated, fail with a
		// 401 status code (UNAUTHORIZED) and
		// pass back a WWW-Authenticate header for
		// this servlet.
		//
		// Note that this is the normal situation the
		// first time you access the page. The client
		// web browser will prompt for userID and password
		// and cache them so that it doesn't have to
		// prompt you again.

		if (user == null) {
			final String s = "Basic realm=\"Login Test Servlet Users\"";
			response.setHeader("WWW-Authenticate", s);
			response.setStatus(401);
		} else {
			request.setAttribute("SierraUser", user);
			chain.doFilter(request, response);
		}

	}

	public void init(final FilterConfig arg0) throws ServletException {
		// Do nothing
	}

}
