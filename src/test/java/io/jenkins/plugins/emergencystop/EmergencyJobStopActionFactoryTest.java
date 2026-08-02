package io.jenkins.plugins.emergencystop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Action;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.JenkinsExtension;

@ExtendWith(JenkinsExtension.class)
class EmergencyJobStopActionFactoryTest {

    @Test
    void createForShouldReturnActionWithAdminPermission(JenkinsRule j) throws Exception {
        EmergencyJobStopActionFactory factory = new EmergencyJobStopActionFactory();
        FreeStyleProject project = j.createFreeStyleProject("test-job-factory");

        Collection<? extends Action> actions = factory.createFor(project);
        assertEquals(1, actions.size());
        assertTrue(actions.iterator().next() instanceof EmergencyJobStopAction);
    }

    @Test
    void createForShouldReturnEmptyWhenNoAdminPermission(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        EmergencyJobStopActionFactory factory = new EmergencyJobStopActionFactory();
        FreeStyleProject project = j.createFreeStyleProject("test-job-no-permission");

        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            Collection<? extends Action> actions = factory.createFor(project);
            assertTrue(actions.isEmpty());
        }
    }

    @Test
    void typeShouldReturnJobClass(JenkinsRule j) {
        EmergencyJobStopActionFactory factory = new EmergencyJobStopActionFactory();
        assertEquals(Job.class, factory.type());
    }
}
