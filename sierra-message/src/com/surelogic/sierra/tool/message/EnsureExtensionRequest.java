package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class EnsureExtensionRequest {
	protected List<ExtensionName> extensions;

	public List<ExtensionName> getExtensions() {
		if (extensions == null) {
			extensions = new ArrayList<ExtensionName>();
		}
		return extensions;
	}
}
