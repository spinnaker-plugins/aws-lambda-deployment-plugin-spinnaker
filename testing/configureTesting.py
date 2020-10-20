import json
import random
import string
import argparse

scenarios = ["Scenario8"]

class ConfigurePipelines:
  def __init__(
    self,
    appName,
    accountId,
    lambdaRole,
    s3Bucket,
    eventArn,
    qualifier=None
  ):
    if qualifier == None:
      letters = string.ascii_lowercase
      qualifier = ( ''.join(random.choice(letters) for i in range(10)) )
    
    self.appName = appName
    self.accountId = accountId
    self.lambdaRole = lambdaRole
    self.s3Bucket = s3Bucket
    self.eventArn = eventArn
    self.qualifier = qualifier
  
  def formatScenario(self, scenarioJson):
    configStage = []
    for stage in scenarioJson["stages"]:
      stage["functionName"] = stage["functionName"].format(self.appName)
      stage["account"] = self.accountId
      stage["role"] = self.lambdaRole
      stage["s3bucket"] = self.s3Bucket

      if "triggerArns" in stage.keys():
        stage["triggerArns"] = [ self.eventArn ]

      configStage.append(stage)

    scenarioJson["stages"] = configStage

    return scenarioJson

  def run(self):
    for scenario in scenarios:
      print("Configuring {}".format(scenario))
      with open("{}/pipeline.json".format(scenario), 'r') as f:
        scenarioJson = json.load(f)
      
      configuredScenario = self.formatScenario(scenarioJson)

      json.dump(configuredScenario, open("{}/pipeline-{}.json".format(scenario, self.qualifier), 'w'), indent=2, sort_keys=True)

if __name__ == "__main__":
  parser = argparse.ArgumentParser()
  parser.add_argument('--app-name', help='Spinnaker application name for test pipelines', required=True)
  parser.add_argument('--account-id', help='Account name that is registered in your Spinnaker environment', required=True)
  parser.add_argument('--lambda-role', help='ARN of the role that will be assigned to your lambda', required=True)
  parser.add_argument('--s3-bucket', help='Name of the S3 bucket that your psuedo code is in', required=True)
  parser.add_argument('--event-arn', help='ARN of a single valid Lambda trigger', required=True)

  parser.add_argument('--qualifier', help='String that will be appended to the scenario pipeline configuration', required=False)

  args = parser.parse_args()
  
  config = ConfigurePipelines(args.app_name, args.account_id, args.lambda_role, args.s3_bucket, args.event_arn, args.qualifier)
  config.run()
