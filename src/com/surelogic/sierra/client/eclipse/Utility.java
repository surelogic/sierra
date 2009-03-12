package com.surelogic.sierra.client.eclipse;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.swt.graphics.Image;

import com.surelogic.common.CommonImages;
import com.surelogic.common.eclipse.SLImages;
import com.surelogic.common.jdbc.ConnectionQuery;
import com.surelogic.sierra.jdbc.tool.ArtifactTypeDO;
import com.surelogic.sierra.jdbc.tool.FindingTypes;
import com.surelogic.sierra.tool.*;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.Importance;

public final class Utility {

	private Utility() {
		// no instances
	}

	public static Image getImageFor(Importance importance) {
		final String imageName;
		if (importance == Importance.IRRELEVANT)
			imageName = CommonImages.IMG_ASTERISK_ORANGE_0;
		else if (importance == Importance.LOW)
			imageName = CommonImages.IMG_ASTERISK_ORANGE_25;
		else if (importance == Importance.MEDIUM)
			imageName = CommonImages.IMG_ASTERISK_ORANGE_50;
		else if (importance == Importance.HIGH)
			imageName = CommonImages.IMG_ASTERISK_ORANGE_75;
		else
			imageName = CommonImages.IMG_ASTERISK_ORANGE_100;
		return SLImages.getImage(imageName);
	}

	public static void checkForNewArtifactTypes() {
		try {
			// Get known artifact types
			final Connection c = Data.getInstance().readOnlyConnection();
			final FindingTypes ft = new FindingTypes(new ConnectionQuery(c));
			final Config config = new Config();		
			final Set<ArtifactType> knownTypes = new HashSet<ArtifactType>();
			for(ITool t : ToolUtil.createTools(config)) {
				List<ArtifactTypeDO> temp = ft.getToolArtifactTypes(t.getName(), t.getVersion());
				for(ArtifactTypeDO a : temp) {
					knownTypes.add(new ArtifactType(a.getTool(), a.getVersion(), "", a.getMnemonic(), ""));
				}				                          
			}
			final Set<ArtifactType> types = ToolUtil.getArtifactTypes();
			final List<ArtifactType> unknown = new ArrayList<ArtifactType>();
			for(ArtifactType a : types) {
				if (!knownTypes.contains(a)) {
					unknown.add(a);
				}
			}
			if (unknown.isEmpty()) {
				System.out.println("No new artifact types");
			} else {
				Collections.sort(unknown);
				for(ArtifactType a : unknown) {
					System.out.println("Couldn't find "+a.type+" for "+a.tool+", v"+a.version);
				}
			}
			// FIX Show dialog
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
