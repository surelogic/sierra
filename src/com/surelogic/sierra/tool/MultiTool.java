package com.surelogic.sierra.tool;

import java.io.File;
import java.net.URI;
import java.util.*;

import com.surelogic.common.jobs.*;
import com.surelogic.sierra.tool.analyzer.ILazyArtifactGenerator;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.targets.IToolTarget;

public class MultiTool extends AbstractTool implements Iterable<ITool> {
	protected List<ITool> tools = new ArrayList<ITool>();

	public MultiTool(Config config) {
		super("MultiTool", "1.0", "MultiTool", "A container for other tools", config);
	}

	public Iterator<ITool> iterator() {
		return tools.iterator();
	}
	
	public Set<ArtifactType> getArtifactTypes() {
		Set<ArtifactType> merged = new HashSet<ArtifactType>();
		for(ITool t : tools) {
			merged.addAll(t.getArtifactTypes());
		}
		return merged;
	}
	
	@Override
	public List<File> getRequiredJars() {
		List<File> merged = new ArrayList<File>();
		for(ITool t : tools) {
			merged.addAll(t.getRequiredJars());
		}
		return merged;
	}

	@Override
	protected IToolInstance create(String name, ILazyArtifactGenerator generator,
			boolean close) {
		return new Instance(config, this, name, generator, close);
	}

	public void addTool(ITool t) {
		if (t != null && !tools.contains(t)) {
			tools.add(t);
		}
	}

	public int size() {
		return tools.size();
	}
	
	private static class Instance extends MultiTool implements IToolInstance {
		private final List<IToolInstance> instances = new ArrayList<IToolInstance>();
		private IToolInstance first = null;

		private final ILazyArtifactGenerator genHandle;
		private final boolean closeWhenDone;

		Instance(Config config, MultiTool mt, String name,
				 ILazyArtifactGenerator gen, boolean close) {
			super(config);
			for (ITool tool : mt.tools) {
				this.tools.add(tool);

				IToolInstance i = tool.create(name, gen);
				instances.add(i);
				if (first == null) {
					first = i;
				}
			}
			genHandle = gen;
			closeWhenDone = close;
		}

		private void init(SLProgressMonitor mon) {
			mon.begin(100*(instances.size()+1));
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
				SLStatus s = AbstractSLJob.invoke(i, mon, 100);
				status.addChild(s);
			}
			if (closeWhenDone) {
				if (mon.isCanceled()) {
					status.addChild(SLStatus.CANCEL_STATUS);
				} else {
					SLJob j = new AbstractSLJob("Closing results file") {
						public SLStatus run(SLProgressMonitor monitor) {
							ArtifactGenerator generator = genHandle.create(Instance.this);
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
