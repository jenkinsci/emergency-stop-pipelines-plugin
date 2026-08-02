# Emergency Stop Pipelines Plugin

A Jenkins plugin that provides an emergency stop button to immediately halt all running and queued Jenkins builds.

## Features

- **Global Emergency Stop** — Stop all running builds and queued items across the entire Jenkins instance from the top navigation bar.
- **Folder-Level Emergency Stop** — Stop all running builds and queued items within a specific folder and its subfolders.
- **Permission-Gated** — Both actions are restricted to users with `ADMINISTER` permission.
- **CSRF Protection** — All destructive actions use POST requests with Jenkins CSRF protection.

## Installation

Install the plugin from the Jenkins Plugin Manager or download the latest HPI from the [releases page](https://github.com/jenkinsci/emergency-stop-pipelines-plugin/releases).

## Usage

### Global Stop

After installing the plugin, an **Emergency STOP Pipelines** button appears in the top navigation bar for users with `ADMINISTER` permission. Clicking it opens a confirmation dialog that stops all running builds and cancels all queued items.

![Top Bar Menu](screenshots/top_bar_button.png)
![Main Page](screenshots/main_page.png)
![After Completion](screenshots/after_trigger_completed.png)

### Folder Stop

A folder-level emergency stop button appears in the sidebar for folders where the user has `ADMINISTER` permission. It stops all running builds and queued items within that folder and all subfolders.

## Requirements

- Jenkins 2.568 or later
- Java 21 or later
- [cloudbees-folder](https://plugins.jenkins.io/cloudbees-folder/) plugin (for folder-level stop support)

## Configuration

No additional configuration is required. The plugin works out of the box after installation.

## Building from Source

```bash
git clone https://github.com/jenkinsci/emergency-stop-pipelines-plugin.git
cd emergency-stop-pipelines-plugin
mvn clean verify
```

## License

Licensed under the MIT License. See [LICENSE](LICENSE) for details.