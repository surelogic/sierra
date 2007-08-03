/**
 * 
 */
package com.surelogic.sierra.tool.ant;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.surelogic.sierra.tool.config.Config;

/**
 * @author ethan
 *
 */
public class TestSierraAnalysis {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.surelogic.sierra.tool.ant.SierraAnalysis#SierraAnalysis(com.surelogic.sierra.tool.config.Config)}.
	 */
	@Test
	public void testSierraAnalysisConfig() {
		Config config = new Config();
		config.setBaseDirectory("/Users/ethan/sierra-workspace/sierra-tool/");
		config.setJavaVersion("1.4");
		config.setProject("sierra-tool");
		config.setQualifiers(new ArrayList<String>());
		config.setRunDateTime(null);
		config.setToolsDirectory("/Users/ethan/sierra-workspace/sierra-tool/Tools");
		SierraAnalysis sa = new SierraAnalysis(config);
		sa.execute();
	}

}
