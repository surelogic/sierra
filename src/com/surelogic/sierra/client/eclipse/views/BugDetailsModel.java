package com.surelogic.sierra.client.eclipse.views;
//package com.surelogic.spsToolPlugin.views;
//
//import java.io.File;
//
//import com.surelogic.sps.bug.BugEntry;
//import com.surelogic.sps.project.Project;
//import com.surelogic.sps.project.RunEntry;
//
//public class BugDetailsModel {
//	private BugEntry bugDetails[];
//
//	public BugDetailsModel() {
//
//		Project project = new Project();
//		project.setId(1);
//		project.setName("TestFindBugs");
//		project.setBaseDirectory(new File("C:\\work\\FindBugs\\runtime"));
//
//		RunEntry runEntry = new RunEntry();
//
//		runEntry.setId(10);
//		runEntry.setProject(project);
//		runEntry.setTimestamp(null);
//		runEntry.setType(null);
//
//		bugDetails = new BugEntry[2];
//
//		bugDetails[0] = new BugEntry();
//		bugDetails[0].setPackageName("com.surelogic.buggyapp");
//		bugDetails[0].setClassName("Bleep");
//		bugDetails[0].setMethodName("oopsMethod");
//		bugDetails[0].setFieldName(null);
//		bugDetails[0].setLineNumber(51);
//		bugDetails[0].setRunEntry(runEntry);
//
//		bugDetails[1] = new BugEntry();
//		bugDetails[1].setPackageName("com.surelogic.buggyapp");
//		bugDetails[1].setClassName("Testy");
//		bugDetails[1].setMethodName("uhohMethod");
//		bugDetails[1].setFieldName("myValue");
//		bugDetails[1].setLineNumber(214);
//		bugDetails[1].setRunEntry(runEntry);
//
//	}
//
//	public BugEntry[] getDetails() {
//		return bugDetails;
//	}
//}
