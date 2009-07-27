package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DownloadExtensionRequest {
	protected ExtensionName extension;

	public DownloadExtensionRequest() {
	}

	public DownloadExtensionRequest(final ExtensionName extension) {
		this.extension = extension;
	}

	public ExtensionName getExtension() {
		return extension;
	}

	public void setExtension(final ExtensionName extension) {
		this.extension = extension;
	}

}
