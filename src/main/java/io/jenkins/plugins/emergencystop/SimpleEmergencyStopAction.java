package io.jenkins.plugins.emergencystop;

import hudson.Extension;
import hudson.model.*;
import jenkins.model.CauseOfInterruption;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@Extension
public class SimpleEmergencyStopAction implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(SimpleEmergencyStopAction.class.getName());

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
    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        Jenkins jenkins = Jenkins.get();
        if (jenkins == null || !jenkins.hasPermission(Item.CANCEL)) {
            rsp.sendError(403, "You need CANCEL permission to access this page");
            return;
        }
        req.getView(this, "index.jelly").forward(req, rsp);
    }

    @POST
    public void doStop(StaplerRequest req, StaplerResponse rsp) throws IOException {
        Jenkins jenkins = Jenkins.get();

        if (jenkins == null || !jenkins.hasPermission(Item.CANCEL)) {
            rsp.sendError(403, "You need CANCEL permission to perform this action");
            return;
        }

        int stoppedPipelines = 0;

        try {
            LOGGER.info("EMERGENCY STOP initiated by: " + jenkins.getAuthentication().getName());

            // Stop all running builds
            List<Job> allJobs = jenkins.getAllItems(Job.class);
            for (Job<?, ?> job : allJobs) {
                for (Run<?, ?> build : job.getBuilds()) {
                    if (build.isBuilding()) {
                        Executor executor = build.getExecutor();
                        if (executor != null) {
                            LOGGER.info("Interrupting build: " + build.getFullDisplayName());
                            executor.interrupt(Result.ABORTED,
                                new CauseOfInterruption.UserInterruption("EMERGENCY STOP by " +
                                        jenkins.getAuthentication().getName()));
                            stoppedPipelines++;
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
                    LOGGER.info("Cancelled queued item: " + item.task.getFullDisplayName());
                    stoppedPipelines++;
                } else {
                    LOGGER.warning("Failed to cancel queued item: " + item.task.getFullDisplayName());
                }
            }

            LOGGER.info("EMERGENCY STOP completed. Stopped: " + stoppedPipelines + " pipelines.");

        } catch (Exception e) {
            LOGGER.severe("Error during emergency stop: " + e.getMessage());
            e.printStackTrace();
            rsp.sendError(500, "Error during emergency stop: " + e.getMessage());
            return;
        }

        // ✅ Your original HTML restored here
        rsp.setContentType("text/html");
        rsp.getWriter().println(
            "<!DOCTYPE html>" +
            "<html><head><title>Emergency Stop Completed</title>" +
            "<meta http-equiv='refresh' content='10;url=" + jenkins.getRootUrl() + "'>" +
            "</head>" +
            "<body style='font-family: Arial, sans-serif; text-align: center; margin-top: 50px; background: #f8f9fa;'>" +
            "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
            "<h2 style='color: #d9534f; margin-bottom: 30px;'>Emergency Stop Completed</h2>" +
            "<div style='background: #d4edda; border: 1px solid #c3e6cb; color: #155724; padding: 20px; margin: 20px 0; border-radius: 8px;'>" +
            "<h3 style='margin-top: 0;'>✅ Results:</h3>" +
            "<p style='font-size: 16px; margin: 10px 0;'><strong>Stopped:</strong> " + stoppedPipelines + " pipelines</p>" +
            "</div>" +
            "<p style='color: #6c757d; margin-top: 30px;'>Redirecting to Jenkins dashboard in 10 seconds...</p>" +
            "<p><a href='" + jenkins.getRootUrl() + "' style='color: #007bff; text-decoration: none; font-weight: bold; font-size: 16px;'>← Return to Jenkins Dashboard Now</a></p>" +
            "</div>" +
            "</body></html>"
        );
    }
}
