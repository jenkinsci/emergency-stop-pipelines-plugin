package io.jenkins.plugins.emergencystop;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import jenkins.model.CauseOfInterruption;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.POST;

public class EmergencyFolderStopAction implements Action {
    private final Folder folder;

    public EmergencyFolderStopAction(Folder folder) {
        this.folder = folder;
    }

    @Override
    public String getIconFileName() {
        // Use folder-specific permission check for better security granularity
        return folder.hasPermission(Jenkins.ADMINISTER) ? "symbol-stop-circle" : null;
    }

    @Override
    public String getDisplayName() {
        return folder.hasPermission(Jenkins.ADMINISTER) ? "Emergency Stop Folder Pipelines" : null;
    }

    @Override
    public String getUrlName() {
        return folder.hasPermission(Jenkins.ADMINISTER) ? "emergency-stop-folder-pipelines" : null;
    }

    @POST
    public void doStop(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException, ServletException {
        folder.checkPermission(Jenkins.ADMINISTER);

        try {
            String stopUser = Jenkins.getAuthentication2().getName();
            stopFolderRecursively(folder, stopUser);

            rsp.sendRedirect2(req.getContextPath() + "/" + folder.getUrl());
        } catch (Exception e) {
            rsp.sendError(500, "Error during emergency stop: " + e.getMessage());
        }
    }

    /**
     * Recursively stops all running builds and queued items in this folder and subfolders.
     */
    private void stopFolderRecursively(AbstractFolder<?> currentFolder, String stopUser) {
        CauseOfInterruption cause = new CauseOfInterruption.UserInterruption("EMERGENCY STOP by " + stopUser);

        for (TopLevelItem item : currentFolder.getItems()) {
            if (item instanceof Job<?, ?> job) {
                for (Run<?, ?> build : job.getBuilds()) {
                    if (build.isBuilding()) {
                        Executor executor = build.getExecutor();
                        if (executor != null) {
                            executor.interrupt(Result.ABORTED, cause);
                        }
                    }
                }
            } else if (item instanceof AbstractFolder<?> subFolder) {
                stopFolderRecursively(subFolder, stopUser);
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
