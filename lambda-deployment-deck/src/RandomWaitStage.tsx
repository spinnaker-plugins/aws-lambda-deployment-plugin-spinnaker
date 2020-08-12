import React from 'react';

import {
  ExecutionDetailsSection,
  ExecutionDetailsTasks,
  FormikFormField,
  FormikStageConfig,
  FormValidator,
  HelpContentsRegistry,
  HelpField,
  IExecutionDetailsSectionProps,
  IStage,
  IStageConfigProps,
  IStageTypeConfig,
  NumberInput,
  Validators,
} from '@spinnaker/core';

import './RandomWaitStage.less';

export function LambdaDeploymentExecutionDetails(props: IExecutionDetailsSectionProps) {
  return (
    <ExecutionDetailsSection name={props.name} current={props.current}>
      <div>
        <p>Waited {props.stage.outputs.timeToWait} second(s)</p>
      </div>
    </ExecutionDetailsSection>
  );
}

/*
  IStageConfigProps defines properties passed to all Spinnaker Stages.
  See IStageConfigProps.ts (https://github.com/spinnaker/deck/blob/master/app/scripts/modules/core/src/pipeline/config/stages/common/IStageConfigProps.ts) for a complete list of properties.
  Pass a JSON object to the `updateStageField` method to add the `maxWaitTime` to the Stage.

  This method returns JSX (https://reactjs.org/docs/introducing-jsx.html) that gets displayed in the Spinnaker UI.
 */
function LambdaDeploymentConfig(props: IStageConfigProps) {
  return (
    <div className="LambdaDeploymentConfig">
      <FormikStageConfig
        {...props}
        validate={validate}
        onChange={props.updateStage}
        render={(props) => (
          <FormikFormField
            name="maxWaitTime"
            label="Max Time To Wait"
            help={<HelpField id="armory.randomWaitStage.maxWaitTime" />}
            input={(props) => <NumberInput {...props} />}
          />
        )}
      />
    </div>
  );
}

/*
  This is a contrived example of how to use an `initialize` function to hook into arbitrary Deck services. 
  This `initialize` function provides the help field text for the `LambdaDeploymentConfig` stage form defined above.

  You can hook into any service exported by the `@spinnaker/core` NPM module, e.g.:
   - CloudProviderRegistry
   - DeploymentStrategyRegistry

  When you use a registry, you are diving into Deck's implementation to add functionality. 
  These registries and their methods may change without warning.
*/
export const initialize = () => {
  HelpContentsRegistry.register('armory.randomWaitStage.maxWaitTime', 'The maximum time, in seconds, that this stage should wait before continuing.');
};

function validate(stageConfig: IStage) {
  const validator = new FormValidator(stageConfig);

  validator
    .field('maxWaitTime')
    .required()
    .withValidators((value, label) => (value < 0 ? `${label} must be non-negative` : undefined));

  return validator.validateForm();
}

export namespace LambdaDeploymentExecutionDetails {
  export const title = 'lambdaDeploymentStage';
}

/*
  Define Spinnaker Stages with IStageTypeConfig.
  Required options: https://github.com/spinnaker/deck/master/app/scripts/modules/core/src/domain/IStageTypeConfig.ts
  - label -> The name of the Stage
  - description -> Long form that describes what the Stage actually does
  - key -> A unique name for the Stage in the UI; ties to Orca backend
  - component -> The rendered React component
  - validateFn -> A validation function for the stage config form.
 */
export const randomWaitStage: IStageTypeConfig = {
  key: 'lambdaDeploymentStage',
  label: `Lambda Deployment`,
  description: 'Stage that manages AWS Lambda function deployment: Creation, Update, Delete, Versioning',
  component: LambdaDeploymentConfig, // stage config
  executionDetailsSections: [LambdaDeploymentExecutionDetails, ExecutionDetailsTasks],
  validateFn: validate,
};
