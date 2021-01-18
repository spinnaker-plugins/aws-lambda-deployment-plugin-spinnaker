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

import './LambdaUpdateCodeStage.less';
import {
  s3BucketNameValidator,
} from '../utils/aws.validators';
import { UpdateCodeLambdaFunctionStageForm } from './components/UpdateCodeStageForm';

export function LambdaUpdateCodeConfig(props: IStageConfigProps) {
  return (
    <div className="LambdaUpdateCodeConfig">
      <FormikStageConfig
        {...props}
        validate={validate}
        onChange={props.updateStage}
        render={(props: IFormikStageConfigInjectedProps) => <UpdateCodeLambdaFunctionStageForm {...props} />}
      />
    </div>
  );
}

export function validate(stageConfig: IStage) {
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
      .field('s3key', 'S3 Object Key')
      .required()

  validator
      .field('s3bucket', 'S3 Bucket Name')
      .required()
      .withValidators(s3BucketNameValidator);

  return validator.validateForm();
}
