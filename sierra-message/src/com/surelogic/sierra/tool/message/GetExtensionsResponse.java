package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GetExtensionsResponse {
	protected List<Extension> extensions;

	public List<Extension> getExtension() {
		if (extensions == null) {
			extensions = new ArrayList<Extension>();
		}
		return extensions;
	}

}
