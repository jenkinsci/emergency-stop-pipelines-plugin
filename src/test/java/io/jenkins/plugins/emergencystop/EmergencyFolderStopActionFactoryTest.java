package io.jenkins.plugins.emergencystop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Action;
import hudson.model.TopLevelItem;
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
class EmergencyFolderStopActionFactoryTest {

    @Test
    void createForShouldReturnActionWithAdminPermission(JenkinsRule j) throws Exception {
        EmergencyFolderStopActionFactory factory = new EmergencyFolderStopActionFactory();
        Folder folder = createFolder(j);

        Collection<? extends Action> actions = factory.createFor(folder);
        assertEquals(1, actions.size());
        assertTrue(actions.iterator().next() instanceof EmergencyFolderStopAction);
    }

    @Test
    void createForShouldReturnEmptyWhenNoAdminPermission(JenkinsRule j) throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

        EmergencyFolderStopActionFactory factory = new EmergencyFolderStopActionFactory();
        Folder folder = createFolder(j);

        User user = User.getById("user", true);
        try (ACLContext ctx = ACL.as(user)) {
            Collection<? extends Action> actions = factory.createFor(folder);
            assertTrue(actions.isEmpty());
        }
    }

    @Test
    void typeShouldReturnFolderClass(JenkinsRule j) {
        EmergencyFolderStopActionFactory factory = new EmergencyFolderStopActionFactory();
        assertEquals(Folder.class, factory.type());
    }

    private static Folder createFolder(JenkinsRule j) {
        try {
            TopLevelItem item = j.createProject(Folder.class, "test-folder-factory");
            if (item instanceof Folder) {
                return (Folder) item;
            }
            throw new RuntimeException("Failed to create folder: got " + item.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
