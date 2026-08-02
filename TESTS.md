# Testing Guide

This document describes how to write and run tests for the Emergency Stop Pipelines plugin.

## Running Tests

### Run all tests

```bash
mvn test
```

### Run a specific test class

```bash
mvn test -Dtest=EmergencyStopAllActionTest
```

### Run a specific test method

```bash
mvn test -Dtest=EmergencyStopAllActionTest#iconFileNameShouldReturnSymbol
```

### Check code formatting

```bash
mvn spotless:check
```

### Check for banned JUnit 4 imports

```bash
mvn ban-junit4-imports:check
```

### Full verification (compile, test, check)

```bash
mvn clean verify
```

## Test Framework

Tests use [JUnit 5 (Jupiter)](https://junit.org/junit5/) via the Jenkins Test Harness JUnit 5 extension.

### Key annotations and classes

- `@ExtendWith(JenkinsExtension.class)` — registers the JUnit 5 parameter resolver that manages a full Jenkins test instance
- `@RegisterExtension JenkinsRule` — alternative: register a `JenkinsRule` as a static field (JUnit 4 style, still supported)
- `JenkinsRule j` — parameter type for test methods; the extension injects a running Jenkins instance
- `JenkinsRule.WebClient` — HTMLUnit-based client for making HTTP requests to the test Jenkins instance
- `MockAuthorizationStrategy` — allows fine-grained permission configuration for testing access control
- `ACL.as(User)` — temporarily switches the security context to a specific user (useful for testing permission denial)

## Writing Tests

### Basic test structure

```java
package io.jenkins.plugins.emergencystop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.JenkinsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(JenkinsExtension.class)
class MyActionTest {

    @Test
    void iconFileNameShouldReturnSymbol(JenkinsRule j) {
        MyAction action = new MyAction();
        assertEquals("symbol-warning", action.getIconFileName());
    }
}
```

### Testing with admin permissions

By default, `JenkinsRule` runs with `ACL.SYSTEM`, meaning all permissions are granted. This allows testing the "happy path" without additional setup:

```java
@Test
void createForShouldReturnAction(JenkinsRule j) throws Exception {
    MyFactory factory = new MyFactory();
    FreeStyleProject project = j.createFreeStyleProject();
    Collection<? extends Action> actions = factory.createFor(project);
    assertEquals(1, actions.size());
}
```

### Testing without admin permissions

To test permission denial, set up a security realm and authorization strategy, then use `ACL.as()` to switch the current user:

```java
@Test
void iconFileNameNullWhenNoAdmin(JenkinsRule j) throws Exception {
    j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
    j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

    User user = User.getById("user", true);
    try (ACLContext ctx = ACL.as(user)) {
        assertNull(new MyAction().getIconFileName());
    }
}
```

### Testing web endpoints (POST)

Actions with `@POST` endpoints can be tested via the web client:

```java
@Test
void doStopShouldRedirect(JenkinsRule j) throws Exception {
    FreeStyleProject project = j.createFreeStyleProject("test-job");

    JenkinsRule.WebClient wc = j.createWebClient();
    wc.setThrowExceptionOnFailingStatusCode(false);

    URL url = new URL(j.getURL(), project.getUrl() + "emergency-stop-job/stop");
    WebRequest request = new WebRequest(url, HttpMethod.POST);
    wc.addCrumb(request);
    Page page = wc.getPage(request);

    assertTrue(page.getWebResponse().getStatusCode() >= 200);
}
```

### Testing web endpoints without permission

```java
@Test
void doStopWithoutAdminShouldReturn403(JenkinsRule j) throws Exception {
    j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
    j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());

    JenkinsRule.WebClient wc = j.createWebClient();
    wc.setThrowExceptionOnFailingStatusCode(false);
    wc.login("user");

    URL url = new URL(j.getURL(), project.getUrl() + "emergency-stop-job/stop");
    WebRequest request = new WebRequest(url, HttpMethod.POST);
    wc.addCrumb(request);
    Page page = wc.getPage(request);

    assertTrue(page.getWebResponse().getStatusCode() >= 400);
}
```

## Test Files

| File | Description |
|------|-------------|
| `EmergencyStopAllActionTest` | Tests for the global root action (icon, display name, URL, permission checks) |
| `EmergencyJobStopActionTest` | Tests for the job-level action (icon, display name, URL, permission checks, doStop) |
| `EmergencyFolderStopActionTest` | Tests for the folder-level action (icon, display name, URL, permission checks, doStop) |
| `EmergencyJobStopActionFactoryTest` | Tests for the job action factory (createFor, type) |
| `EmergencyFolderStopActionFactoryTest` | Tests for the folder action factory (createFor, type) |

## Notes

- Do NOT use `JUnit5JenkinsRule` as a parameter type in test methods — it is package-private. Use `JenkinsRule` instead.
- `JenkinsExtension` resolves parameters of type `JenkinsRule` only.
- `MockAuthorizationStrategy` with no grants denies all permissions to non-SYSTEM users.
- `ACL.as(User)` returns an `ACLContext` that must be used in a try-with-resources block.
- When testing POST endpoints via web client, use `WebRequest` with `HttpMethod.POST` and call `wc.addCrumb(request)` to handle CSRF tokens.
