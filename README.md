# Build Plan Runner for Jira

An Atlassian Jira workflow postfunction to run an Atlassian Bamboo Build Plan. It is possible to set variables for the plan to run.

## Installation

As this plugin currently supports server installations only and Atlassian [plans to drop support for these](https://www.atlassian.com/migration/journey-to-cloud?tab=server-key-changes), I will not be providing this in the Atlassian Marketplace. I might implement cloud support in the future and provide that in the Marketplace. Until then you can download a [release](https://github.com/clocken/bamboo-plan-runner/releases) and install it via the [Jira plugin manager](https://confluence.atlassian.com/upm/installing-marketplace-apps-273875715.html#InstallingMarketplaceapps-Installingbyfileupload) or if you don't trust my builds [build it for yourself](CONTRIBUTING.md#building-a-deployment-package).

## Prerequisites

The plugin uses [Atlassian Application Links](https://confluence.atlassian.com/applinks/link-atlassian-applications-to-work-together-785449117.html) to trigger the configured plan on the Bamboo site. So once installed, ensure the following prerequisites are met before using the plugin:

- Create an [_OAuth_ application link](https://confluence.atlassian.com/bamboo/linking-to-another-application-360677713.html#Linkingtoanotherapplication-Impersonatingandnon-impersonatingauthenticationtypes) for each Atlassian Bamboo instance you want to trigger plan builds on
  - If you don't use the same user store for both Jira and Bamboo sites, you will need to specify _OAuth (impersonating)_ for the Application Link and manually create all triggering Jira users on the Bamboo site
- Ensure all users that might trigger the workflow transition have _Build access_ to the plan being triggered

## Configuration

Ensure the [prerequisites](#prerequisites) are met. Then:

- Add the _Run bamboo plan_ postfunction to your workflow transition
  
  <img src="doc/workflow-postfunction-selection.png" width="350px">

- Select the Bamboo instance and the plan to trigger
- You have two options to set values for variables of the plan
  - **Field:** Select a Jira issue field from which the value should be taken
  - **Custom:** Enter custom text and optionally use `$(field-id)` to substitute a Jira issue field value inside the text

    <img src="doc/variable-selection.png" width="350px">

    - Don't know the ID of a field? The selection list has the ID as a tooltip for each field

      <img src="doc/id-tooltip.png" width="300px">

## Contributing

Found a bug, have a feature request or just want to get involved? See [CONTRIBUTING](CONTRIBUTING.md).