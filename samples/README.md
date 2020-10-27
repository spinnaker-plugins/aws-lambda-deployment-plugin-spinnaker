## Spinnaker Plugin for AWS Lambda Deployment Samples
These files are sample configuration files for reference only. 

## Additional Configuration Guidance
- [Enabling Lambda Support](#enabling-lambda-support)
- [Enabling Plugin](#enabling-lambda-plugin)

### Enabling Lambda Support
When enabling AWS Lambda support for Spinnaker it is important to enable Lambda globally, as well as for each account as required.

In the below example Lambda is enabled globally for AWS and, also, explicity enabled for the `test` account. See the `config` sample file for more details.

```
aws:
  lambda:
    enabled: true
  accounts:
  - name: test
    lambdaEnabled: true
    accountId: 'xxxxxxxxxxxx'
    regions: 
    - name: us-west-2
    - name: us-east-1
    assumeRole: role/your-custom-role-name
```

### Enabling Lambda Plugin
Enabling the Spinnaker Plugin for AWS Lambda Deployment requires the addition of the plugin repository to Halyard config, and a Deck proxy in `gatel-local.yml`.

When enabling the plugin within Halyard config ensure proper indentation. Substitute `<<VERSION NUMBER>>` for the desired plugin version, and validate that the included plugin repository `url` contains the desired version.

```
  spinnaker:
    extensibility:
      plugins:
        Aws.LambdaDeploymentPlugin:
          enabled: true
          version: <<VERSION NUMBER>>
          extensions:
            Aws.LambdaDeploymentStage:
              enabled: true
      repositories:
        awsLambdaDeploymentPluginRepo:
          id: awsLambdaDeploymentPluginRepo
          url: https://raw.githubusercontent.com/awslabs/aws-lambda-deployment-plugin-spinnaker/release/0.0.1/plugins.json
```

## License

This project is licensed under the Apache-2.0 License.



