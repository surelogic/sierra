package com.surelogic.sierra.tool.analyzer;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.surelogic.sierra.tool.message.MessageArtifactGenerator;

public class ParserTest {

	private Parser parser;
	private MessageArtifactGenerator generator;

	@Before
	public void setUp() throws Exception {
		generator = new MessageArtifactGenerator();
		parser = new Parser(generator);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParsePMD() {
		fail("Not yet implemented");
	}

	@Test
	public void testParseFB() {
		parser.parseFB(ParserTest.class.getResource("fb.xml").getFile(),
				new String[] {});
		
	}

}
