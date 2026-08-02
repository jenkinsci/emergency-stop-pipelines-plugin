package io.jenkins.plugins.emergencystop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.Jenkins;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.JenkinsExtension;

@ExtendWith(JenkinsExtension.class)
class EmergencyStopAllActionTest {

    @Test
    void iconFileNameShouldReturnSymbol(JenkinsRule j) {
        EmergencyStopAllAction action = new EmergencyStopAllAction();
        assertEquals("symbol-warning", action.getIconFileName());
    }

    @Test
    void displayNameShouldReturnText(JenkinsRule j) {
        EmergencyStopAllAction action = new EmergencyStopAllAction();
        assertEquals("Emergency STOP Pipelines", action.getDisplayName());
    }

    @Test
    void urlNameShouldReturnPath(JenkinsRule j) {
        EmergencyStopAllAction action = new EmergencyStopAllAction();
        assertEquals("emergency-stop-pipelines", action.getUrlName());
    }

    @Test
    void iconFileNameNullWhenNoAdmin(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            assertNull(new EmergencyStopAllAction().getIconFileName());
        }
    }

    @Test
    void displayNameNullWhenNoAdmin(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            assertNull(new EmergencyStopAllAction().getDisplayName());
        }
    }

    @Test
    void urlNameNullWhenNoAdmin(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            assertNull(new EmergencyStopAllAction().getUrlName());
        }
    }

    @Test
    void adminPermissionShouldBeDeniedForNonAdmin(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            assertFalse(Jenkins.get().hasPermission(Jenkins.ADMINISTER));
        }
    }
}
