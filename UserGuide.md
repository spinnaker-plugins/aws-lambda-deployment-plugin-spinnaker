## Spinnaker Plugin for AWS Lambda Deployment - User Guide

### Requirements

1. This guide assumes the plugin is deployed and configured. Use the README.md to deploy the plugin
2. Note that configuration of the plugin should be done for deck and orca.
3. Separately configuration of clouddriver should be done to enable lambda (functions).
4. AWS Lambda functions must be enabled in your spinnaker environment and for all required AWS accounts. 

### Overview

This plugin provides 4 stages for spinnaker

* Lambda Deployment
* Lambda Deletion
* Lambda Traffic Management
* Lambda Invocation

#### Lambda Deployment

* Use this to create/update lambda
* If lambda exists, it is updated, otherwise a new lambda is created.
* It is also possible to create the lambda outside of spinnaker and use this stage only for updating code or specific configuration.

#### Lambda Deletion

* Use this to delete specific versions of the lambda or the lambda itself.
* You can also use this stage to manage how many older versions of the lambda you want to keep

#### Lambda Traffic Management

After lambda is created use this to direct traffic to lambda.
1. Simple Deployment: Create an alias and point the version at this lambda
2. Weighted Deployment: Create an alias and point the alias at upto 2 versions of the lambda
3. Blue/Green Deployment: Invoke the lambda and 

#### Lambda Invocation

* Use this stage to invoke lambda multiple times and capture output
* This stage can be used to generate traffic and logs after a new version of lambda is deployed
* It can be part of a canary analysis where further stages can be used to analyse traffic and make decisions on updating traffic.
* So a lambda deployment pipeline would involve:
    * Deployment stage to create/update lambda based on github commits/ cron jobs etc.
    * Inocation stage to call the newly created version and invoke the lambda multiple times, generating traffic.
    * Canary analysis stage (not part of this plugin) to analyse traffic
    * Traffic management stage to update an alias to this new version if canary succeeds.
    * Or a delete stage to delete the newly created version if the canary fails.

#### Troubleshooting

* Coming soon...

#### Other Resources

* The following guides provide examples of plugin development:
  
    * [Spinnaker setup](https://spinnaker.io/setup/install/)
    * [AWS Blog Post on enabling Lambda in spinnaker](https://aws.amazon.com/blogs/opensource/how-to-integrate-aws-lambda-with-spinnaker/)
	* [Test a Pipeline Stage Plugin](https://spinnaker.io/guides/developer/plugin-creators/deck-plugin)
	* [Plugin Creators Overview](https://spinnaker.io/guides/developer/plugin-creators/overview/)

