## Lambda Deployment UI Plugin

### Overview

### Atomic Stages
#### Deploy Lambda
The deploy Lambda stage (contained within `deployLambda` directory) manages the creation and updating of AWS Lambda infrastructure. 

#### Route Lambda
The route Lambda stage (contained within `routeLambda` directory) manages the routing of traffic to Lambda function versions.

#### Destroy Lambda
The destroy Lambda stage (contained within `destroyLambda` directory) manages the deletion of Lambda infrastructure. The stage is configurable to specify a particular version to delete, set a static number of latest versions to maintain, or delete all infrastructure for a given function.

### Development
#### Plugin Development
The UI plugin can be build by executing `yarn build` or `yarn watch` from this directory. By following the development installation of the plugin, changes should be rendered in your Deck UI.  

## License

This project is licensed under the Apache-2.0 License.

