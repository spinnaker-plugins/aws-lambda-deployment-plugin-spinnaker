## Spinnaker Plugin for AWS Lambda Deployment

This plugin provides support for AWS Lambda Deployment via Pipelines in Spinnaker

## Version Compatibility
| Plugin  | Spinnaker Platform |
|:----------- | :--------- |
| 1.0.x  |  1.23.x |

This plugin is currently only compatible with Spinnaker platform 1.23.x and up. It is possible to run the plugin in an environment running an earlier release by making the following changes to your environment:
1. Checkout `master` branch for `spinnaker/orca`
2. Checkout `master` branch for `spinnaker/deck`
3. Checkout `master` branch for `spinnaker/clouddriver`
4. Install the plugin

## Requirements
1. This plugin requires Java 11
2. AWS Lambda functions must be enabled in your spinnaker environment [more info](https://aws.amazon.com/blogs/opensource/how-to-integrate-aws-lambda-with-spinnaker/)
 
## Plugin Users Guide

### Usage
1. Add the following to `orca-local.yml` in the necessary [profile](https://spinnaker.io/reference/halyard/custom/#custom-profiles) to load the Orca backend
```
spinnaker:
  extensibility:
    plugins:
      Aws.LambdaDeploymentPlugin:
        enabled: true
        version:<<VERSION NUMBER>> 
        extensions:
          Aws.LambdaDeploymentStage:
            enabled: true
       repositories:
         awsLambdaDeploymentPluginRepo:
           id: awsLambdaDeploymentPluginRepo
           url: https://raw.githubusercontent.com/awslabs/aws-lambda-deployment-plugin-spinnaker/release/0.0.1/plugins.json
```
2. Add the following to `gate-local.yml` in the necessary [profile](https://spinnaker.io/reference/halyard/custom/#custom-profiles) to load the Deck frontend
```
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
           url: https://raw.githubusercontent.com/awslabs/aws-lambda-deployment-plugin-spinnaker/release/0.0.1/plugins.json 
```
3. Execute `hal deploy apply` to deploy the changes


## Plugin Developers Guide

<details>
  <summary> Developing in 1.20.x Environment </summary>

### Overview

There are 2 main components to this plugin.   

* Orca (in the directory `aws-lambda-deployment-orca`) 
* Deck (in the directory `aws-lambda-deployment-deck`)

The development process involves the following high level steps:

1. Get orca and deck running on your development machine.
2. Get other services running elsewhere (typically on another EC2 instance or a kubernetes cluster)
3. Ensure your spinnaker instance is running fine - using your own local deck and orca instance.
2. Clone this repo and build the plugin
3. Configure your local deck to use your plugin.
4. Configure your local orca to load and use your plugin. 
5. Restart deck and orca, load the UI and make sure plugin is available.


Steps 1, 2 and 3 are best covered in the spinnaker documentation.

eg.
 
* [Test a Pipeline Stage Plugin](https://spinnaker.io/guides/developer/plugin-creators/deck-plugin)
* [Plugin Creators Overview](https://spinnaker.io/guides/developer/plugin-creators/overview/)

### Build

* `cd` to the root of the repository
* Build the plugin:

```./gradlew releaseBundle```

This should create the following files :

* `lambda-deployment-orca/build/aws-lambda-deployment-plugin-orca.plugin-ref`
*  `lambda-deployment-deck/build/dist/index.js`

Verify the above files have been created at the end of the gradle command above.


### Updating Orca

* Create a plugins directory in your orca project
* Copy the plugin-ref file from the build above to the plugins directory
* Create a orca-local.yml file in ~/.spinnaker/ with the following contents:

```
spinnaker:
   extensibility:
     plugins:
       Aws.LambdaDeploymentPlugin:
         enabled: true
         version: 1.0.1
         extensions:
           Aws.LambdaDeploymentStage:
             enabled: true
             config:
               defaultMaxWaitTime: 20
```

* Restart Orca (from your IntelliJ IDE)

### Updating Deck 

* Update the deck/plugin-manifest.json with the plugin information.

```
 [
     {
         "id": "Aws.LambdaDeploymentPlugin",
         "url": "./plugins/index.js",
         "version": "1.1.14"
     }
 ]
```

* Create a deck/plugins directory 
* Symlink `lambda-deployment-deck/build/dist/index.js` to deck/plugins/index.js like so:

```
cd <deck_root_dir>
ln -s <full_path_to_this_plugin>/lambda-deployment-deck/build/dist/index.js plugins/index.js
```

### Restart Deck

* Use this command:

`yarn run`

### Restart Orca

* Load your Orca project in the IDE and restart from there. Details here:
* [How to run Orca in IntelliJ](https://spinnaker.io/guides/developer/plugin-creators/deck-plugin/#run-orca-in-intellij)

### Verification

Load your deck UI typically `http://localhost:9000` and make sure your stage is available.   
The stage will typically show up with the name `AWS Lambda Deployment`

</details>

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.



