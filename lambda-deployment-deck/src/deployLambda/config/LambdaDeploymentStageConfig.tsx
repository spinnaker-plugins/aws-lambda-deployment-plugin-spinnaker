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

  validator.field('runtime', 'Runtime').required();

  validator.field('s3key', 'S3 Object Key').required();

  validator.field('handler', 'Handler').required();

  validator.field('functionUid', 'Function Name').required();

  validator
    .field('stackName', 'Stack Name')
    .optional()
    .withValidators(simpleStringValidator);

  validator
    .field('detailName', 'Detail Name')
    .optional()
    .withValidators(simpleStringValidator);

  validator
    .field('s3bucket', 'S3 Bucket Name')
    .required()
    .withValidators(s3BucketNameValidator);

  validator
    .field('role', 'Role ARN')
    .required()
    .withValidators(iamRoleValidator);

  return validator.validateForm();
}
