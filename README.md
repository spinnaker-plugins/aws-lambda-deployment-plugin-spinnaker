## Spinnaker Plugin for AWS Lambda Deployment

This plugin provides support for AWS Lambda Deployment via Pipelines in Spinnaker.  This repository is in transition
from its current distribution as a plugin into to the core Spinnaker project. Updates will be less frequent until
the migration is complete and feature parity is achieved in future Spinnaker releases.

### Version Compatibility
| Plugin  | Spinnaker Platform |
|:------------- |:-------------------|
| 1.0.11 >= | 1.29.x             |
| 1.2.0 >= | 1.30.x             |
| 1.2.0 <= | 1.32.x             |

This plugin is currently only compatible with Spinnaker platform 1.28.x and up.

## Major changes

- 11/02/2022 - Release 1.1.0 - removes older versions of the runtime engine from the UI.  This means editing older pipelines will no longer let you use the unsupported lambda runtimes.  Please see https://docs.aws.amazon.com/lambda/latest/dg/lambda-runtimes.html for questions/information.
- 09/21/2023 - Release 1.2.0 - Adds the ability to override clouddriver native functionality and supports clouddriver plugin.  Removed references to unsupported spinnaker versions.

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

  # you can also optionally configure cache refresh retries and timeouts.  Several settings are for 
  # overall service communication timeouts should be set globally.   
  # https://github.com/spinnaker/kork/blob/master/kork-web/src/main/groovy/com/netflix/spinnaker/okhttp/OkHttpClientConfigurationProperties.groovy#L29-L32
  lambdaPluginConfig:
    cacheRefreshRetryWaitTime: 15 # defaults to 15 sec
    cacheOnDemandRetryWaitTime: 15 # defaults to 15 sec
    cloudDriverPostRequestRetries: 5 # defaults to 5.  Disable if you don't want duplicates.
    cloudDriverRetrieveNewPublishedLambdaWaitSeconds: 40 # defaults to 40 sec
    cloudDriverRetrieveMaxValidateWeightsTimeSeconds: 240 # defaults to 240 sec
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




