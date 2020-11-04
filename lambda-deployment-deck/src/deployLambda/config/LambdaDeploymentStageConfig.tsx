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
  awsArnValidator, 
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

  validator
    .field('triggerArns', 'Trigger ARNs')
    .optional()
    .withValidators((value:any, label: string) => {
        let tmp: any[]  = value.map((arn: string) => {
          return awsArnValidator(arn, arn);
        })
        let ret: boolean = tmp.every((el) => el === undefined);
        return ret ? undefined : "Invalid ARN. Event ARN must match regular expression: /^arn:aws[a-zA-Z-]?:[a-zA-Z_0-9.-]+:./"; 
      }) 

  return validator.validateForm();
}
