# Contributing

If you find a bug or have a suggestion for improvement or a feature request, please open an issue here.

Help fixing a bug, implementing a suggestion/feature, improving documentation or anything else you think needs to be done is most welcome.

- Create a new issue for your work if there is none already
- Fork this repo
- Implement your changes in a new branch
- Create a PR referencing the issue you're working on

## Setting up a development environment

Use `JDK-1.8`, set JAVA_HOME, your PATH etc.

I recommend using the [Atlassian SDK](https://developer.atlassian.com/server/framework/atlassian-sdk/) - see the link for more information on setting that up. Also see [Atlassian JIRA Plugin Development](https://developer.atlassian.com/server/framework/atlassian-sdk/set-up-the-atlassian-plugin-sdk-and-build-a-project/) to get started developing Atlassian (JIRA) plugins.

After setting up the Atlassian SDK execute `atlas-run` on the root folder. This will fire up a local JIRA instance with the plugin installed and [QuickReload](https://developer.atlassian.com/server/framework/atlassian-sdk/automatic-plugin-reinstallation-with-quickreload/) enabled. Use `atlas-package` to rebuild the artifact and have it automatically picked up inside the local JIRA instance.

To fire up a local Bamboo instance to link to and test plan triggering run `atlas-run-standalone --product bamboo -p 8080 -ajp 1234` preferrably outside the root folder to keep it clean. This will run a Bamboo instance on `localhost:8080/bamboo` (the `-ajp` is necessary to avoid an AJP port clash with the running JIRA instance). Use it to create test bamboo plans and for an application link from your local JIRA instance.

## Building a deployment package

Make sure you have set up your [development environment](#setting-up-a-development-environment).

`git-checkout` the version tag/commit/branch you want to build, e. g. `v1.0.0`.

Run `atlas-clean && atlas-package` on the root folder to create the artifact inside the `target` folder.