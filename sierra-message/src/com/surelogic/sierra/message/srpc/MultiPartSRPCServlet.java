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
import com.surelogic.sierra.tool.message.InvalidVersionException;

public abstract class MultiPartSRPCServlet extends HttpServlet {

  protected final Logger log = SLLogger.getLoggerFor(this.getClass());

  private MultiPartEncoding codec;

  private static final long serialVersionUID = 5771077152119139820L;

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    try {
      ResponseStatus status;
      Object response;
      try {
        MethodInvocation method = null;
        Class<?> returnType = null;
        try {
          method = codec.decodeMethodInvocation(req);
          try {
            response = method.invoke(this);
            status = ResponseStatus.OK;
            returnType = method.getMethod().getReturnType();
          } catch (final InvocationTargetException e) {
            response = new RaisedException(e.getCause());
            status = ResponseStatus.RAISED;
            returnType = RaisedException.class;
          }
        } catch (final InvalidVersionException e) {
          response = new InvalidVersion(e.getServiceVersion(), e.getClientVersion());
          status = ResponseStatus.VERSION;
          returnType = InvalidVersion.class;
        }
        codec.encodeResponse(resp.getOutputStream(), status, response, returnType);
        log.info("Request: " + method + " Response Status: " + status);
      } catch (final SRPCException e) {
        // If we had some type of general messaging/processing
        // exception, send a failure.
        e.printStackTrace();
        response = new Failure(e);
        status = ResponseStatus.FAIL;
        log.log(Level.INFO, "Exception processing request: " + e, e);
      } catch (final Exception e) {
        log.log(Level.WARNING, e.getMessage(), e);
        e.printStackTrace();
        response = new Failure(e);
        status = ResponseStatus.FAIL;
        log.log(Level.INFO, "General request processing exception: " + e, e);
      }
    } catch (final Exception e) {
      // We couldn't even send a message back to the client, log out a
      // failure.
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  public void init() throws ServletException {
    super.init();
    for (final Class<?> c : this.getClass().getInterfaces()) {
      System.out.println(c.getSimpleName());
      if (c.getAnnotation(Service.class) != null) {
        codec = MultiPartEncoding.getEncoding(c);
        return;
      }
    }
    throw new SRPCException("The servlet " + getClass().getName() + " does not implement an SRPC service.");
  }

}
