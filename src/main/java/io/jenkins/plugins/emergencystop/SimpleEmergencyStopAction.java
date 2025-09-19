package io.jenkins.plugins.emergencystop;

import hudson.Extension;
import hudson.model.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import jenkins.model.CauseOfInterruption;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.POST;

@Extension
public class SimpleEmergencyStopAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(SimpleEmergencyStopAction.class.getName());

    private int stoppedPipelines;
    private int notStoppedPipelines;

    public int getStoppedPipelines() {
        return stoppedPipelines;
    }

    public int getNotStoppedPipelines() {
        return notStoppedPipelines;
    }

    @Override
    public String getIconFileName() {
        Jenkins jenkins = Jenkins.get();
        return jenkins.hasPermission(Jenkins.ADMINISTER) ? "symbol-warning" : null;
    }

    @Override
    public String getDisplayName() {
        Jenkins jenkins = Jenkins.get();
        return jenkins.hasPermission(Jenkins.ADMINISTER) ? "Emergency STOP Pipelines" : null;
    }

    @Override
    public String getUrlName() {
        Jenkins jenkins = Jenkins.get();
        return jenkins.hasPermission(Jenkins.ADMINISTER) ? "emergency-stop-pipelines" : null;
    }

    @POST
    public void doStop(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException, ServletException {
        Jenkins jenkins = Jenkins.get();

        if (!jenkins.hasPermission(Jenkins.ADMINISTER)) {
            rsp.sendError(403, "You need ADMIN permission to perform this action");
            return;
        }

        this.stoppedPipelines = 0;
        this.notStoppedPipelines = 0;

        try {
            // Stop all running builds
            List<Job> allJobs = jenkins.getAllItems(Job.class);
            for (Job<?, ?> job : allJobs) {
                for (Run<?, ?> build : job.getBuilds()) {
                    if (build.isBuilding()) {
                        Executor executor = build.getExecutor();
                        if (executor != null) {
                            executor.interrupt(
                                    Result.ABORTED,
                                    new CauseOfInterruption.UserInterruption("EMERGENCY STOP by "
                                            + jenkins.getAuthentication2().getName()));
                            this.stoppedPipelines++;
                        } else {
                            LOGGER.warning("Cannot stop build (no executor): " + build.getFullDisplayName());
                            this.notStoppedPipelines++;
                        }
                    }
                }
            }

            // Cancel all queued items
            Queue queue = jenkins.getQueue();
            for (Queue.Item item : queue.getItems()) {
                boolean cancelled = queue.cancel(item);
                if (cancelled) {
                    this.stoppedPipelines++;
                } else {
                    LOGGER.warning("Failed to cancel queued item: " + item.task.getFullDisplayName());
                    this.notStoppedPipelines++;
                }
            }

        } catch (Exception e) {
            LOGGER.severe("Error during emergency stop: " + e.getMessage());
            rsp.sendError(500, "Error during emergency stop: " + e.getMessage());
            return;
        }

        rsp.setContentType("text/html");
        req.setAttribute("stoppedPipelines", this.stoppedPipelines);
        req.setAttribute("notStoppedPipelines", this.notStoppedPipelines);
        req.getView(this, "success.jelly").forward(req, rsp);
    }
}
