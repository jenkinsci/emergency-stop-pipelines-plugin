package io.jenkins.plugins.emergencystop;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.Extension;
import hudson.model.Action;
import java.util.Collection;
import java.util.Collections;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;

@Extension
public class EmergencyFolderStopActionFactory extends TransientActionFactory<Folder> {

    @Override
    public Class<Folder> type() {
        return Folder.class;
    }

    @Override
    public Collection<? extends Action> createFor(Folder folder) {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            return Collections.emptyList(); // no action for non-admins
        }
        return Collections.singletonList(new EmergencyFolderStopAction(folder));
    }
}
