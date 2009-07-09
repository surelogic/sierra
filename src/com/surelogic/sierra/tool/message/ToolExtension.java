package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.*;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public final class ToolExtension {
	private String tool;
	private String id; 
	private String version;
	
	public void setTool(String value) {
		tool = value;
	}
	
	public String getTool() {
		return tool;
	}

	public void setId(String value) {
		id = value;
	}
	
	public String getId() {
		return id;
	}
	
	public void setVersion(String value ) {
		version = value;
	}
	
	public String getVersion() {
		return version;
	}
}
