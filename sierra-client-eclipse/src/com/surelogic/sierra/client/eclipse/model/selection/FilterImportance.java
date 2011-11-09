package com.surelogic.sierra.client.eclipse.model.selection;

import java.util.Arrays;

import org.eclipse.swt.graphics.Image;

import com.surelogic.sierra.client.eclipse.Utility;
import com.surelogic.sierra.tool.message.Importance;

public final class FilterImportance extends Filter {

	public static final ISelectionFilterFactory FACTORY = new AbstractFilterFactory() {
		public Filter construct(Selection selection, Filter previous) {
			return new FilterImportance(selection, previous);
		}

		public String getFilterLabel() {
			return "Importance";
		}
	};

	FilterImportance(Selection selection, Filter previous) {
		super(selection, previous);
	}

	@Override
	public ISelectionFilterFactory getFactory() {
		return FACTORY;
	}

	@Override
	protected String getColumnName() {
		return "IMPORTANCE";
	}

	@Override
	public Image getImageFor(String value) {
		Importance aType = Importance.fromValue(value);
		return Utility.getImageFor(aType);
	}
	
	public static final String CRITICAL = "Critical";
	public static final String HIGH = "High";
	public static final String MEDIUM = "Medium";
	public static final String LOW = "Low";
	public static final String IRRELEVANT = "Irrelevant";

	@Override
	protected void deriveAllValues() {
		String[] values = new String[] { CRITICAL, HIGH, MEDIUM, LOW,
				IRRELEVANT };
		synchronized (this) {
			f_allValues.clear();
			f_allValues.addAll(Arrays.asList(values));
		}
	}
}
