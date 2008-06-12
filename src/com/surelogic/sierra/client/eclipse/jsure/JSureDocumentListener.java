package com.surelogic.sierra.client.eclipse.jsure;

import static com.surelogic.jsure.xml.JSureXMLReader.CUNIT_ATTR;
import static com.surelogic.jsure.xml.JSureXMLReader.HASH_ATTR;
import static com.surelogic.jsure.xml.JSureXMLReader.IR_DROP;
import static com.surelogic.jsure.xml.JSureXMLReader.MESSAGE_ATTR;
import static com.surelogic.jsure.xml.JSureXMLReader.PKG_ATTR;
import static com.surelogic.jsure.xml.JSureXMLReader.PROMISE_DROP;
import static com.surelogic.jsure.xml.JSureXMLReader.PROVED_ATTR;
import static com.surelogic.jsure.xml.JSureXMLReader.RESULT_ATTR;
import static com.surelogic.jsure.xml.JSureXMLReader.RESULT_DROP;
import static com.surelogic.jsure.xml.JSureXMLReader.TYPE_ATTR;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.jsure.xml.AbstractXMLResultListener;
import com.surelogic.jsure.xml.Entity;
import com.surelogic.jsure.xml.SourceRef;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.AssuranceType;
import com.surelogic.sierra.tool.message.IdentifierType;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.ScanGenerator;
import com.surelogic.sierra.tool.message.Severity;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.message.ArtifactGenerator.SourceLocationBuilder;

public class JSureDocumentListener extends AbstractXMLResultListener {
	final ScanGenerator generator;
	final SLProgressMonitor monitor;
	ArtifactGenerator aGenerator;
	ArtifactBuilder builder;

	public JSureDocumentListener(ScanGenerator gen, SLProgressMonitor mon) {
		generator = gen;
		monitor = mon;
	}

	@Override
	public void start(String uid, String project) {
		generator.uid(uid);
		generator.project(project);
		aGenerator = generator.build();
		builder = aGenerator.artifact();
	}

	public ArtifactGenerator getArtifactGenerator() {
		return aGenerator;
	}

	@Override
	protected boolean define(final int id, Entity e) {
		// Only build for Sierra-like entities
		final String name = e.getName();
		final boolean warning;
		final String aType;
		AssuranceType assuranceType = null;

		final boolean isResultDrop = RESULT_DROP.equals(name);
		if (isResultDrop || PROMISE_DROP.equals(name)) {
			final String consistent = e.getAttribute(PROVED_ATTR);			
			warning = !"true".equals(consistent);
			assuranceType = warning ? AssuranceType.INCONSISTENT : AssuranceType.CONSISTENT;

			if (isResultDrop) {
				aType = e.getAttribute(RESULT_ATTR);
			} else {
				final String type = e.getAttribute(TYPE_ATTR);
				aType = "MethodControlFlow".equals(type) ? "UniquenessAssurance"
						: type;
			}
		} else if (IR_DROP.equals(name)) {
			final String type = e.getAttribute(TYPE_ATTR);
			warning = "WarningDrop".equals(type);

			final String result = e.getAttribute(RESULT_ATTR);
			aType = result != null ? result : "JSure";
		} else {
			return false;
		}
		if (createSourceLocation(builder.primarySourceLocation(), e.getSource())) {
			final String msg = e.getAttribute(MESSAGE_ATTR);
			builder.message(msg);
			if (warning) {
				builder.severity(Severity.WARNING).priority(Priority.HIGH);
			} else {
				builder.severity(Severity.INFO).priority(Priority.LOW);
			}
			builder.findingType("JSure", "1.1", aType);
			builder.scanNumber(id);
			builder.assurance(assuranceType);
			// e.getAttribute(CATEGORY_ATTR));
			builder.build();
			return true;
		}
		return false;
	}

	private boolean createSourceLocation(SourceLocationBuilder loc, SourceRef s) {
		if (s != null) {
			final String cu = s.getAttribute(CUNIT_ATTR);
			loc.compilation(cu);
			if (cu.endsWith(".java")) {
	       loc.className(cu.substring(0, cu.length() - 5));
			} else {
			  loc.className(cu);
			}
			loc.packageName(s.getAttribute(PKG_ATTR));

			final int line = Integer.parseInt(s.getLine());
			loc.lineOfCode(line);
			loc.endLine(line);
			loc.hash(Long.decode(s.getAttribute(HASH_ATTR)));
			loc.identifier("unknown");
			loc.type(IdentifierType.CLASS);
			loc.build();
			return true;
		}
		return false;
	}

	@Override
	protected void handleRef(String from, int fromId, Entity to) {
		System.out.println("Handled " + to + " ref from " + from + " to "	+ to.getId());
		aGenerator.relation(fromId, Integer.valueOf(to.getId()), to.getName());
	}
	/*
	 * @Override public void done() { }
	 */
}
