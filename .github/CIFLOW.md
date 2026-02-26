# Development Version and Branch Handling

## Branches

The versioning policy follows a GitFlow-based workflow. See [Atlassian's GitFlow guide](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) for background.

```mermaid
gitGraph
    commit id: "initial"
    branch develop
    checkout develop
    commit id: "dev work"
    branch feature/JNG-1
    commit id: "feature 1"
    checkout develop
    merge feature/JNG-1
    branch feature/JNG-2
    commit id: "feature 2"
    checkout develop
    merge feature/JNG-2
    branch release/1.0-beta1
    commit id: "stabilize"
    branch bugfix/JNG-4
    commit id: "fix"
    checkout release/1.0-beta1
    merge bugfix/JNG-4
    checkout develop
    merge release/1.0-beta1
```

| Branch pattern | Base | Purpose |
|---|---|---|
| `develop` | — | Main development branch with latest sources of the active version |
| `feature/JNG-NUMBER_summary` | `develop` | New features for the current version |
| `release/X.Y-betaN` or `X_Y_betaN` | `develop` | Release stabilization branches (the `release/` prefix is reserved for CI) |
| `bugfix/JNG-NUMBER_summary` | release branch | Bug fixes applied during release testing; must also be merged to newer release and develop branches |
| `support/JNG-NUMBER_summary` | release branch | Minor changes for a previous release; merged back to the release branch |
| `master` | — | Contains the latest released sources |
| `hotfix/JNG-NUMBER_summary` | `master` | Critical fixes applied to both release and master branches |

## Version Numbers

Versions follow semantic versioning with these rules:

| Event | Version change |
|-------|---------------|
| Start a `feature/` branch | No change — inherits from `develop` |
| Start a release branch from `develop` | 2nd number (minor) incremented on `develop` |
| Start a `bugfix/` branch | No change — fixes applied to release branch before merge to master |
| Start a `support/` branch | 3rd number (patch) incremented |
| Start a `hotfix/` branch | 4th number incremented |

## GitHub Actions Workflows

The CI/CD pipeline consists of four interconnected workflows:

### build.yml — Main Build Pipeline

Triggered on pushes to `develop` and pull requests targeting `develop`, `master`, `increment/*`, or `release/*`.

```mermaid
flowchart TD
    trigger["Push on develop<br/>or PR on develop/master/increment/release"]
    trigger --> check{Branch type?}
    check -->|master, release/*| fixedVer["Version from pom.xml<br/>(without -SNAPSHOT)"]
    check -->|develop, increment/*| dynVer["Version: major.minor.qualifier<br/>.date_commitId_branch"]
    fixedVer --> build["Build & deploy to Nexus"]
    dynVer --> build
    build --> tag["Create git tag v&lt;version&gt;"]
    tag --> prCheck{increment/* or release/*?}
    prCheck -->|Yes| mergeTag["Create tag merge-pr/&lt;version&gt;"]
    mergeTag --> triggerMerge["Trigger merge-pr-tagged.yml"]
    prCheck -->|No| devCheck{develop?}
    devCheck -->|Yes| changelog["Build changelog"]
    changelog --> release["Create GitHub pre-release"]
```

### merge-pr-tagged.yml — PR Merge Handler

Triggered when a `merge-pr/*` tag is pushed.

```mermaid
flowchart TD
    trigger["Push on merge-pr/* tag"]
    trigger --> getVer["Extract version from tag"]
    getVer --> check{Version format?}
    check -->|major.minor.qualifier| mergeMaster["Merge PR to master"]
    mergeMaster --> triggerRelease["Trigger create-release-on-master.yml"]
    check -->|other format| squashDev["Squash PR to develop"]
    squashDev --> triggerBuild["Trigger build.yml"]
    mergeMaster --> cleanup["Delete merge-pr tag"]
    squashDev --> cleanup
```

### create-release-on-master.yml — Release Creation

Triggered on pushes to `master`.

```mermaid
flowchart LR
    trigger["Push on master"] --> version["Get version from tag"]
    version --> changelog["Build changelog"]
    changelog --> release["Create GitHub release<br/>(latest)"]
```

### release.yml — Manual Release Trigger

Manually triggered with a version parameter (`auto` or `major.minor.qualifier`).

```mermaid
flowchart TD
    trigger["Manual trigger<br/>with version parameter"]
    trigger --> check{Version = 'auto'?}
    check -->|Yes| fromPom["Use pom.xml version<br/>(without -SNAPSHOT)"]
    check -->|No| manual["Use given version"]
    fromPom --> next["Next version = qualifier + 1"]
    manual --> next
    next --> prMaster["Create PR to master<br/>with release version"]
    next --> prDevelop["Create PR to develop<br/>with next version"]
    prMaster --> buildTrigger1["Trigger build.yml"]
    prDevelop --> buildTrigger2["Trigger build.yml"]
```

## Development Rules

> **Important:** There is no commit without a ticket number. Every pull request and commit must reference a JIRA ticket in the format `JNG-xxx`.

Issue tracking: [BlackBelt JIRA](https://blackbelt.atlassian.net/jira/dashboards)
