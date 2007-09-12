package com.surelogic.sierra.tool.message;

import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;

import org.junit.Test;

public class SierraServiceClientTest extends TestCase {

	private final String username = "alex";
	private final String badPassword = "alexBAD";
	private final String goodPassword = "alex";
	private final String host = "localhost";
	private final int port = 8080;
	private final boolean secure = false;

	@Test
	public void testSierraServiceClientBadLogin() {
		try {
			SierraServerLocation serverLocation = new SierraServerLocation(
					host, secure, port, username, badPassword);
			SierraService service = new SierraServiceClient(serverLocation)
					.getSierraServicePort();
			service.getQualifiers(new QualifierRequest());
		} catch (WebServiceException wse) {
			assertEquals("request requires HTTP authentication: Unauthorized",
					wse.getMessage());
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	public void testSierraServiceClientGoodLogin() {
		try {
			SierraServerLocation serverLocation = new SierraServerLocation(
					host, secure, port, username, goodPassword);
			SierraService service = new SierraServiceClient(serverLocation)
					.getSierraServicePort();
			Qualifiers qualifiers = service
					.getQualifiers(new QualifierRequest());
			assertNotNull("Qualifiers is null", qualifiers);
			assertTrue("No Qualifiers exist", !qualifiers.getQualifier()
					.isEmpty());
		} catch (WebServiceException wse) {
			fail("request requires HTTP authentication: Unauthorized");
			wse.printStackTrace();
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
}
