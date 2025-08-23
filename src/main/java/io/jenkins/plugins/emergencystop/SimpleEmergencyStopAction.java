package io.jenkins.plugins.emergencystop;

import hudson.Extension;
import hudson.model.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jenkins.model.CauseOfInterruption;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.POST;

@Extension
public class SimpleEmergencyStopAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(SimpleEmergencyStopAction.class.getName());

    public int stoppedPipelines = 0;
    
    public int getStoppedPipelines() {
        return stoppedPipelines;
    }

    @Override
    public String getIconFileName() {
        Jenkins jenkins = Jenkins.get();
        if (jenkins != null && jenkins.hasPermission(Item.CANCEL)) {
            return "warning.png";
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Emergency STOP Pipelines";
    }

    @Override
    public String getUrlName() {
        return "emergency-stop";
    }

    @POST
    public void doIndex(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException, ServletException {
        req.getView(this, "index.jelly").forward(req, rsp);
    }

    @POST
    public void doStop(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException, ServletException {
        Jenkins jenkins = Jenkins.get();

        if (jenkins == null || !jenkins.hasPermission(Item.CANCEL)) {
            rsp.sendError(403, "You need CANCEL permission to perform this action");
            return;
        }

        int count = 0;

        try {
            // Stop all running builds
            List<Job> allJobs = jenkins.getAllItems(Job.class);
            for (Job<?, ?> job : allJobs) {
                for (Run<?, ?> build : job.getBuilds()) {
                    if (build.isBuilding()) {
                        Executor executor = build.getExecutor();
                        if (executor != null) {
                            executor.interrupt(Result.ABORTED,
                                new CauseOfInterruption.UserInterruption("EMERGENCY STOP by " +
                                        jenkins.getAuthentication2().getName()));
                            count++;
                        } else {
                            LOGGER.warning("Cannot stop build (no executor): " + build.getFullDisplayName());
                        }
                    }
                }
            }

            // Cancel all queued items
            Queue queue = jenkins.getQueue();
            for (Queue.Item item : queue.getItems()) {
                boolean cancelled = queue.cancel(item);
                if (cancelled) {
                    count++;
                } else {
                    LOGGER.warning("Failed to cancel queued item: " + item.task.getFullDisplayName());
                }
            }

        } catch (Exception e) {
            LOGGER.severe("Error during emergency stop: " + e.getMessage());
            rsp.sendError(500, "Error during emergency stop: " + e.getMessage());
            return;
        }

        this.stoppedPipelines = count;

        rsp.setContentType("text/html");
        req.getView(this, "success.jelly").forward(req, rsp);
    }
}
