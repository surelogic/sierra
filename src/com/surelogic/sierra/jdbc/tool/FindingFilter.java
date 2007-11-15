package com.surelogic.sierra.jdbc.tool;

import com.surelogic.sierra.tool.message.Importance;
import com.surelogic.sierra.tool.message.Priority;
import com.surelogic.sierra.tool.message.Severity;

/**
 * An implementation of FindingFilter specifies policy as to whether a given
 * artifact type should be matched to a filter, and what the importance of a
 * finding should be.
 * 
 * @author nathan
 * 
 */
public interface FindingFilter {

	boolean accept(Long artifactTypeId);

	Importance calculateImportance(Long findingTypeId, Priority priority,
			Severity severity);

}