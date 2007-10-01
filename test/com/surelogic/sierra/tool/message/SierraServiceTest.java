package com.surelogic.sierra.tool.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
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
public class SierraServiceTest {

	private SierraService service;

	@Before
	public void setUp() throws Exception {
		service = new SierraServiceClient(new SierraServerLocation("localhost",
				false, 8080, "test", "test")).getSierraServicePort();
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testPublishRun() {
		try {
			JAXBContext context;
			context = JAXBContext.newInstance(Scan.class);
			Unmarshaller um = context.createUnmarshaller();
			InputStream in = getResource("ad-hoc-query.sierra");
			Scan run = (Scan) um.unmarshal(in);
			service.publishRun(run);
		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	public void testGetQualifiers() {
		assertTrue(service.getQualifiers(new QualifierRequest()).getQualifier()
				.contains("Default"));
	}

	@Test
	public void testSingleComment() {

		String server = service.getUid(new ServerUIDRequest()).getUid();
		MergeAuditTrailRequest mergeReq = new MergeAuditTrailRequest();
		mergeReq.setServer(server);
		mergeReq.setProject("sierra-entity");
		List<Merge> merges = new ArrayList<Merge>();
		mergeReq.setMerge(merges);
		Merge merge = new Merge();
		merges.add(merge);
		List<Match> matches = new ArrayList<Match>();
		merge.setMatch(matches);
		matches.add(new Match("package", "class", 34L,
				"SW_SWING_METHODS_INVOKED_IN_SWING_THREAD"));
		try {
			MergeAuditTrailResponse mergeRes = service
					.mergeAuditTrails(mergeReq);
			String trail = mergeRes.getTrail().get(0);
			mergeRes = service.mergeAuditTrails(mergeReq);
			String newTrail = mergeRes.getTrail().get(0);
			assertNotNull(trail);
			assertEquals(trail, newTrail);
			matches.add(new Match("package2", "class2", 35L,
					"SW_SWING_METHODS_INVOKED_IN_SWING_THREAD"));
			mergeRes = service.mergeAuditTrails(mergeReq);
			newTrail = mergeRes.getTrail().get(0);
			assertEquals(trail, newTrail);
			CommitAuditTrailRequest auditTrails = new CommitAuditTrailRequest();
			auditTrails.setServer(server);
			List<AuditTrail> trails = new ArrayList<AuditTrail>();
			auditTrails.setAuditTrail(trails);
			AuditTrail auditTrail = new AuditTrail();
			trails.add(auditTrail);
			auditTrail.setFinding(trail);
			List<Audit> audits = new ArrayList<Audit>();
			auditTrail.setAudits(audits);
			Audit audit = new Audit(null, AuditEvent.COMMENT, "Some comment");
			audits.add(audit);
			service.commitAuditTrails(auditTrails);
			GetAuditTrailRequest request = new GetAuditTrailRequest();
			request.setServer(server);
			request.setProject("sierra-entity");
			request.setRevision(-1L);
			AuditTrailResponse response = service.getAuditTrails(request);
			AuditTrailUpdate update = response.getUpdate().get(0);
			assertEquals(audit.getEvent(), update.getAudit().get(
					update.getAudit().size() - 1).getEvent());
			assertEquals(audit.getValue(), update.getAudit().get(
					update.getAudit().size() - 1).getValue());
			assertEquals(2, update.getMatch().size());
		} catch (ServerMismatchException e) {
			fail();
		}
	}

	private static InputStream getResource(String name) {
		return PublishMessageRunGenerator.class
				.getResourceAsStream("/com/surelogic/sierra/tool/message/"
						+ name);
	}
}
