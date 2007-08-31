package com.surelogic.sierra.tool.message;

import static org.junit.Assert.fail;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

public class PublishMessageRunGeneratorTest {

	@Test
	public void testPublishRun() {
		try {
			JAXBContext context = JAXBContext.newInstance(Scan.class);
			Unmarshaller um = context.createUnmarshaller();
			InputStream in = getResource("sierra-entity.xml.parsed");
			Scan run = (Scan) um.unmarshal(in);
			PublishMessageRunGenerator generator = new PublishMessageRunGenerator();
			MessageWarehouse.readScan(run, generator);
		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	private static InputStream getResource(String name) {
		return PublishMessageRunGenerator.class
				.getResourceAsStream("/com/surelogic/sierra/tool/message/"
						+ name);
	}
}
