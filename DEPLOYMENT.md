# Deployment Guide

Guide for maintainers to publish releases to GitHub Packages.

## Prerequisites

- Maintainer access to the repository
- GitHub account with permissions

## Publishing a Release

### 1. Update Version

Update version in `pom.xml`:
```xml
<version>1.1.0</version>
```

Commit the change:
```bash
git add pom.xml
git commit -m "Bump version to 1.1.0"
git push
```

### 2. Create GitHub Release

1. Go to https://github.com/mehrdadfalahati/pg-distinct-jpa/releases
2. Click "Create a new release"
3. Tag version: `v1.1.0`
4. Release title: `v1.1.0`
5. Description: Document changes, new features, bug fixes
6. Click "Publish release"

**GitHub Actions will automatically build and publish to GitHub Packages.**

### 3. Verify Deployment

1. Check "Actions" tab - workflow should complete successfully
2. Check "Packages" - new version should appear

## Manual Deployment (if needed)

```bash
mvn clean deploy -Pgithub
```

## Version Numbers

Use Semantic Versioning (SemVer):
- **MAJOR** (2.0.0): Breaking changes
- **MINOR** (1.1.0): New features, backward compatible
- **PATCH** (1.0.1): Bug fixes, backward compatible

## GitHub Actions Workflows

### CI Workflow
- Triggers: Push to main/master, Pull Requests
- Actions: Build, test, package

### Publish Workflow
- Triggers: Release creation
- Actions: Build, test, publish to GitHub Packages

## Troubleshooting

### Build Fails
- Check GitHub Actions logs
- Ensure all tests pass locally first

### Authentication Issues
- GITHUB_TOKEN is automatically provided
- Ensure workflow has `packages: write` permission

## For Users

After publishing, users can add to their `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/mehrdadfalahati/pg-distinct-jpa</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.mehrdadfalahati</groupId>
        <artifactId>pg-distinct-jpa</artifactId>
        <version>1.1.0</version>
    </dependency>
</dependencies>
```

Users need GitHub authentication in `~/.m2/settings.xml`:
```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>GITHUB_USERNAME</username>
            <password>GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

Token needs `read:packages` scope.
