package com.surelogic.sierra.tool.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import com.surelogic.sierra.tool.config.Config;

public class MessageWarehouseTest {

	MessageWarehouse mw;

	@Before
	public void setUp() {
		this.mw = MessageWarehouse.getInstance();
	}

	@Test
	public final void testEquality() {
		ToolOutput t = mw.fetchToolOutput(MessageWarehouseTest.class
				.getResourceAsStream("sierra-entity.xml.parsed"));
		try {
			File f = File.createTempFile("some-temp-file", "parsed");
			f.deleteOnExit();
			mw.writeToolOutput(t, f.getPath());
			assertEquals(t, mw.fetchToolOutput(new FileInputStream(f)));

		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public final void testWrite() {
		try {
			JAXBContext ctx = JAXBContext.newInstance(Run.class);
			File f = new File("/home/dandelion/test.xml");
			FileOutputStream out = new FileOutputStream(f);
			Run r = new Run();
			r.setUid(UUID.randomUUID().toString());
			r.setConfig(new Config());
			r.setToolOutput(new ToolOutput());
			ctx.createMarshaller().marshal(r, out);
			out.flush();
			out.close();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
