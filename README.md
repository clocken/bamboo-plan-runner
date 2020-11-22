# Bamboo Plan Runner

An Atlassian JIRA workflow postfunction to run an Atlassian Bamboo Plan. It is possible to set variables for the plan to run.

## Installation

Either install it from the [Atlassian Marketplace](TODO: add link here) or [build it for yourself](TODO: add link here) and install it via the JIRA plugin manager.

## Configuration

The plugin uses [Atlassian Application Links](TODO: add link here) to trigger the configured plan on the Bamboo site. Thus once installed, ensure the following prerequesites are met to use the plugin:

- Create an _OAuth_ [application link](TODO: add link here) for each Atlassian Bamboo instance you want to trigger plan builds on
  - If you don't use the same user store for both JIRA and Bamboo sites, you will need to specify _OAuth (impersonating)_ for the Application Link and manually create all triggering users from JIRA on the Bamboo site
- Ensure all users that might trigger the workflow transition have _Build access_ to the plan being triggered

After the application links have been set up, you may add the postfunction to your workflow
