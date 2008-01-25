package com.surelogic.sierra.jdbc.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FormSecurityFilter implements Filter {

	private static final String USER = "SierraUser";
	
	public void destroy() {

	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		
		
	}

	public void init(FilterConfig arg0) throws ServletException {

	}
	
}
