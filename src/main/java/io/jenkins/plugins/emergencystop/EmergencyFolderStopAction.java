package io.jenkins.plugins.emergencystop;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Action;
import hudson.model.Executor;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import jenkins.model.CauseOfInterruption;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

public class EmergencyFolderStopAction implements Action {
    private final Folder folder;

    public EmergencyFolderStopAction(Folder folder) {
        this.folder = folder;
    }

    @Override
    public String getIconFileName() {
        Jenkins jenkins = Jenkins.get();
        return jenkins.hasPermission(Jenkins.ADMINISTER) ? "symbol-warning" : null;
    }

    @Override
    public String getDisplayName() {
        Jenkins jenkins = Jenkins.get();
        return jenkins.hasPermission(Jenkins.ADMINISTER) ? "Emerg. Stop Folder Pipelines" : null;
    }

    @Override
    public String getUrlName() {
        Jenkins jenkins = Jenkins.get();
        return jenkins.hasPermission(Jenkins.ADMINISTER) ? "emergency-stop-folder-pipelines" : null;
    }

    public void doStop(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException, ServletException {
        
        Jenkins jenkins = Jenkins.get();
        if (!jenkins.hasPermission(Jenkins.ADMINISTER)) {
            rsp.sendError(403, "You need ADMIN permission to perform this action");
            return;
        }

        if (!"GET".equals(req.getMethod())) {
            rsp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET required");
            return;
        }

        try {
            stopFolderRecursively(folder);
        } catch (Exception e) {
            rsp.sendError(500, "Error during emergency stop: " + e.getMessage());
            return;
        }

        rsp.setStatus(200);
    }

    /**
     * Recursively stops all running builds and queued items in this folder and subfolders.
     */
    private void stopFolderRecursively(AbstractFolder<?> currentFolder) {
        // Stop running builds for jobs in this folder
        for (TopLevelItem item : currentFolder.getItems()) {
            if (item instanceof Job<?, ?> job) {
                for (Run<?, ?> build : job.getBuilds()) {
                    if (build.isBuilding()) {
                        Executor executor = build.getExecutor();
                        if (executor != null) {
                            executor.interrupt(
                                    Result.ABORTED,
                                    new CauseOfInterruption.UserInterruption("EMERGENCY STOP by "
                                            + Jenkins.getAuthentication2().getName()));
                        }
                    }
                }
            } else if (item instanceof AbstractFolder<?> subFolder) {
                // Recurse into subfolder
                stopFolderRecursively(subFolder);
            }
        }

        // Cancel queued items for jobs in this folder
        Queue queue = Jenkins.get().getQueue();
        for (Queue.Item item : queue.getItems()) {
            if (item.task instanceof Job<?, ?> job && job.getParent() == currentFolder) {
                queue.cancel(item);
            }
        }
    }
}
