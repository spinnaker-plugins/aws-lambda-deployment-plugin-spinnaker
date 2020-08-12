## AWS Spinnaker plugin for AWS Lambda Deployment

This plugin provides support for AWS Lambda Deployment in Spinnaker

## Setup for Development

### Build

* `cd` to the root of the repository
* Build the plugin:

```./gradlew releaseBundle```

This should create the following files :

* `lambda-deployment-orca/build/aws-lambda-deployment-plugin-orca.plugin-ref`
*  `lambda-deployment-deck/build/dist/index.js`


### Updating Orca

* Create a plugins directory in your orca project
* Copy the plugin-ref file from the build above to the plugins directory
* Create a orca-local.yml file in ~/.spinnaker/ with the following contents:

```
Coming soon
```

* Restart deck

### Updating Deck 

* Update the deck/plugin-manifest.json with the plugin information.

File contents below:
```
Coming soon
```

* Create a deck/plugins directory 
* Symlink `lambda-deployment-deck/build/dist/index.js` to deck/plugins/index.js like so:

```
cd <deck_root_dir>
ln -s <full_path_to_this_plugin>/lambda-deployment-deck/build/dist/index.js plugins/index.js
```

### Restart Deck

* Use this command:

```yarn run```

### Restart Orca

* Restart from IDE

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.
