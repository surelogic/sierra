package com.surelogic.sierra.message.srpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surelogic.common.logging.SLLogger;

/**
 * Implementors of a service should extend this class, and implement the
 * interface they intend to serve. For example:
 * 
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
			final Encoding codec = getEncoding(req.getContentType());
			ResponseStatus status;
			Object response;
			try {
				final MethodInvocation method = codec
						.decodeMethodInvocation(req.getInputStream());
				try {
					response = method.invoke(this);
					status = ResponseStatus.OK;
				} catch (InvocationTargetException e) {
					response = new RaisedException(e.getCause());
					status = ResponseStatus.RAISED;
				}
			} catch (SRPCException e) {
				// If we had some type of general messaging/processing
				// exception, send a failure.
				e.printStackTrace();
				response = new Failure(e);
				status = ResponseStatus.FAIL;
			} catch (Exception e) {
				log.log(Level.WARNING, e.getMessage(), e);
				e.printStackTrace();
				response = new Failure(e);
				status = ResponseStatus.FAIL;
			}
			resp.setContentType(codec.getContentType());
			codec.encodeResponse(resp.getOutputStream(), status, response);
		} catch (Exception e) {
			// We couldn't even send a message back to the client, log out a
			// failure.
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private Encoding getEncoding(String contentType) throws SRPCException {
		for (Class<?> c : this.getClass().getInterfaces()) {
			if (Service.class.isAssignableFrom(c)) {
				return Encoding.getEncoding(c, contentType);
			}
		}
		throw new SRPCException(
				"The servlet does not implement an SRPC service.");
	}

}
