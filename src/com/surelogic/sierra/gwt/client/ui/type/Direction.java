package com.surelogic.sierra.gwt.client.ui.type;

public enum Direction {
	UP("up"), DOWN("down"), LEFT("left"), RIGHT("right"), SHRINK("shrink"), GROW(
			"grow");

	private String text;

	private Direction(final String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
