// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  FormikStageConfig,
  FormValidator,  
  IFormikStageConfigInjectedProps,
  IStage,
  IStageConfigProps,
} from '@spinnaker/core';

import './LambdaDeploymentStage.less';
import { 
  s3BucketNameValidator, 
  iamRoleValidator,
  simpleStringValidator,
} from '../../utils/aws.validators';
import {constructNewAwsFunctionTemplate} from './function.defaults';
import {upsertDefaults} from '../../utils/UpsertDefaults';
import { AwsLambdaFunctionStageForm } from '../components/AwsLambdaFunctionStageForm';

export function LambdaDeploymentConfig(props: IStageConfigProps) {
  const defaultFunction = constructNewAwsFunctionTemplate();

  return (
    <div className="LambdaDeploymentConfig">
      <FormikStageConfig
        {...props}
        stage={upsertDefaults(props.stage,defaultFunction)}
        validate={validate}
        onChange={props.updateStage}
        render={(props: IFormikStageConfigInjectedProps) => <AwsLambdaFunctionStageForm {...props} />}
      />
    </div>
  );
}

export function validate(stageConfig: IStage) {
  const validator = new FormValidator(stageConfig);
  validator
    .field('s3bucket', 'S3 Bucket Name')
    .optional()
    .withValidators(s3BucketNameValidator);

  validator
    .field('stackName', 'Stack Name')
    .required()
    .withValidators(simpleStringValidator);

  validator
    .field('detailName', 'Detail Name') 
    .withValidators(simpleStringValidator);

  validator
    .field('role', 'Role ARN')
    .required()
    .withValidators(iamRoleValidator);
  return validator.validateForm();
}
