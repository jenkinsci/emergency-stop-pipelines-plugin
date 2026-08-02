package io.jenkins.plugins.emergencystop;

import hudson.model.Action;
import hudson.model.Executor;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import jakarta.servlet.ServletException;
import java.io.IOException;
import jenkins.model.CauseOfInterruption;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.POST;

public class EmergencyJobStopAction implements Action {
    private final Job<?, ?> job;

    public EmergencyJobStopAction(Job<?, ?> job) {
        this.job = job;
    }

    @Override
    public String getIconFileName() {
        Jenkins jenkins = Jenkins.get();
        return jenkins.hasPermission(Jenkins.ADMINISTER) ? "symbol-stop-circle" : null;
    }

    @Override
    public String getDisplayName() {
        Jenkins jenkins = Jenkins.get();
        return jenkins.hasPermission(Jenkins.ADMINISTER) ? "Emergency Stop This Pipeline" : null;
    }

    @Override
    public String getUrlName() {
        Jenkins jenkins = Jenkins.get();
        return jenkins.hasPermission(Jenkins.ADMINISTER) ? "emergency-stop-job" : null;
    }

    @POST
    public void doStop(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException, ServletException {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            String stopUser = Jenkins.getAuthentication2().getName();
            CauseOfInterruption cause = new CauseOfInterruption.UserInterruption("EMERGENCY STOP by " + stopUser);

            for (Run<?, ?> build : job.getBuilds()) {
                if (build.isBuilding()) {
                    Executor executor = build.getExecutor();
                    if (executor != null) {
                        executor.interrupt(Result.ABORTED, cause);
                    }
                }
            }

            Queue queue = Jenkins.get().getQueue();
            for (Queue.Item item : queue.getItems()) {
                if (item.task instanceof Job<?, ?> queueJob && queueJob == job) {
                    queue.cancel(item);
                }
            }
        } catch (Exception e) {
            rsp.sendError(500, "Error during emergency stop: " + e.getMessage());
            return;
        }

        rsp.sendRedirect2(req.getContextPath() + "/" + job.getUrl());
    }
}
