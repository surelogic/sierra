package com.surelogic.sierra.client.eclipse.jsure;

import com.surelogic.common.*;
import com.surelogic.jsure.xml.*;
import com.surelogic.sierra.tool.message.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator.ArtifactBuilder;
import com.surelogic.sierra.tool.message.ArtifactGenerator.SourceLocationBuilder;

import static com.surelogic.jsure.xml.JSureXMLReader.*;

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
	protected void define(Entity e) {
		// Only build for Sierra-like entities
		final String name = e.getName();
		final boolean warning;
		if (RESULT_DROP.equals(name) || PROMISE_DROP.equals(name)) {
			final String consistent = e.getAttribute(PROVED_ATTR);
			warning = "true".equals(consistent); 
		} 
		else if (IR_DROP.equals(name)) {	
			final String type = e.getAttribute(TYPE_ATTR);
			warning = "WarningDrop".equals(type);
		} else {
			return;
		}		
		if (createSourceLocation(builder.primarySourceLocation(), e.getSource())) {
			if (warning) {
				builder.severity(Severity.WARNING).priority(Priority.HIGH);
			} else {
				builder.severity(Severity.INFO).priority(Priority.LOW);
			}
			builder.message(e.getAttribute(MESSAGE_ATTR));
			builder.findingType("JSure", "1.9.0", e.getAttribute(CATEGORY_ATTR));
		}
		builder.build();
	}

	private boolean createSourceLocation(SourceLocationBuilder loc, SourceRef s) {
		if (s != null) {
			final String cu = s.getAttribute(CUNIT_ATTR);
			loc.compilation(cu);
			loc.className(cu);
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
	protected void handleDanglingRef(String from, Entity to) {
		// TODO handle
		System.out.println("Handled "+to+" ref from "+from+" to "+to.getId());
	}	
	/*
	@Override
	public void done() {
	}
	*/	
}
