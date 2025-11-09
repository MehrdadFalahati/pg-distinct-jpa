# Deployment Guide

This guide explains how to deploy the `pg-distinct-jpa` library to GitHub Packages using GitHub Actions.

## Prerequisites

1. **GitHub Repository**: Create a repository named `pg-distinct-jpa` on GitHub
2. **Git**: Installed locally
3. **GitHub Account**: With permissions to push to the repository

## Setup Steps

### 1. Create GitHub Repository

1. Go to https://github.com/new
2. Repository name: `pg-distinct-jpa`
3. Description: "PostgreSQL DISTINCT ON support for Hibernate/JPA"
4. Choose Public or Private
5. Click "Create repository"

### 2. Push Code to GitHub

```bash
cd d:/Projects/new/pg-distinct-jpa

# Initialize git (already done)
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit: PostgreSQL DISTINCT ON for Hibernate/JPA

- Custom PostgreSQL dialect with DISTINCT ON support
- Fixed argument ordering bug
- Comprehensive test suite with Testcontainers
- Unit and integration tests
- Complete documentation"

# Add remote (replace with your GitHub username if different)
git remote add origin https://github.com/mehrdadfalahati/pg-distinct-jpa.git

# Push to GitHub
git branch -M main
git push -u origin main
```

### 3. Verify GitHub Actions

After pushing, GitHub Actions will automatically:

1. **CI Workflow** (`maven-ci.yml`):
   - Triggers on every push to main/master/develop
   - Runs tests
   - Builds the package
   - Uploads build artifacts

2. **Publish Workflow** (`maven-publish.yml`):
   - Triggers on release creation or manual dispatch
   - Builds and tests
   - Publishes to GitHub Packages

## Publishing a Release

### Method 1: Create a GitHub Release (Recommended)

1. Go to your repository on GitHub
2. Click "Releases" → "Create a new release"
3. Click "Choose a tag" → Type `v1.0.0` → "Create new tag"
4. Release title: `v1.0.0 - Initial Release`
5. Description:
   ```markdown
   ## Features
   - Custom Hibernate dialect for PostgreSQL DISTINCT ON
   - Fixed critical argument ordering bug
   - Comprehensive test coverage
   - Support for single and multiple column DISTINCT ON

   ## Installation
   See README.md for installation instructions
   ```
6. Click "Publish release"

This will automatically trigger the `maven-publish.yml` workflow.

### Method 2: Manual Workflow Dispatch

1. Go to your repository on GitHub
2. Click "Actions" tab
3. Select "Publish Maven Package" workflow
4. Click "Run workflow"
5. Optionally specify a version
6. Click "Run workflow"

## Using the Published Package

### For Users: Install from GitHub Packages

Add this to your `pom.xml`:

```xml
<project>
    <!-- Add GitHub Packages repository -->
    <repositories>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/mehrdadfalahati/pg-distinct-jpa</url>
        </repository>
    </repositories>

    <!-- Add dependency -->
    <dependencies>
        <dependency>
            <groupId>com.github.mehrdadfalahati</groupId>
            <artifactId>pg-distinct-jpa</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
</project>
```

### Authentication for GitHub Packages

Users need to authenticate to download from GitHub Packages.

**Create `~/.m2/settings.xml`:**

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

**Generate a GitHub Token:**
1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Select scopes: `read:packages`
4. Copy the token and use it as the password in `settings.xml`

## Alternative: Publishing to Maven Central

If you want to publish to Maven Central (more public and easier for users):

### Prerequisites

1. **Sonatype Account**: Register at https://issues.sonatype.org/
2. **GPG Key**: For signing artifacts
3. **Domain Verification**: Verify ownership of `com.github.mehrdadfalahati` or use `io.github.mehrdadfalahati`

### Setup for Maven Central

1. Update `pom.xml`:

```xml
<groupId>io.github.mehrdadfalahati</groupId>

<distributionManagement>
    <snapshotRepository>
        <id>ossrh</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
    </snapshotRepository>
    <repository>
        <id>ossrh</id>
        <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
    </repository>
</distributionManagement>
```

2. Add Nexus Staging Plugin:

```xml
<plugin>
    <groupId>org.sonatype.plugins</groupId>
    <artifactId>nexus-staging-maven-plugin</artifactId>
    <version>1.6.13</version>
    <extensions>true</extensions>
    <configuration>
        <serverId>ossrh</serverId>
        <nexusUrl>https://s01.oss.sonatype.org/</nexusUrl>
        <autoReleaseAfterClose>true</autoReleaseAfterClose>
    </configuration>
</plugin>
```

3. Set GitHub Secrets:
   - `OSSRH_USERNAME`: Your Sonatype username
   - `OSSRH_TOKEN`: Your Sonatype token
   - `GPG_PRIVATE_KEY`: Your GPG private key
   - `GPG_PASSPHRASE`: Your GPG passphrase

## Workflow Overview

### CI Workflow (`maven-ci.yml`)

**Triggers:**
- Push to main, master, or develop branches
- Pull requests to main or master

**Steps:**
1. Checkout code
2. Setup Java 21
3. Build with Maven
4. Run tests
5. Generate test report
6. Package JAR
7. Upload artifacts

### Publish Workflow (`maven-publish.yml`)

**Triggers:**
- Release creation
- Manual workflow dispatch

**Steps:**
1. Checkout code
2. Setup Java 21
3. Build package
4. Run tests
5. Publish to GitHub Packages
6. Upload artifacts

## Versioning

This project uses Semantic Versioning (SemVer):

- **MAJOR**: Incompatible API changes
- **MINOR**: Add functionality in a backward compatible manner
- **PATCH**: Backward compatible bug fixes

Example versions:
- `1.0.0` - Initial release
- `1.1.0` - Add new features
- `1.0.1` - Bug fixes
- `2.0.0` - Breaking changes

## Updating the Version

To release a new version:

1. Update version in `pom.xml`:
   ```xml
   <version>1.1.0</version>
   ```

2. Commit changes:
   ```bash
   git add pom.xml
   git commit -m "Bump version to 1.1.0"
   git push
   ```

3. Create a new release on GitHub with tag `v1.1.0`

## Troubleshooting

### Build Fails in GitHub Actions

**Check:**
- Java version compatibility (should be 21)
- Test failures (tests require Docker, which is available in GitHub Actions)
- Review workflow logs in the Actions tab

### Authentication Issues

**For GitHub Packages:**
- Ensure GITHUB_TOKEN has `packages: write` permission
- For users downloading: verify GitHub personal access token has `read:packages` scope

### GPG Signing Issues

**If deploying to Maven Central:**
- Ensure GPG private key is correctly added to GitHub Secrets
- Verify passphrase is correct
- Check GPG plugin configuration in `pom.xml`

## Monitoring Deployments

1. Go to your repository on GitHub
2. Click "Actions" tab
3. View workflow runs and logs
4. Check "Packages" (right sidebar) to see published versions

## Best Practices

1. ✅ Always run tests locally before pushing
2. ✅ Use semantic versioning
3. ✅ Write meaningful commit messages
4. ✅ Document breaking changes in release notes
5. ✅ Keep dependencies up to date
6. ✅ Review GitHub Actions logs after deployment

## Support

For issues with deployment:
- Check GitHub Actions logs
- Review this deployment guide
- Open an issue in the repository

## License

This project is licensed under the MIT License - see the LICENSE file for details.
