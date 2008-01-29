package com.surelogic.sierra.jdbc.server;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.surelogic.sierra.jdbc.user.User;

/**
 * FormSecurityFilter is responsible for Sierra's form-based authentication
 * mechanism. It does this by checking for a {@link User} in the session. If
 * none exists, then a form is sent instead that allows the user to login and
 * return to the originally requested resource.
 * 
 * @author nathan
 * 
 */
public class FormSecurityFilter implements Filter {

	public void destroy() {

	}

	public void doFilter(final ServletRequest req, final ServletResponse resp,
			final FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			final HttpServletRequest request = (HttpServletRequest) req;
			if (!"/login".equals(request.getServletPath())) {
				final HttpSession session = request.getSession();
				User user = (User) session.getAttribute(SecurityHelper.USER);
				if (user == null) {
					HttpServletResponse response = (HttpServletResponse) resp;
					final OutputStream out = response.getOutputStream();
					SecurityHelper.writeLoginForm(out,
							request.getServletPath(), false);
				} else {
					chain.doFilter(req, resp);
				}
			} else {
				chain.doFilter(req, resp);
			}
		}
	}

	public void init(FilterConfig arg0) throws ServletException {

	}

}
