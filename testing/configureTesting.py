import json
import time 
import random
import string
import argparse
import subprocess

scenarios = ["Scenario1","Scenario2","Scenario3","Scenario4","Scenario5","Scenario6","Scenario7","Scenario8"]

class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

def pprint_status(scenario, status):
  if (status == "ERROR") or (status == "TERMINAL"):
    print(bcolors.FAIL + "Pipeline {} Failed.".format(scenario) + bcolors.ENDC)
  elif status == "SUCCEEDED":
    print(bcolors.OKGREEN + "Pipeline {} Succeeded.".format(scenario) + bcolors.ENDC)
  else:
    print(bcolors.WARNING + "Pipeline {} in state {}.".format(scenario, status) + bcolors.ENDC)

def validate_process(stream, msg):
  if (msg in stream):
    return True
  else:
    return False

class ConfigurePipelines:
  def __init__(
    self,
    appName,
    accountId,
    lambdaRole,
    s3Bucket,
    eventArn,
    qualifier=None,
    no_execute=False
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
    self.no_execute = no_execute
  
  def perform_executions(self): 
    for scenario in scenarios:
      # Create pipeline in application...
      create_cmd =  'spin pipeline save --file {}/pipeline-{}.json'.format(scenario, self.qualifier)
      process = subprocess.Popen(create_cmd.split(), stdout=subprocess.PIPE)
      output, error = process.communicate()

      if not( validate_process(output.decode(), "Pipeline save succeeded") ):    
        continue
      else:
        print("Successfully Created Pipeline {}-{}".format(scenario, self.qualifier))

      # Execute pipeline...
      exec_cmd = 'spin pipeline execute --application {} --name {}-{}'.format(self.appName, scenario, self.qualifier)
      
      process = subprocess.Popen(exec_cmd.split(), stdout=subprocess.PIPE)
      output, error = process.communicate()

      if not( validate_process(output.decode(), "Pipeline execution started") ):
        continue
      else:
        print("Successfully Executed Pipeline {}-{}".format(scenario, self.qualifier))

  def setup_monitor(self):
    self.pipeline_status = {}
    self.pipeline_lookup = {}

    all_pipelines_cmd = "spin pipeline list --application {}".format(self.appName)
    process = subprocess.Popen(all_pipelines_cmd.split(), stdout=subprocess.PIPE)
    output, error = process.communicate()

    try: 
      pipeline_json = json.loads(output.decode())
    except:
      print("Unable to monitor pipeline executions!")

    for pipeline in pipeline_json:
      self.pipeline_status[pipeline["id"]] = "NULL"
      self.pipeline_lookup[pipeline["id"]] = pipeline["name"]

  def query_pipeline(self, pipeline_id):
    query_cmd = "spin pipeline execution list --pipeline-id {}".format(pipeline_id)
    process = subprocess.Popen(query_cmd.split(), stdout=subprocess.PIPE)
    output, error = process.communicate()

    status = "ERROR"

    try:
      executions_json = json.loads(output.decode())
      status = executions_json[0]['status']
    except:
      print("Unable to retrieve status for pipeline id {}!".format(pipeline_id))

    return status


  def monitor_executions(self):
    self.setup_monitor()
    while any([True if ( i == 'NULL' or i == 'RUNNING') else False for i in self.pipeline_status.values()]):
      print("Checking pipeline statuses...")
      for pipeline in self.pipeline_status.keys():
        self.pipeline_status[pipeline] = self.query_pipeline(pipeline)
      time.sleep(5)

    for pipeline in self.pipeline_status.keys():
      pprint_status(self.pipeline_lookup[pipeline], self.pipeline_status[pipeline]) 
    print("Functional testing complete.")


  def formatScenario(self, scenarioJson):
    configStage = []
    scenarioJson['application'] = scenarioJson["application"].format(self.appName)
    scenarioJson['name'] = scenarioJson["name"].format(self.qualifier)
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
      print("Configuring {}...".format(scenario))
      with open("{}/pipeline.json".format(scenario), 'r') as f:
        scenarioJson = json.load(f)
      
      configuredScenario = self.formatScenario(scenarioJson)

      json.dump(configuredScenario, open("{}/pipeline-{}.json".format(scenario, self.qualifier), 'w'), indent=2, sort_keys=True)

    if not(self.no_execute):
      self.perform_executions()
      
    self.monitor_executions()
        

if __name__ == "__main__":
  parser = argparse.ArgumentParser()
  parser.add_argument('--app-name', help='Spinnaker application name for test pipelines', required=True)
  parser.add_argument('--account-id', help='Account name that is registered in your Spinnaker environment', required=True)
  parser.add_argument('--lambda-role', help='ARN of the role that will be assigned to your lambda', required=True)
  parser.add_argument('--s3-bucket', help='Name of the S3 bucket that your psuedo code is in', required=True)
  parser.add_argument('--event-arn', help='ARN of a single valid Lambda trigger', required=True)
  parser.add_argument('--no-execute', action='store_true')

  parser.add_argument('--qualifier', help='String that will be appended to the scenario pipeline configuration', required=False)

  args = parser.parse_args()
  
  config = ConfigurePipelines(args.app_name, args.account_id, args.lambda_role, args.s3_bucket, args.event_arn, args.qualifier, args.no_execute)
  config.run()
