// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

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
  IFormikStageConfigInjectedProps,
  IStage,
  IStageConfigProps,
  IStageTypeConfig, 
  StageFailureMessage, 
  Validators,
} from '@spinnaker/core';

import './LambdaDeleteStage.less';

import { DestroyLambdaFunctionStageForm } from './DestroyLambdaFunctionStageForm';

export function DestroyLambdaExecutionDetails(props: IExecutionDetailsSectionProps) {
  const { stage, name, current } = props;
  return (
    <ExecutionDetailsSection name={name} current={current}>
      <StageFailureMessage stage={stage} message={stage.outputs.failureMessage} />
      <div>
        <p> <b> Status: </b> {stage.outputs.message ? stage.outputs.message : "N/A"} </p> 
      </div>
    </ExecutionDetailsSection>
  );
}

/*
  IStageConfigProps defines properties passed to all Spinnaker Stages.
  See IStageConfigProps.ts (https://github.com/spinnaker/deck/blob/master/app/scripts/modules/core/src/pipeline/config/stages/common/IStageConfigProps.ts) for a complete list of properties.
  Pass a JSON object to the `updateStageField` method to add the `account` to the Stage.

  This method returns JSX (https://reactjs.org/docs/introducing-jsx.html) that gets displayed in the Spinnaker UI.
 */
function DestroyLambdaConfig(props: IStageConfigProps) {
  return (
    <div className="DestroyLambdaStageConfig">
      <FormikStageConfig
        {...props}
        validate={validate}
        onChange={props.updateStage}
        render={(props: IFormikStageConfigInjectedProps) => <DestroyLambdaFunctionStageForm {...props} />}  
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
  HelpContentsRegistry.register('aws.lambdaDeploymentStage.lambda', 'Lambda Name');
};

function validate(stageConfig: IStage) {
  const validator = new FormValidator(stageConfig);
  validator
    .field('account', 'Account Name')
    .required() 
  
  validator
    .field('region', 'Region')
    .required()

  validator
    .field('functionName', 'Lambda Function Name')
    .required()

  validator
   .field('version', 'Lambda Function Version')
   .required()

  return validator.validateForm();
}

export namespace DestroyLambdaExecutionDetails {
  export const title = 'Destroy Lambda Stage';
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
export const lambdaDeleteStage: IStageTypeConfig = {
  key: 'Aws.LambdaDeleteStage',
  label: `AWS Lambda Destroy`,
  description: 'Delete an AWS Lambda Function',
  component: DestroyLambdaConfig, // stage config
  executionDetailsSections: [DestroyLambdaExecutionDetails, ExecutionDetailsTasks],
  validateFn: validate,
};
