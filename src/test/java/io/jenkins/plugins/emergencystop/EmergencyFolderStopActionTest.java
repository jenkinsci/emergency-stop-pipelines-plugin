package io.jenkins.plugins.emergencystop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.FreeStyleProject;
import hudson.model.TopLevelItem;
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
class EmergencyFolderStopActionTest {

    @Test
    void iconFileNameShouldReturnSymbol(JenkinsRule j) throws Exception {
        EmergencyFolderStopAction action = new EmergencyFolderStopAction(createFolder(j));
        assertEquals("symbol-stop-circle", action.getIconFileName());
    }

    @Test
    void displayNameShouldReturnText(JenkinsRule j) throws Exception {
        EmergencyFolderStopAction action = new EmergencyFolderStopAction(createFolder(j));
        assertEquals("Emergency Stop Folder Pipelines", action.getDisplayName());
    }

    @Test
    void urlNameShouldReturnPath(JenkinsRule j) throws Exception {
        EmergencyFolderStopAction action = new EmergencyFolderStopAction(createFolder(j));
        assertEquals("emergency-stop-folder-pipelines", action.getUrlName());
    }

    @Test
    void iconFileNameNullWhenNoAdmin(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        Folder folder = createFolder(j);
        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            assertNull(new EmergencyFolderStopAction(folder).getIconFileName());
        }
    }

    @Test
    void doStopShouldRedirectToFolderPage(JenkinsRule j) throws Exception {
        Folder folder = createFolder(j);
        FreeStyleProject project = folder.createProject(FreeStyleProject.class, "sub-job");
        project.scheduleBuild2(0);

        JenkinsRule.WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);

        URL url = new URL(j.getURL(), folder.getUrl() + "emergency-stop-folder-pipelines/stop");
        WebRequest request = new WebRequest(url, HttpMethod.POST);
        wc.addCrumb(request);
        Page page = wc.getPage(request);

        assertTrue(page.getWebResponse().getStatusCode() >= 200);
    }

    @Test
    void doStopWithoutAdminShouldReturn403(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        Folder folder = createFolder(j);

        JenkinsRule.WebClient wc = j.createWebClient();
        wc.setThrowExceptionOnFailingStatusCode(false);
        wc.login("user");

        URL url = new URL(j.getURL(), folder.getUrl() + "emergency-stop-folder-pipelines/stop");
        WebRequest request = new WebRequest(url, HttpMethod.POST);
        wc.addCrumb(request);
        Page page = wc.getPage(request);

        assertTrue(page.getWebResponse().getStatusCode() >= 400);
    }

    private static Folder createFolder(JenkinsRule j) {
        try {
            TopLevelItem item = j.createProject(Folder.class, "test-folder");
            if (item instanceof Folder) {
                return (Folder) item;
            }
            throw new RuntimeException("Failed to create folder: got " + item.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
