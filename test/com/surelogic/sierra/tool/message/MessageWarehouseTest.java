package com.surelogic.sierra.tool.message;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.ws.ServiceMode;

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
				.getResourceAsStream("FB--jEdit-4.3pre6.xml.parsed"));
		try {
			File f = File.createTempFile("FB-temp-file", "parsed");
			f.deleteOnExit();
			mw.writeToolOutput(t, f.getPath());
			assertEquals(t, mw.fetchToolOutput(new FileInputStream(f)));
			
		} catch (IOException e) {
			fail();
		}
	}

}
