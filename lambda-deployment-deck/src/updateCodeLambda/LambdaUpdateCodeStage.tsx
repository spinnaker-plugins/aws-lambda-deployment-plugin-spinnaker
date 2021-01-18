// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  ExecutionDetailsTasks,
  HelpContentsRegistry,
  IStageTypeConfig,
} from '@spinnaker/core';

import { LambdaUpdateCodeExecutionDetails } from './LambdaUpdateCodeStageExecutionDetails';
import { LambdaUpdateCodeConfig, validate } from './LambdaUpdateCodeStageConfig';

export const initialize = () => {
  HelpContentsRegistry.register('aws.lambdaDeploymentStage.lambda', 'Lambda Name');
};
 
export const lambdaUpdateCodeStage: IStageTypeConfig = {
  key: 'Aws.LambdaUpdateCodeStage',
  label: `AWS Lambda Update Code`,
  description: 'Update code for a single AWS Lambda Function',
  component: LambdaUpdateCodeConfig, // stage config
  executionDetailsSections: [LambdaUpdateCodeExecutionDetails, ExecutionDetailsTasks],
  validateFn: validate,
};
