package com.surelogic.sierra.client.eclipse.views;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

import com.surelogic.common.core.EclipseUtility;
import com.surelogic.common.logging.SLLogger;
import com.surelogic.common.ui.jobs.SLUIJob;
import com.surelogic.sierra.client.eclipse.actions.SynchronizeAllProjectsAction;
import com.surelogic.sierra.client.eclipse.jobs.ServerProjectGroupJob;
import com.surelogic.sierra.client.eclipse.model.ConnectedServerManager;
import com.surelogic.sierra.client.eclipse.model.ServerSyncType;
import com.surelogic.sierra.client.eclipse.preferences.ServerFailureReport;
import com.surelogic.sierra.client.eclipse.preferences.SierraPreferencesUtility;
import com.surelogic.sierra.jdbc.settings.ConnectedServer;

public class SierraServersAutoSync {
  private static final AtomicLong lastServerUpdateTime = new AtomicLong(System.currentTimeMillis());

  static final AtomicReference<ServerProjectGroupJob> lastSyncGroup = new AtomicReference<>();

  private static AutoJob f_doServerAutoSync = null;

  static void asyncSyncWithServer(final ServerSyncType type, final int delay) {
    asyncSyncWithServer(type, delay, SierraPreferencesUtility.getServerFailureReporting());
  }

  static void asyncSyncWithServer(final ServerSyncType type, final int delay, final ServerFailureReport reporting) {
    final long now = System.currentTimeMillis();
    lastServerUpdateTime.set(now); // Sync >> update
    SLLogger.getLogger().fine("Sync at: " + now);

    final UIJob job = new SLUIJob() {
      @Override
      public IStatus runInUIThread(final IProgressMonitor monitor) {
        final Job group = lastSyncGroup.get();
        if ((group == null) || (group.getResult() != null)) {
          final SynchronizeAllProjectsAction sync = new SynchronizeAllProjectsAction(type, reporting, false, delay);
          sync.run(null);
          lastSyncGroup.set(sync.getGroup());
        } else {
          System.out.println("Last sync is still running");
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  public static synchronized void start() {
    if (f_doServerAutoSync == null) {
      f_doServerAutoSync = new AutoJob();
      f_doServerAutoSync.schedule(f_doServerAutoSync.getDelay());
    }
  }

  public static synchronized void stop() {
    if (f_doServerAutoSync != null) {
      f_doServerAutoSync.stop();
      f_doServerAutoSync = null;
    }
  }

  private static class AutoJob extends Job {
    final AtomicLong lastTime;
    boolean enabled = true;

    public AutoJob() {
      super("Server auto-sync");
      setSystem(true);
      lastTime = SynchronizeAllProjectsAction.getLastSyncTime();
    }

    protected long computeGap() {
      final long now = System.currentTimeMillis();
      final long next = lastTime.get() + getDelay();
      return next - now;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
      if (isEnabled()) {
        final long gap = computeGap();
        if (gap > 0) {
          System.out.println("Wait a bit longer: " + gap);
          schedule(gap);
          return Status.OK_STATUS;
        }
        // No need to wait ...
        run();
      }
      schedule(getDelay());

      return Status.OK_STATUS;
    }

    protected boolean isEnabled() {
      return enabled;
    }

    public void stop() {
      enabled = false;
      super.cancel();
    }

    // In msec
    protected long getDelay() {
      final int serverInteractionPeriodInMinutes = EclipseUtility
          .getIntPreference(SierraPreferencesUtility.SERVER_INTERACTION_PERIOD_IN_MINUTES);
      return serverInteractionPeriodInMinutes * 60000;
    }

    protected void run() {
      // Check if any servers have auto-sync on
      boolean autoSync = false;
      for (ConnectedServer s : ConnectedServerManager.getInstance().getServers()) {
        if (s.getLocation().isAutoSync()) {
          autoSync = true;
          break;
        }
      }
      if (autoSync) {
        SierraServersAutoSync.asyncSyncWithServer(ServerSyncType.BY_SERVER_SETTINGS, 2000);
      }
    }
  }
}
