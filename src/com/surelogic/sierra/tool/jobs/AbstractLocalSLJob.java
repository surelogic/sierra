package com.surelogic.sierra.tool.jobs;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.*;

import com.surelogic.common.jobs.AbstractSLJob;
import com.surelogic.common.jobs.JobException;
import com.surelogic.common.jobs.Local;
import com.surelogic.common.jobs.Remote;
import com.surelogic.common.jobs.SLJobConstants;
import com.surelogic.common.jobs.SLProgressMonitor;
import com.surelogic.common.jobs.SLStatus;
import com.surelogic.common.jobs.SubSLProgressMonitor;
import com.surelogic.common.jobs.TestCode;
import com.surelogic.common.logging.SLLogger;

public abstract class AbstractLocalSLJob extends AbstractSLJob {
    protected static final Logger LOG = SLLogger.getLogger();
	private static final int FIRST_LINES = 3;
	
	protected final int work;
	protected final TestCode testCode;
	protected final int memorySize;
	protected final SLStatus.Builder status   = new SLStatus.Builder();
	private Stack<SubSLProgressMonitor> tasks = new Stack<SubSLProgressMonitor>();
	
	protected AbstractLocalSLJob(String name, int work, TestCode code, int memSize) {
		super(name);
		this.work  = work;
		testCode   = code;
		memorySize = memSize;
	}

	protected JobException newException(int number, Object... args) {
		throw new JobException(number, args);
	}
	
	protected void addToPath(Project proj, Path path, String name) {
		addToPath(proj, path, name, true);
	}

	protected boolean addToPath(Project proj, Path path, String name,
			boolean required) {
		final File f = new File(name);
		final boolean exists = f.exists();
		if (!exists) {
			if (required) {
				throw newException(
						SLJobConstants.ERROR_CODE_MISSING_FOR_JOB,
						name);
			}
		} else if (TestCode.MISSING_CODE.equals(testCode)) {
			throw newException(
					SLJobConstants.ERROR_CODE_MISSING_FOR_JOB, name);
		} else {
			path.add(new Path(proj, name));
		}
		return exists;
	}
	
	protected void findJars(Project proj, Path path, String folder) {
		findJars(proj, path, new File(folder));
	}

	protected void findJars(Project proj, Path path, File folder) {
		for (File f : folder.listFiles()) {
			String name = f.getAbsolutePath();
			if (name.endsWith(".jar")) {
				path.add(new Path(proj, name));
			}
		}
	}
	
	private String copyException(String type, String msg, BufferedReader br)
	throws IOException {
		SubSLProgressMonitor mon = tasks.peek();
		StringBuilder sb = new StringBuilder(mon.getName() + ' ' + type);
		System.out.println(msg);
		sb.append(": ").append(msg).append('\n');

		String line = br.readLine();
		while (line != null && line.startsWith("\t")) {
			System.out.println(line);
			sb.append(' ').append(line).append('\n');
			line = br.readLine();
		}
		if (line != null) {
			System.out.println(line);
		}
		final String errMsg = sb.toString();
		status.addChild(SLStatus.createErrorStatus(-1, errMsg));
		return errMsg;
	}
	
