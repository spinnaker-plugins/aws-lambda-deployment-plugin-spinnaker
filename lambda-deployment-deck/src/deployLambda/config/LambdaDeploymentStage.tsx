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
  IStage,
  IStageConfigProps,
  IStageTypeConfig,
  NumberInput,
  TextInput,
  Validators,
} from '@spinnaker/core';

import { LambdaDeploymentExecutionDetails } from './LambdaDeploymentStageExecutionDetails';
import { LambdaDeploymentConfig, validate } from './LambdaDeploymentStageConfig';

export const initialize = () => {
  HelpContentsRegistry.register('aws.lambdaDeploymentStage.lambda', 'Lambda Name');
};
 
export const lambdaDeploymentStage: IStageTypeConfig = {
  key: 'Aws.LambdaDeploymentStage',
  label: `AWS Lambda Deployment`,
  description: 'Create a single AWS Lambda Function',
  component: LambdaDeploymentConfig, // stage config
  executionDetailsSections: [LambdaDeploymentExecutionDetails, ExecutionDetailsTasks],
  validateFn: validate,
};
