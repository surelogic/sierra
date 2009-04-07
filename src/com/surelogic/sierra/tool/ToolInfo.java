package com.surelogic.sierra.tool;

class ToolInfo {
	private static final String HTTP_PREFIX = "http://";
	
	final String id;
	final String version;
	final String name;
	final String description;
	
	ToolInfo(String id, String version, String name, String website, String description) {
		this.id = id;
		this.version = version == null ? "?.?" : version;
		this.name = name == null ? id : name;
		
		if (description == null) {
			this.description = "No description available.";
		} else {		
			final StringBuilder sb = new StringBuilder();
			if (website == null) {
				sb.append(name);
			} else {
				sb.append("<A HREF=\"");
				if (!website.startsWith(HTTP_PREFIX)) {
					sb.append(HTTP_PREFIX);
				}
				sb.append(website).append("\">").append(name).append("</A>");
			}
			sb.append(" is ");
			
			// Uncapitalize the first letter of the description
			sb.append(Character.toLowerCase(description.charAt(0)));
			sb.append(description, 1, description.length());
			this.description = sb.toString();
		}
	}

	public ToolInfo() {
		this("Unknown", null, null, null, null);
	}
}
