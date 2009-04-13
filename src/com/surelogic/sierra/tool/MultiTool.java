package com.surelogic.sierra.tool;

import java.net.URI;
import java.util.*;

import com.surelogic.common.jobs.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class MultiTool extends AbstractSLJob implements IToolInstance, Iterable<IToolInstance> {
	public static final IToolFactory factory = 
		new DummyToolFactory("MultiTool", "1.0", "MultiTool", "A container for other tools");

	protected List<IToolInstance> tools = new ArrayList<IToolInstance>();
	private IToolInstance first = null;

	private final Config config;
	private final ILazyArtifactGenerator genHandle;
	private final boolean closeWhenDone;

	public MultiTool(Config config) {
		this(config, true);
	}
	
	private MultiTool(Config config, boolean close) {
		super(factory.getId());
		this.config = config;
		genHandle = AbstractToolFactory.createGenerator(config);
		closeWhenDone = close;		
	}

	public Iterator<IToolInstance> iterator() {
		return tools.iterator();
	}

	public void addTool(IToolFactory f) {
		if (f != null) {
			final IToolInstance t = f.create(config, genHandle, f.getId());
			addTool(t);
		}
	}
		
	public void addTool(IToolInstance t) {
		if (t != null && !tools.contains(t)) {
			if (first == null) {
				first = t;
			}
			tools.add(t);
		}
	}

	public final String getHTMLInfo() {
		return factory.getHTMLInfo();
	}

	public final String getId() {
		return factory.getId();
	}

	public final String getName() {
		return factory.getName();
	}

	public final String getVersion() {
		return factory.getVersion();
	}
	
	public int size() {
		return tools.size();
	}

	private void init(SLProgressMonitor mon) {
		mon.begin(100*(tools.size()+1));
		mon.subTask("Setting up scans");
	}

	public SLStatus run(final SLProgressMonitor mon) {
		SLStatus.Builder status = new SLStatus.Builder();
		init(mon);

		for (IToolInstance i : tools) {
			if (mon.isCanceled()) {
				status.addChild(SLStatus.CANCEL_STATUS);
				break;
			}
			System.out.println("run() on " + i.getName());
			SLStatus s = AbstractSLJob.invoke(i, mon, 100);
			status.addChild(s);
		}
		if (closeWhenDone) {
			if (mon.isCanceled()) {
				status.addChild(SLStatus.CANCEL_STATUS);
			} else {
				SLJob j = new AbstractSLJob("Closing results file") {
					public SLStatus run(SLProgressMonitor monitor) {
						ArtifactGenerator generator = genHandle.create(factory);
						generator.finished(monitor);
						genHandle.finished();
						return SLStatus.OK_STATUS;
					}

				};
				SLStatus s = AbstractSLJob.invoke(j, mon, 100);
				status.addChild(s);
			}
		}
		mon.done();
		return status.build();
	}

	public void addTarget(IToolTarget target) {
		for (IToolInstance i : tools) {
			i.addTarget(target);
		}
	}

	public void addToClassPath(URI loc) {
		for (IToolInstance i : tools) {
			i.addToClassPath(loc);
		}
	}

	public ArtifactGenerator getGenerator() {
		return first != null ? first.getGenerator() : null;
	}

	public void reportError(String msg, Throwable t) {
		if (first != null) {
			first.reportError(msg, t);
		}
	}

	public void reportError(String msg) {
		if (first != null) {
			first.reportError(msg);
		}
	}

	public void setOption(String key, String value) {
		for (IToolInstance i : tools) {
			i.setOption(key, value);
		}
	}
}
