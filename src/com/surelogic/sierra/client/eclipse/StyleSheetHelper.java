package com.surelogic.sierra.client.eclipse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

import com.surelogic.common.logging.SLLogger;

public final class StyleSheetHelper {

	private static final StyleSheetHelper INSTANCE = new StyleSheetHelper();

	public static StyleSheetHelper getInstance() {
		return INSTANCE;
	}

	public String getStyleSheet() {
		return f_styleSheet.toString();
	}

	private final StringBuilder f_styleSheet;

	private StyleSheetHelper() {
		// singleton
		f_styleSheet = new StringBuilder();
		loadStyleSheet(f_styleSheet);
	}

	private void loadStyleSheet(final StringBuilder styleSheet) {
		Bundle bundle = Activator.getDefault().getBundle();
		URL styleSheetURL = bundle.getEntry("/lib/DetailsViewStyleSheet.css");
		if (styleSheetURL == null)
			return;

		try {
			styleSheetURL = FileLocator.toFileURL(styleSheetURL);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					styleSheetURL.openStream()));
			StringBuilder buffer = new StringBuilder(200);
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line);
				buffer.append('\n');
				line = reader.readLine();
			}

			// FontData fontData = JFaceResources.getFontRegistry().getFontData(
			// PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
			styleSheet.append(buffer.toString());
		} catch (IOException ex) {
			SLLogger.getLogger().log(Level.SEVERE,
					"Failure loading style sheet for details view.", ex);
			return;
		}
	}

}
