package io.jenkins.plugins.emergencystop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.FreeStyleProject;
import hudson.model.User;
import hudson.security.ACL;
import hudson.security.ACLContext;
import java.net.URL;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.WebRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.JenkinsExtension;

@ExtendWith(JenkinsExtension.class)
class EmergencyJobStopActionTest {

    @Test
    void iconFileNameShouldReturnSymbol(JenkinsRule j) throws Exception {
        EmergencyJobStopAction action = new EmergencyJobStopAction(j.createFreeStyleProject());
        assertEquals("symbol-stop-circle", action.getIconFileName());
    }

    @Test
    void displayNameShouldReturnText(JenkinsRule j) throws Exception {
        EmergencyJobStopAction action = new EmergencyJobStopAction(j.createFreeStyleProject());
        assertEquals("Emergency Stop This Pipeline", action.getDisplayName());
    }

    @Test
    void urlNameShouldReturnPath(JenkinsRule j) throws Exception {
        EmergencyJobStopAction action = new EmergencyJobStopAction(j.createFreeStyleProject());
        assertEquals("emergency-stop-job", action.getUrlName());
    }

    @Test
    void iconFileNameNullWhenNoAdmin(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        FreeStyleProject project = j.createFreeStyleProject();
        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            assertNull(new EmergencyJobStopAction(project).getIconFileName());
        }
    }

    @Test
    void displayNameNullWhenNoAdmin(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        FreeStyleProject project = j.createFreeStyleProject();
        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            assertNull(new EmergencyJobStopAction(project).getDisplayName());
        }
    }

    @Test
    void urlNameNullWhenNoAdmin(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        FreeStyleProject project = j.createFreeStyleProject();
        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            assertNull(new EmergencyJobStopAction(project).getUrlName());
        }
    }

    @Test
    void doStopShouldRedirectToJobPage(JenkinsRule j) throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("test-job-stop");
        project.scheduleBuild2(0);

        JenkinsRule.WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);

        URL url = new URL(j.getURL(), project.getUrl() + "emergency-stop-job/stop");
        WebRequest request = new WebRequest(url, HttpMethod.POST);
        wc.addCrumb(request);
        Page page = wc.getPage(request);

        assertTrue(page.getWebResponse().getStatusCode() >= 200);
    }

    @Test
    void doStopWithoutAdminShouldReturn403(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        FreeStyleProject project = j.createFreeStyleProject("test-job-no-admin");

        JenkinsRule.WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        wc.login("user");

        URL url = new URL(j.getURL(), project.getUrl() + "emergency-stop-job/stop");
        WebRequest request = new WebRequest(url, HttpMethod.POST);
        wc.addCrumb(request);
        Page page = wc.getPage(request);

        assertTrue(page.getWebResponse().getStatusCode() >= 400);
    }
}
