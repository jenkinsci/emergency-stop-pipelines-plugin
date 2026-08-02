package io.jenkins.plugins.emergencystop;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import java.util.Collection;
import java.util.Collections;
import jenkins.model.Jenkins;
import jenkins.model.TransientActionFactory;

@Extension
public class EmergencyJobStopActionFactory extends TransientActionFactory<Job<?, ?>> {

    @SuppressWarnings("unchecked")
    @Override
    public Class<Job<?, ?>> type() {
        return (Class<Job<?, ?>>) (Class<?>) Job.class;
    }

    @Override
    public Collection<? extends Action> createFor(Job<?, ?> job) {
        if (!job.hasPermission(Jenkins.ADMINISTER)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new EmergencyJobStopAction(job));
    }
}