	public SLStatus run(final SLProgressMonitor topMonitor) {
		final boolean debug = LOG.isLoggable(Level.FINE);
		CommandlineJava cmdj = new CommandlineJava();
		setupJVM(debug, cmdj);

		if (debug) {
			System.out.println("Starting process:");
			for (String arg : cmdj.getCommandline()) {
				System.out.println("\t" + arg);
			}
		}
		ProcessBuilder pb = new ProcessBuilder(cmdj.getCommandline());
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			String firstLine = br.readLine();
			if (debug) {
				while (firstLine != null) {
					// Copy verbose output until we get to the first line
					// from RemoteTool
					if (firstLine.startsWith("[")) {
						// if (!firstLine.endsWith("rt.jar]")) {
						System.out.println(firstLine);
						// }
						firstLine = br.readLine();
					} else {
						break;
					}
				}
			}
			System.out.println("First line = " + firstLine);

			if (firstLine == null) {
				throw newException(SLJobConstants.ERROR_NO_OUTPUT_FROM_JOB);
			}
			final String[] firstLines = new String[FIRST_LINES];
			int numLines = 1;
			firstLines[0] = firstLine;

			// Copy any output
			final PrintStream pout = new PrintStream(p.getOutputStream());
			if (TestCode.SCAN_CANCELLED.equals(testCode)) {
				cancel(p, pout);
			}
			topMonitor.begin(work);
			
			String line = br.readLine();
			loop: while (line != null) {
				final SLProgressMonitor monitor = 
					tasks.isEmpty() ? topMonitor : tasks.peek();
				
				if (numLines < FIRST_LINES) {
					firstLines[numLines] = line;
					numLines++;
				}
				if (monitor.isCanceled()) {
					cancel(p, pout);
				}

				if (line.startsWith("##")) {
					StringTokenizer st = new StringTokenizer(line, "#,");
					if (st.hasMoreTokens()) {
						String first = st.nextToken();
						switch (Remote.valueOf(first)) {
						case TASK:
							System.out.println(line);
							final String task = st.nextToken();
							final String work = st.nextToken();
							// LOG.info(task+": "+work);
							SubSLProgressMonitor mon = new SubSLProgressMonitor(monitor, task, 1);
							tasks.push(mon);
							mon.begin(Integer.valueOf(work.trim()));
							break;
						case SUBTASK:
							monitor.subTask(st.nextToken());
							break;
						case WORK:
							monitor.worked(Integer.valueOf(st.nextToken()
									.trim()));
							break;
						case WARNING:
							System.out.println(line);
							copyException(first, st.nextToken(), br);
							break;
						case FAILED:
							System.out.println(line);
							String msg = copyException(first, st
									.nextToken(), br);
							System.out.println("Terminating run");
							p.destroy();
							if (msg
									.contains("FAILED:  java.lang.OutOfMemoryError")) {
								throw newException(
										SLJobConstants.ERROR_MEMORY_SIZE_TOO_SMALL,
										memorySize);
							}
							throw new RuntimeException(msg);
						case DONE:
							System.out.println(line);
							tasks.pop();
							/*
							if (tasks.isEmpty()) {
								monitor.done();
								break loop;
							}
							*/
							break;
						default:
							System.out.println(line);
						}
					} else {
						System.out.println(line);
					}
				} else {
					System.out.println(line);
				}
				line = br.readLine();
			}
			line = br.readLine();
			if (line != null) {
				System.out.println(line);
			}
			// See if the process already died?
			int value = handleExitValue(p);
			br.close();
			pout.close();
			if (value != 0) {
				examineFirstLines(firstLines);
				throw newException(SLJobConstants.ERROR_PROCESS_FAILED, value);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// FIX status not built if exception thrown
		return status.build();
	}

	protected final void setupJVM(boolean debug, CommandlineJava cmdj) {
		if (testCode != null) {
			cmdj.createVmArgument().setValue(
					"-D" + SLJobConstants.TEST_CODE_PROPERTY + "="
							+ testCode);
		}
		if (TestCode.LOW_MEMORY.equals(testCode)) {
			cmdj.setMaxmemory("2m");
		} else if (TestCode.HIGH_MEMORY.equals(testCode)) {
			cmdj.setMaxmemory("2048m");
		} else if (TestCode.MAD_MEMORY.equals(testCode)) {
			cmdj.setMaxmemory("9999m");
		} else if (memorySize > 0) {
			cmdj.setMaxmemory(memorySize + "m");
		} else {
			cmdj.setMaxmemory("1024m");
		}
		cmdj.createVmArgument().setValue("-XX:MaxPermSize=128m");
		if (false) {
			cmdj.createVmArgument().setValue("-verbose");
		}
		cmdj.setClassname(getRemoteClass().getCanonicalName());
		
		final Project proj = new Project();
		final Path path = cmdj.createClasspath(proj);
		setupClassPath(debug, proj, path);
		// TODO convert into error if things are really missing
		if (debug) {
			for (String p : path.list()) {
				if (!new File(p).exists()) {
					System.out.println("Does not exist: " + p);
				} else if (debug) {
					System.out.println("Path: " + p);
				}
			}
		}		
		//cmdj.createArgument().setValue("This is a argument.");
		
		finishSetupJVM(debug, cmdj);
	}
	
	protected abstract Class<?> getRemoteClass();
	
	protected abstract void setupClassPath(boolean debug, Project proj, Path path);
	
	protected abstract void finishSetupJVM(boolean debug, CommandlineJava cmdj);
	
	private void cancel(Process p, final PrintStream pout) {
		pout.println("##" + Local.CANCEL);
		p.destroy();
		throw newException(SLJobConstants.ERROR_JOB_CANCELLED);
	}

	private void examineFirstLines(String[] firstLines) {
		for (String line : firstLines) {
			if (line.startsWith("Could not reserve enough space")
					|| line.startsWith("Invalid maximum heap size")) {
				throw newException(
						SLJobConstants.ERROR_MEMORY_SIZE_TOO_BIG,
						memorySize);
			}
		}
	}
	
	private int handleExitValue(Process p) {
		int value;
		try {
			value = p.exitValue();
			System.out.println("Process result after waiting = " + value);
		} catch (IllegalThreadStateException e) {
			// Not done yet
			final Thread currentThread = Thread.currentThread();
			Thread t = new Thread() {
				public void run() {
					// Set to timeout in 1 minute
					try {
						Thread.sleep(60000);
						currentThread.interrupt();
					} catch (InterruptedException e) {
						// Just end
					}
				}
			};

			final long start = System.currentTimeMillis();
			t.start();
			try {
				value = p.waitFor();
				t.interrupt();
			} catch (InterruptedException ie) {
				long time = System.currentTimeMillis() - start;
				throw new RuntimeException(
						"Timeout waiting for process to exit: " + time
								+ " ms");
			}
			System.out.println("Process result after waiting = " + value);
		}
		return value;
	}
}
