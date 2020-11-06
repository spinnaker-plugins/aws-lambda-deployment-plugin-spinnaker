# Lambda Deployment UI Plugin

## Overview
Contained within this directory are the Deck components to support AWS Lambda operations within Spinnaker pipeline stages. 

## Atomic Stages
### Deploy Lambda
The deploy Lambda stage (contained within `deployLambda` directory) manages the creation and updating of AWS Lambda infrastructure. 

#### Current Limitations
1. Aliases specified in the deploy stage will be created, and connected to `Version 1` of the created AWS Lambda function. Subsequent updates of the AWS Lambda function via the deploy stage will _not_ change the alias mapping.
2. Removing aliases from a deploy stage _after_ it has been successfully executed once will not remove the aliases from the AWS Lambda function.


### Route Lambda
The route Lambda stage (contained within `routeLambda` directory) manages the routing of traffic to Lambda function versions.

_Supported Stategies_
1. Simple: Routes 100% of alias traffic to the specified AWS Lambda function version
2. Weighted: Splits alias traffic between two specified AWS Lambda function versions with weights provided in the stage.
3. Blue Green Deployment: Routes 100% of alias traffic to the `$LATEST` AWS Lambda function version if, and only if, the AWS Lambda function passes the user specified health check.

### Delete Lambda
The delete Lambda stage (contained within `deleteLambda` directory) manages the deletion of Lambda infrastructure. The stage is configurable to specify a particular version to delete, set a static number of latest versions to maintain, or delete all infrastructure for a given function.

#### Current Limitations
1. The `Function Name` field within the delete stage is populated via the AWS Lambda function cache, and will only display functions within the cache. To provide free-form text inputs or SpEL expressions, directly edit the stage as JSON.

## Development
### Plugin Development Setup (Spinnaker 1.20.x)
While running the AWS Lambda Deployment Plugin in your development environment, changes may be made to the Deck plugin component without the need to restart Spinnaker services.

1. Install the plugin as described in the developer guide
2. Make desired changes to files within this directory
3. Execute `yarn build` from this directory
4. Reload Deck within your desired web browser

### Plugin Development Setup (Spinnaker 1.21.x or higher)
1. Install the plugin as described in the developer guide
2. Make desired changes to files within this directory
3. Execute `./gradlew releaseBundle` within the `aws-lambda-deployment-plugin-spinnaker` directory
4. Navigate to `aws-lambda-deployment-plugin-spinnaker/build/distributions`
5. Unzip the generated bundle
6. Navigate to `/opt/gate/plugins/`

## Testing
Due to the nature of the `@spinnaker/pluginsdk` library, unit testing is not possible for any file that contains dependencies on this library. The avaiable unit tests can be run by executing `yarn test` in this directory. 

# License
This project is licensed under the Apache-2.0 License.

