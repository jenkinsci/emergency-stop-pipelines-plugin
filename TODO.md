# TODO

## Missing Test Coverage

The following behaviors are not yet covered by tests and should be addressed in future work.

### EmergencyStopAllAction

- **`doStop()` web endpoint**: The global stop POST endpoint (`POST /emergency-stop-pipelines/stop`) is not tested via the web client. A root action accessibility test could not be included due to the `JenkinsRule` not registering `RootAction` extensions as web-accessible endpoints in the test harness. Needs investigation.

### EmergencyJobStopAction

- **Build interruption verification**: The `doStop()` test verifies the HTTP response but does not assert that the build was actually interrupted or that the queue item was cancelled. Need a way to poll for build state changes or use `Run` listeners.
- **Success path with running builds**: The `doStopShouldRedirectToJobPage` test starts a build but does not verify the build is actually stopped after the action.

### EmergencyFolderStopAction

- **Recursive folder behavior**: The `stopFolderRecursively()` private method is not tested directly. Tests cover single-level folders but not nested subfolders.
- **Build interruption verification**: Same as job-level — does not verify builds are actually stopped after the action.

### Future Enhancements

- Add integration tests using `WorkflowJob` / pipeline jobs for `EmergencyJobStopAction` and `EmergencyFolderStopAction`.
- Add tests for the Jelly view templates (form submission, CSRF crumb handling).
- Add tests for the `index.js` JavaScript behavior (form-based POST submission).
