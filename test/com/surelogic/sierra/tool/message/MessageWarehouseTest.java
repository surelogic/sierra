package com.surelogic.sierra.tool.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class MessageWarehouseTest {

	MessageWarehouse mw;

	@Before
	public void setUp() {
		this.mw = MessageWarehouse.getInstance();
	}

	@Test
	public final void testEquality() {
		ToolOutput t = mw.fetchToolOutput(MessageWarehouseTest.class
				.getResourceAsStream("jasen-0.9.xml.parsed"));
		try {
			File f = File.createTempFile("some-temp-file", "parsed");
			f.deleteOnExit();
			mw.writeToolOutput(t, f.getPath());
			assertEquals(t, mw.fetchToolOutput(new FileInputStream(f)));

		} catch (IOException e) {
			fail();
		}
	}

}
