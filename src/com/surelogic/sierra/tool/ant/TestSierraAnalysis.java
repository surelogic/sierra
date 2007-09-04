package com.surelogic.sierra.tool.ant;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.surelogic.sierra.tool.config.Config;

public class TestSierraAnalysis {
	private Config config;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		config = new Config();
		config.setProject("fluid");
		config.setDestDirectory(new File("/Users/ethan/test-sandbox-3.2"));
		config.setBaseDirectory(new File("/Users/ethan/workspace/fluid"));
		config.setToolsDirectory(new File(
				"/Users/ethan/sierra-workspace/sierra-tool/Tools"));
		config.setRunDocument(new File(config.getDestDirectory(),
				"testRun.xml.parsed"));
		config.setMultithreaded(true);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testStop() {
		File run = config.getRunDocument();
		if (run.exists()) {
			run.delete();
		}
		assertFalse(config.getRunDocument().exists());

		SierraAnalysis sa = new SierraAnalysis(config, null);
		new Thread(new Stopper(sa)).start();

		sa.execute();// should wait

		assertFalse(config.getRunDocument().exists());
	}

}

class Stopper implements Runnable {

	SierraAnalysis sa = null;

	public Stopper(SierraAnalysis sa) {
		this.sa = sa;
	}

	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			sa.stop();
		}
	}
}