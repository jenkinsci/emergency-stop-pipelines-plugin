# Contributing to Emergency Stop Pipelines

Thank you for your interest in contributing to this plugin!

## Getting Started

### Prerequisites

- Java 21 or later
- Apache Maven 3.9+
- Git

### Building Locally

```bash
mvn clean verify
```

This compiles the plugin, runs tests, and packages the HPI file.

### Code Style

This project uses [Spotless](https://github.com/diffplug/spotless) for code formatting. The formatter is applied automatically during the build. To manually format your code:

```bash
mvn spotless:apply
```

To check formatting without applying changes:

```bash
mvn spotless:check
```

### Submitting Changes

1. Fork the repository and create a feature branch from `main`.
2. Make your changes with clear, atomic commits.
3. Ensure all checks pass (`mvn clean verify`).
4. Write tests for new or changed behavior (see [TESTS.md](TESTS.md) for the testing guide).
5. Submit a pull request describing the change and its motivation.

### Commit Guidelines

Follow conventional commit style:

- `feat:` — new feature
- `fix:` — bug fix
- `docs:` — documentation changes
- `refactor:` — code restructuring without behavior change
- `test:` — test additions or updates
- `chore:` — maintenance tasks (deps, config, etc.)

## Testing

See [TESTS.md](TESTS.md) for a detailed guide on writing and running tests. In summary:

- Use `mvn test` to run all tests.
- Use `mvn spotless:check` to verify code formatting.
- All changes must pass `mvn clean verify` before submission.

## Reviewing Pull Requests

Pull requests are reviewed manually. Please be patient — review times may vary.

Community reviewers are encouraged to:

- Test pull request builds
- Verify that changes address the reported issue
- Leave constructive feedback

## Reporting Issues

Open a GitHub issue with a clear title and description, including:

- Jenkins version
- Plugin version
- Steps to reproduce
- Expected vs. actual behavior
- Relevant logs (if applicable)

## Maintainers

To request maintainer or contributor status, open an issue titled "Request to become maintainer/contributor".