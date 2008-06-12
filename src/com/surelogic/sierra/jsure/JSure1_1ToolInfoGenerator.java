package com.surelogic.sierra.jsure;

import java.sql.Connection;
import java.sql.SQLException;

import com.surelogic.sierra.jdbc.tool.*;
import com.surelogic.sierra.jdbc.tool.ToolBuilder.ArtifactTypeBuilder;

public class JSure1_1ToolInfoGenerator {
	/**
	 * Load the rulesets and persist them to the embedded database.
	 * 
	 */
	public static void generateTool(Connection conn) {
		try {
			ArtifactTypeBuilder atb = ToolBuilder.getBuilder(conn).build("JSure", "1.1");
			for(String type : artifactTypes) {
				atb.category("JSure");
				atb.info("");
				atb.link("");
				atb.mnemonic(type);
				atb.build();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Problem with JSure tool info", e);
		}
	}
	
	private static final String[] artifactTypes = {
		"JSure",
		// Various kinds of PromiseDrops
		"BorrowedPromiseDrop",
		"UniquePromiseDrop",
		"AggregatePromiseDrop",
		"StartsPromiseDrop",		
		"RegionEffectsPromiseDrop",
		"InRegionPromiseDrop",
		"RegionModel",
		"LockModel",		
		"RequiresLockPromiseDrop",
		"SelfProtectedPromiseDrop",
		"SingleThreadedPromiseDrop",
		"TransparentPromiseDrop",
		"PromisePromiseDrop",
		// Various messages for ResultDrops
		"ThreadEffectsAnalysis_threadEffectDrop",
		"ThreadEffectsAnalysis_callPromiseDrop",
		"LockAnalysis_ds_IndirectFieldAccessNotAssured",
		"LockAnalysis_ds_SynchronizedMethodWarningDetails",
		"LockAnalysis_ds_HeldJUCLock",
		"LockAnalysis_ds_ReturnAssured",
		"LockAnalysis_ds_MatchingUnlock",
		"LockAnalysis_ds_SynchronizedConstructorAssured",
		"ColorSecondPass_inheritedColor",
		"ColorSecondPass_inferredColor",
		"LockAnalysis_ds_OnBehalfOfConstructor",
		"LockAnalysis_ds_FieldAccessNotAssured",
		"LockAnalysis_ds_DeclaredJUCLockField",
		"ThreadEffectsAnalysis_callNotPromiseDrop",
		"LockAnalysis_ds_SyncedJUCLock",
		"LockAnalysis_ds_PreconditionsAssured",
		"LockAnalysis_ds_PreconditionsNotAssured",
		"LockAnalysis_ds_PreconditionsAssuredAlternative",
		"LockAnalysis_ds_PoisonedLockCall",
		"LockAnalysis_ds_RedundantSynchronized",
		"LockAnalysis_ds_FieldAccessAssured",
		"LockAnalysis_ds_FieldAccessNotResolvable",
		"LockAnalysis_ds_MasqueradingCall2",
		"ColorSecondPass_colorContextDrop",
		"ThreadEffectsAnalysis_noThreadsDrop",
		"LockAnalysis_ds_JUCLockFields",
		"LockAnalysis_ds_PreconditionNotResolvable",
		"LockAnalysis_ds_HeldLock",
		"LockAnalysis_ds_SynchronizedConstructorNotAssured",
		"LockAnalysis_ds_FieldDeclaration",
		"LockAnalysis_ds_PoisonedUnlockCall",
		"LockAnalysis_ds_FieldAccessAssuredAlternative",
		"LockAnalysis_ds_NoMatchingLocks",
		"ColorSecondPass_inheritedTransparent",
		"LockAnalysis_ds_AggregationNeeded",
		"LockAnalysis_ds_UnidentifiableLock",
		"LockAnalysis_ds_NonfinalExpression",
		"LockAnalysis_ds_IndirectFieldAccessAssuredAlternative",
		"LockAnalysis_ds_MatchingLock",
		"LockAnalysis_ds_ReturnNotAssured",
		"LockAnalysis_ds_MasqueradingCall",
		"LockAnalysis_ds_SynchronizedStaticMethodWarningDetails",
		"LockAnalysis_ds_SynchronizationUnused",
		"LockAnalysis_ds_IndirectFieldAccessAssured",
		"LockAnalysis_ds_AggregationNeeded2",
		"LockAnalysis_ds_AggregationEvidence",
		"LockAnalysis_ds_NoMatchingUnlocks",
		"UniquenessAssurance_uniquenessContraints1",
		"UniquenessAssurance_uniquenessContraints2",
		"UniquenessAssurance_uniqueReturnDrop",
		"UniquenessAssurance_borrowedParametersDrop",
		"UniquenessAssurance_uniqueParametersDrop",
		"UniquenessAssurance_effectOfCallDrop",
		"UniquenessAssurance_dependencyDrop",
		"UniquenessAssurance_error",
		"UniquenessAssurance",
		"EffectAssurance_msgUnaccountedFor",
		"EffectAssurance_msgCheckedBy",
		"EffectAssurance_msgEmptyEffects",
		"EffectAssurance_msgContructorRule",
	};
}
