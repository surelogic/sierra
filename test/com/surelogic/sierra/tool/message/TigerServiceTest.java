package com.surelogic.sierra.tool.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		try {
			JAXBContext context;
			context = JAXBContext.newInstance(Run.class);
			Unmarshaller um = context.createUnmarshaller();
			InputStream in = getResource("sierra-entity.xml.parsed");
			Run run = (Run) um.unmarshal(in);
			assertEquals("SUCCESS", service.publishRun(run));
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
	public void testSingleComment() {
		MergeAuditTrailRequest mergeReq = new MergeAuditTrailRequest();
		mergeReq.setQualifier("Default");
		mergeReq.setProject("sierra-entity");
		List<Merge> merges = new ArrayList<Merge>();
		mergeReq.setMerge(merges);
		Merge merge = new Merge();
		merges.add(merge);
		List<Match> matches = new ArrayList<Match>();
		merge.setMatch(matches);
		matches.add(new Match("package", "class", 34L, "PMD", "4.0",
				"ShortVariable"));
		MergeAuditResponse mergeRes = service.mergeAuditTrails(mergeReq);
		String trail = mergeRes.getTrail().get(0);
		service.mergeAuditTrails(mergeReq);
		String newTrail = mergeRes.getTrail().get(0);
		assertNotNull(trail);
		assertEquals(trail, newTrail);
		matches.add(new Match("package2", "class2", 35L, "PMD", "4.0",
				"ShortVariable"));
		service.mergeAuditTrails(mergeReq);
		assertEquals(trail, newTrail);
		AuditTrails auditTrails = new AuditTrails();
		auditTrails.setProject("sierra-entity");
		List<AuditTrail> trails = new ArrayList<AuditTrail>();
		auditTrails.setAuditTrail(trails);
		AuditTrail auditTrail = new AuditTrail();
		trails.add(auditTrail);
		auditTrail.setTrail(trail);
		List<Audit> audits = new ArrayList<Audit>();
		auditTrail.setAudits(audits);
		Audit audit = new Audit(new Date(), "Some comment", AuditEvent.COMMENT);
		audits.add(audit);
		service.commitAuditTrails(auditTrails);
		AuditTrailRequest request = new AuditTrailRequest();
		request.setProject("sierra-entity");
		request.setQualifier("Default");
		request.setRevision(0L);
		AuditTrailResponse response = service.getAuditTrails(request);
		AuditTrailUpdate update = response.getUpdate().get(0);
		assertEquals(audit, update.getAudit().get(0));
		assertEquals(2, update.getMatch().size());
	}

	private static InputStream getResource(String name) {
		return PublishMessageRunGenerator.class
				.getResourceAsStream("/com/surelogic/sierra/tool/message/"
						+ name);
	}
}
