package com.surelogic.sierra.message.srpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.logging.SLLogger;

/**
 * Implementors of a service should extend this class, and implement the
 * interface they intend to serve.  For example:
 * <pre>
 * public class FooServlet implements Foo extends SRPCServlet {
 *    //Implement service methods.
 *    public Foo bar(Baz bin) {
 *    	... do something
 *    }
 * }
 * </pre>
 * 
 * @author nathan
 * 
 */
public abstract class SRPCServlet extends HttpServlet {

	protected final Logger log = SLLogger.getLoggerFor(this.getClass());

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			final Encoding codec = getEncoding();
			final InputStream in = req.getInputStream();
			final OutputStream out = resp.getOutputStream();
			try {
				final MethodInvocation method = codec
						.decodeMethodInvocation(in);
				final Object response = method.invoke(this);
				codec.encodeResponse(resp.getOutputStream(), ResponseStatus.OK,
						response);
			} catch (Exception e) {
				codec.encodeResponse(out, ResponseStatus.FAIL, e);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	Encoding getEncoding() throws SRPCException {
		for (Class<?> c : this.getClass().getInterfaces()) {
			if (Service.class.isAssignableFrom(c)) {
				return Encoding.getEncoding(c);
			}
		}
		throw new SRPCException(
				"The servlet does not implement an SRPC service.");
	}

}
