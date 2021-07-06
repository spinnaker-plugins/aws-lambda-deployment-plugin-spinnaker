## Spinnaker Plugin for AWS Lambda Deployment

This plugin provides support for AWS Lambda Deployment via Pipelines in Spinnaker

### Version Compatibility
| Plugin  | Spinnaker Platform |
|:------------- | :--------- |
| 1.0.5 <= |  1.23.x |
| 1.0.6 >= |  1.26.x |

This plugin is currently only compatible with Spinnaker platform 1.23.x and up. It is possible to run the plugin in an environment running an earlier release by making the following changes to your environment:
1. Checkout `master` branch for `spinnaker/orca`
2. Checkout `master` branch for `spinnaker/deck`
3. Checkout `master` branch for `spinnaker/clouddriver`
4. Install the plugin

### Requirements
1. This plugin requires Java 11
2. AWS Lambda functions must be enabled in your spinnaker environment and for all required AWS accounts. Find more information [here](https://aws.amazon.com/blogs/opensource/how-to-integrate-aws-lambda-with-spinnaker/) or within [samples](samples/README.md).
 
### Plugin Deployment Guide

1. Add the following to the Halyard config (typically found at `~/.hal/config`) to load the Orca backend
```yaml
  spinnaker:
    extensibility:
      plugins:
        Aws.LambdaDeploymentPlugin:
          enabled: true
          version: <<VERSION_NUMBER>> 
          extensions:
            Aws.LambdaDeploymentStage:
              enabled: true
      repositories:
        awsLambdaDeploymentPluginRepo:
          id: awsLambdaDeploymentPluginRepo
          url: https://raw.githubusercontent.com/spinnaker-plugins/aws-lambda-deployment-plugin-spinnaker/master/plugins.json
```
2. Add the following to `gate-local.yml` in the necessary [profile](https://spinnaker.io/reference/halyard/custom/#custom-profiles) to load the Deck frontend
```yaml
spinnaker:
 extensibility:
    deck-proxy:
      enabled: true
      plugins:
        Aws.LambdaDeploymentPlugin:
          enabled: true
          version: <<VERSION NUMBER>>
    repositories:
      awsLambdaDeploymentPluginRepo:
        url: https://raw.githubusercontent.com/spinnaker-plugins/aws-lambda-deployment-plugin-spinnaker/master/plugins.json
```
3. Execute `hal deploy apply` to deploy the changes.
4. You should now be able to see 3 new stages provided by this plugin in the Deck UI when adding a new stage to your pipeline.

### Plugin User Guide

See the plugin user guide [here](UserGuide.md)

### Plugin TroubleShooting Guide

See the plugin user guide for troubleshooting instructions [here](UserGuide.md)

### Plugin Developer Guide

See the plugin developers guide [here](DeveloperGuide.md)

### Releasing New Versions

* Releases are done from the master branch
* Releases uses github actions. Scripts required for this are checked into the .github directory
* First update the version in gradle.properties and get that change merged to master.
* Then, to create a release, we tag the master branch commit with a release number (e.g. release-1.2.3) and push this tag

```
git tag 1.2.3
git push --tag
```

* The scripts in the .github directory trigger a build when this tag is pushed
* Once the build is successful, A new branch is created (called release-1.2.3) off this tag.
* A new commit is added to this branch that updates the plugin.json with artifacts produced by this build
* A PR is created for merging this commit to master. Merge this PR to master. 
* Navigate to the releases page [Releases](https://github.com/spinnaker-plugins/aws-lambda-deployment-plugin-spinnaker/releases) to make sure the new release shows up.
* Use the updated plugin.json in any new spinnaker deploys.

### Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

### License

This project is licensed under the Apache-2.0 License.




