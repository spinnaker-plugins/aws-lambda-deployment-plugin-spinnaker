## Spinnaker Plugin for AWS Lambda Deployment - Developer Guide


### Requirements
1. This plugin requires Java 11
2. AWS Lambda functions must be enabled in your spinnaker environment and for all required AWS accounts. Find more information [here](https://aws.amazon.com/blogs/opensource/how-to-integrate-aws-lambda-with-spinnaker/) or within [samples](samples/README.md).
 
### Overview

There are 2 main components to this plugin.   

* Orca (in the directory `aws-lambda-deployment-orca`) 
* Deck (in the directory `aws-lambda-deployment-deck`)

The development process for developing this plugin involves setting these services up on a separate (Ubuntu) box different from your main development machine.  
This remote box (typically on AWS) will run all the services and a ssh tunnel will be setup in the development box to connect to them.

These are the high level steps:


1. Get spinnaker services running on a remote box (typically on another EC2 instance or a kubernetes cluster)
3. Get orca and deck running on your development machine.
3. Ensure your spinnaker instance is running fine - using your own local deck and orca instance.
4. Clone this repo and build the plugin
5. Configure your local deck to use your plugin.
6. Configure your local orca to use your plugin. 
7. Restart deck and orca, load the UI and make sure plugin is available.


### Details


#### Setting up the Remote Box

* Get familiar with the spinnaker architecture as described here: [Spinnaker Architecture](https://spinnaker.io/reference/architecture/)

	There are close to a dozen microservices that work together to provide the spinnaker service.   
	

*  Setup a remote development machine (remote box) to run these services:

To do this, use this guide [here](https://spinnaker.io/setup/install/) to get spinnaker up and running on the remote box.    
In particular, in step 3 of this guide [Step 3](https://spinnaker.io/setup/install/environment/) follow the local-git installation instructions to setup the remote box. 
      
 At the end of this install you will have spinnaker running on a remote ubuntu box.

Now on your development machine, setup an ssh tunnel to all these services so that they can be accessed by localhost urls from the web browser on your development machine.

e.g.: Use a script like so:

```
REMOTE_BOX=192.168.0.100
REDIS=6379:localhost:6379
DECK=9000:localhost:9000
GATE=8084:localhost:8084
ECHO=8089:localhost:8089
ORCA=8083:localhost:8083
FRONT=8080:localhost:8080
FIAT=7003:localhost:7003
CLOUDDRIVER=7002:localhost:7002
CMD="ssh -AnNT -L ${REDIS} -L ${DECK} -L ${FRONT} -L ${FIAT} -L ${GATE} -L ${ECHO} -L ${CLOUDDRIVER} ${REMOTE_BOX}"
```

Once the tunnel is running you should be able to access the spinnaker UI (called Deck) using this url : http://localhost:9000

* Enable Lambda in the remote box. This involves changing 2 files one for clouddriver and another for the deck UI. Details here: [AWS Lambda for spinnaker](https://aws.amazon.com/blogs/opensource/how-to-integrate-aws-lambda-with-spinnaker/)

Sample clouddriver.yml aws section. In this configuration, aws lambda is enabled, for account aws-managed-1 and disabled for aws-managing. Note that lambda will work only for the specified regions in each account:

```
aws:
  enabled: true
  lambda:
    enabled: true
  accounts:
  - name: aws-managing
    requiredGroupMembership: []
    permissions: {}
    lambdaEnabled: false
    accountId: '584245600472'
    regions:
    - name: us-east-1
    - name: us-west-2
    assumeRole: role/spinnakerManaged
    lifecycleHooks: []
  - name: aws-managed-1
    requiredGroupMembership: []
    permissions: {}
    lambdaEnabled: true
    accountId: '222014522391'
    regions:
    - name: us-east-1
    - name: us-west-2
    assumeRole: role/spinnakerManaged
    lifecycleHooks: []
```

Sample settings.js lambda section for deck:

```
window.spinnakerSettings.feature.functions = true
```


* Once lambda is enabled as above, you should be able to see the lambda tab under infrastructure in the Deck UI. Make sure you are able to create lambdas using the UI and that existing lambdas are visible in the list view.


#### Setting up the Local Development Box

Now that the remote box is setup for development, you will setup the local development box.

High level steps:

* Clone and build deck & orca from github
* Configure deck and orca on the dev box by adding some configuration in `${HOME}/.spinnaker`
* Import Projects into your IDE and start the services.
* Stop the deck server in the remote box.
* Stop the orca server in the remote box.
* Adjust your tunnel scripts to stop tunneling the deck and orca ports to remote box.
 
#### Setting up Orca on the Local Dev Box.

* Clone the spinnaker orca github repo [Orca Github](https://github.com/spinnaker/orca) in your local development box. This release is built using the release-1.23.x branch of orca 
* Build the orca repo and import it into your IntelliJ IDE.  
* Start the orca server and make sure it runs fine. 
*  Create a .spinnaker directory in HOME directory and make sure it has an orca.yml file. Look at the samples directory of this repo for some examples.  example [here](samples/orca-local.yml)
* At this point, shut down the orca server on the remote box. You will be using orca from the development box not the remote box. This can be done using scripts (stop-*.sh) created by the spinnaker installation in steps above.   
* Start a reverse tunnel for the orca server from your development box to the remote box. This is typically done by a script such as:

```
REMOTE_BOX=192.168.0.100
ORCA=8083:localhost:8083
CMD="ssh -NT -R ${ORCA} ${REMOTE_BOX}"
```

* If you are using IntelliJ, import the gradle project into your IDE and use a run configuration similar to one in the samples directory. Example: [here](samples/orca_debug_config.png)


#### Setting up Deck on the Local Dev Box.

Similar to above:


* Clone the spinnaker deck github repo [Deck Github](https://github.com/spinnaker/deck) , build the repo, import it in your IDE
* Stop the tunnel to deck like in the steps above
* Stop the deck service in your remote box.
* Start the deck server on your dev box like so: 

```
#!/bin/bash
. ~/.nvm/nvm.sh
nvm use 12.16.0
export SETTINGS_PATH=~/.spinnaker/settings.js
echo "starting yarn..."
yarn start
```

Sample deck configuration is available in the samples directory. [settings-local.js](samples/settings-local.js)

Ensure your deck UI is still functional (at http://localhost:9000)

At this point, you have orca and deck running on your development box and all other services running on the remote box. The next step is to tell orca (and deck) to load our plugin

#### Setting up the Plugin for Development

Now that orca and deck are running in your dev box, you will configure the plugin as well:

* Clone this plugin repo on your development box. 
* Build the repo:    
	* `cd` to the root of the repository
	*  Build the plugin using ```./gradlew releaseBundle```

* This should create the following files :

	* `lambda-deployment-orca/build/aws-lambda-deployment-plugin-orca.plugin-ref`
	*  `lambda-deployment-deck/build/dist/index.js`

* Verify the above files have been created at the end of the gradle command above.

* In the gradle view of your intelliJ IDE, add the lambda-deployment-orca project from this plugin.

* In the orca directory of your development box (From `Setting up the Local Development Box`), create a plugins directory, and within that create a symlink to your plugin like so:

```
mkdir -p ${orca_repo_root}/plugins
ln -s ${lambda_plugin_repo_root}/build/Aws.LambdaDeploymentPlugin-orca.plugin-ref ${orca_repo_root}/plugins
mkdir -p ${orca_repo_root}/orca-web/plugins
ln -s ${lambda_plugin_repo_root}/build/Aws.LambdaDeploymentPlugin-orca.plugin-ref ${orca_repo_root}/orca-web/plugins
```

* Create a plugins directory in your deck installation and copy over the output of the deck plugin build onto there:

```
rsync -avr lambda-deployment-deck/ ${DECK_DIR}/plugins
```

* During development it may be better to create a symlink instead of copying over the files.

* From your IDE, start orca. There is an image with sample Debug configuration for IntelliJ in the samples directory.

* At this point your IntelliJ IDE is running orca with this lambda deployment plugin enabled.

* Start the deck server with a command like so:

```
#!/bin/bash
. ~/.nvm/nvm.sh
nvm use 12.16.0
export SETTINGS_PATH=~/.spinnaker/settings.js
echo "starting yarn..."
yarn start
```

#### Verification and making changes

1. Navigate to your Deck UI and make sure you see the new stages added by this plugin. 
2. Create a new pipeline, run the pipeline, set breakpoints in the plugin and ensure these breakpoints are hit.
3. At this point, you can modify code in the plugin and see the changes take effect. 
4. Sometimes, the IDE is unable to load changes from the plugin at runtime and you may need to restart orca after making changes to the code in the plugin.


### Build

Building the plugin. First Verify java version:

```
java -version
java version "11.0.8" 2020-07-14 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.8+10-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.8+10-LTS, mixed mode)
```

Then run the build:

```
gradlew releaseBundle
echo "Completed at $(date)"
```


### Updating Orca

* Create a plugins directory in your orca project
* Copy the plugin-ref file from the build above to the plugins directory
* Create a orca-local.yml file in ~/.spinnaker/ with the following contents:

```yaml
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

* During backend development you may need to build only orca:

```
cd lambda-deployment-orca
rm -rf build/
../gradlew build
ln -s <full_path_to_orca_repo>/plugins/Aws.LambdaDeploymentPlugin-orca.plugin-ref <full path to Aws.LambdaDeploymentPlugin-orca.plugin-ref> 
```


### Updating Deck 

* Update the deck/plugin-manifest.json with the plugin information.

```json
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

```bash
cd <deck_root_dir>
ln -s <full_path_to_this_plugin>/lambda-deployment-deck/build/dist/index.js plugins/index.js
```

### Restart Deck

* Use this command:

`yarn run`

### Restart Orca

* Load your Orca project in the IDE and restart from there. Details here:
* [How to run Orca in IntelliJ](https://spinnaker.io/guides/developer/plugin-creators/deck-plugin/#run-orca-in-intellij)

### Other Resources

* The following guides provide examples of plugin development:
  
    * [Spinnaker setup](https://spinnaker.io/setup/install/)
    * [AWS Blog Post on enabling Lambda in spinnaker](https://aws.amazon.com/blogs/opensource/how-to-integrate-aws-lambda-with-spinnaker/)
	* [Test a Pipeline Stage Plugin](https://spinnaker.io/guides/developer/plugin-creators/deck-plugin)
	* [Plugin Creators Overview](https://spinnaker.io/guides/developer/plugin-creators/overview/)


### Verification

Load your deck UI typically `http://localhost:9000` and make sure your stage is available.   
The stage will typically show up with the name `AWS Lambda Deployment`




