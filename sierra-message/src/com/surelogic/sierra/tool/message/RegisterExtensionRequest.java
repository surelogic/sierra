package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RegisterExtensionRequest {
	protected Extension extension;

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(final Extension extension) {
		this.extension = extension;
	}

}
