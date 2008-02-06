package com.surelogic.sierra.jdbc.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Internal utility class. Not for use outside of the server package.
 * 
 * Aaron note: need to make this external or provide a framework for standard session attrib access.
 * I made this public for now.
 * 
 * @author nathan
 * 
 */
public class SecurityHelper {

	static final String AUTH_NAME = "SierraAuthName";
	static final String AUTH_PASS = "SierraAuthPass";
	static final String AUTH_REDIRECT = "SierraAuthRedirect";
	public static final String USER = "SierraUser";
	
	static void writeLoginForm(OutputStream out, String redirect, boolean retry)
			throws IOException {
		final OutputStreamWriter writer = new OutputStreamWriter(out);
		writer
				.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
						+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
						+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
						+ "<head>"
						+ "	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
						+ "	<title>Login to Sierra</title>" + "</head>"
						+ "<body>"
						+ (retry ? "<h3>User name or password invalid, please try again.</h3>"
								: "	<h3>Please login:</h3>")
						+ "	<form action=\"login\" method=\"post\">"
						+ "		<p>"
						+ "         <input type=\"hidden\" name=\""
						+ AUTH_REDIRECT
						+ "\" value=\""
						+ redirect
						+ "\" />"
						+ "			<label for=\""
						+ AUTH_NAME
						+ "\">User: </label> "
						+ "			<input type=\"text\" name=\""
						+ AUTH_NAME
						+ "\" /> "
						+ "			<br />"
						+ "			<label for=\""
						+ AUTH_PASS
						+ "\">Password: </label>"
						+ "			<input type=\"password\" name=\""
						+ AUTH_PASS
						+ "\" />"
						+ "			<br />"
						+ "			<input type=\"submit\" value=\"Login\" />"
						+ "		</p>" + "	</form>" + "</body>" + "</html>");
		writer.flush();
	}
}
