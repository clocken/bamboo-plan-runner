# Bamboo Plan Runner

An Atlassian JIRA workflow postfunction to run an Atlassian Bamboo Plan. It is possible to set variables for the plan to run.

## Installation

Either install it from the [Atlassian Marketplace](TODO: add link here) or [build it for yourself](TODO: add link here) and install it via the [JIRA plugin manager](TODO: add link here).

## Prerequesites

The plugin uses [Atlassian Application Links](TODO: add link here) to trigger the configured plan on the Bamboo site. Thus once installed, ensure the following prerequesites are met to use the plugin:

- Create an _OAuth_ [application link](TODO: add link here) for each Atlassian Bamboo instance you want to trigger plan builds on
  - If you don't use the same user store for both JIRA and Bamboo sites, you will need to specify _OAuth (impersonating)_ for the Application Link and manually create all triggering JIRA users on the Bamboo site
- Ensure all users that might trigger the workflow transition have _Build access_ to the plan being triggered

## Configuration

Ensure the [prerequesites](#prerequesites) are met. Then:

- Add the _Run bamboo plan_ postfunction to your workflow transition (TODO: add screenshot here)
- Select the Bamboo instance and the plan to trigger
- You have two options to set values for variables of the plan
  - **Field:** Select a JIRA issue field, from which the value should be taken
  - **Custom:** Enter custom text and optionally use `$(field-id)` to substitute a JIRA issue field value inside the text

## Examples

(TODO: add screenshots here)

## Contributing

If you find a bug or have a suggestion for improvement/feature request, please open an issue here.

Help fixing a bug or implementing a suggestion/feature is most welcome. Fork this repo, implement your changes in a new branch and then create a PR referencing the issue you want to fix. Create a new issue if there is none already.

### Setting up a development environment

Use `JDK-1.8`, set JAVA_HOME, your PATH etc.

I recommend using the [Atlassian SDK](TODO: add link here) - see the link for more information on setting that up. Also see [Atlassian JIRA Plugin Development](TODO: add link here) to get started developing Atlassian (JIRA) plugins.

After setting up the Atlassian SDK run: `atlas-run`

This will fire up a local JIRA instance with the plugin installed and QuickReload enabled. Use `atlas-package` to rebuild the artifact and have it automatically picked up inside the local JIRA instance.

To fire up a local Bamboo instance to link to and test plan triggering run: `atlas-run-standalone --product bamboo -p 8080 -ajp 1234`

This will run a Bamboo instance on `localhost:8080/bamboo`. Use it to create test bamboo plans and for an application link from your local JIRA instance.

### Building a deployment package

Make sure you have set up your [development environment](#setting-up-a-development-environment).

`git-checkout` the version tag you want to build, e. g. v1.0.0.

Run `atlas-clean && atlas-package` to create the artifact inside the `target` folder.
