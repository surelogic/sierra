package com.surelogic.sierra.tool.message;

import static org.junit.Assert.*;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is actually a test of the web service as well as the client stub. It was
 * convenient, however, to place it in the client project.
 * 
 * @author nathan
 * 
 */
public class TigerServiceTest {

	private TigerService service;

	@Before
	public void setUp() throws Exception {
		service = new TigerServiceClient().getTigerServicePort();
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testPublishRun() {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(Run.class);
			Unmarshaller um = context.createUnmarshaller();
			InputStream in = getResource("sierra-entity.xml.parsed");
			Run run = (Run) um.unmarshal(in);
			service.publishRun(run);
		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	public void testGetQualifiers() {
		assertTrue(service.getQualifiers().getQualifier().contains("Default"));
	}

	@Test
	public void testGetAuditTrail() {
		fail("Not yet implemented");
	}

	@Test
	public void testCommitAuditTrail() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAuditTrails() {
		fail("Not yet implemented");
	}

	private static InputStream getResource(String name) {
		return PublishMessageRunGenerator.class
				.getResourceAsStream("/com/surelogic/sierra/tool/message/"
						+ name);
	}
}
