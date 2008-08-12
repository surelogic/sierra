package com.surelogic.sierra.tool;

import java.net.URI;
import java.util.*;

import com.surelogic.common.jobs.*;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class MultiTool extends AbstractTool {
	protected List<ITool> tools = new ArrayList<ITool>();

	public MultiTool(boolean debug) {
		super("MultiTool", "1.0", "MultiTool", "A container for other tools",
				debug);
	}

	public Set<String> getArtifactTypes() {
		return Collections.emptySet();
	}

	protected IToolInstance create(final ArtifactGenerator generator,
			boolean close) {
		return new Instance(debug, this, generator, close);
	}

	public void addTool(ITool t) {
		if (t != null && !tools.contains(t)) {
			tools.add(t);
		}
	}

	private static class Instance extends MultiTool implements IToolInstance {
		private final List<IToolInstance> instances = new ArrayList<IToolInstance>();
		private IToolInstance first = null;

		private final ArtifactGenerator generator;
		private final boolean closeWhenDone;

		Instance(boolean debug, MultiTool mt, ArtifactGenerator gen,
				boolean close) {
			super(debug);
			for (ITool tool : mt.tools) {
				this.tools.add(tool);

				IToolInstance i = tool.create(gen);
				instances.add(i);
				if (first == null) {
					first = i;
				}
			}
			generator = gen;
			closeWhenDone = close;
		}

		private void init(SLProgressMonitor mon) {
			mon.begin(100);
			mon.subTask("Setting up scans");
		}

		public SLStatus run(final SLProgressMonitor mon) {
			SLStatus.Builder status = new SLStatus.Builder();
			init(mon);

			for (IToolInstance i : instances) {
				if (mon.isCanceled()) {
					status.addChild(SLStatus.CANCEL_STATUS);
					break;
				}
				System.out.println("run() on " + i.getName());
				SLStatus s = AbstractSLJob.invoke(i, mon, 1);
				status.addChild(s);
			}
			if (closeWhenDone) {
				generator.finished(mon);
				mon.done();
			}
			return status.build();
		}

		public void addTarget(IToolTarget target) {
			for (IToolInstance i : instances) {
				i.addTarget(target);
			}
		}

		public void addToClassPath(URI loc) {
			for (IToolInstance i : instances) {
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
			for (IToolInstance i : instances) {
				i.setOption(key, value);
			}
		}
	}
}
