## Functional Testing
Utilities for functional testing of the AWS Lambda Deployment Plugin Spinnaker. A summary of the testing scenarios may be found in `FunctionalTests.xlsx`, including expected outputs.

### Test Setup
1. Create an S3 bucket and upload `lambda_func.zip` to the bucket
2. Populate `pipeline.json` files by executing `configureTesting.py`
3. For each scenario create a pipeline and edit the pipeline JSON
4. Paste the `pipeline-<uniqueID>.json` contents into your pipeline JSON
5. Save pipeline


### Running Tests
1. Navigate to the desired pipeline
2. Select `Start Manual Execution`

### Configure Testing JSON
The included python script `configureTesting.py` will autopopulate each testing scenario with provided credentials. The script requires the following inputs:

- `--app-name`: Spinnaker application name for test pipelines
- `--accound-id`: AWS account name that is registered with Spinnaker
- `--lambda-role`: Name of AWS Lambda execution role to be used
- `--s3-bucket`: Name of the S3 bucket containing `lambda_func.zip` created in Step 1 of Setup
- `--event-arn`: ARN for a resource to operate as AWS Lambda event trigger
- `--qualifier`: [OPTIONAL] Provide a string to append to the `pipeline.json` file name. If not provided, a random qualifier will be generated.

