// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  AccountService, 
  FormikFormField,
  HelpField,
  IAccount,
  IAccountDetails, 
  IFormInputProps,
  IFormikStageConfigInjectedProps,
  IFunction,
  IRegion,    
  ReactSelectInput,
  StageArtifactSelector,
  TextInput,
  useData, 
} from '@spinnaker/core';

import {  
  IAmazonFunctionSourceData,
} from '@spinnaker/amazon';

import {
  IStrategyConstant,
  DeploymentStrategyList,
  DeploymentStrategyPicker,
} from '../constants';

import {
  retrieveComponent,
} from './RenderStrategy';

export function DeploymentStrategyForm(props: IFormikStageConfigInjectedProps) {
  const { values, errors } = props.formik; 
   

  return (
    <div className="form-horizontal"> 
      {  values.deploymentStrategy ? 
        retrieveComponent(values.deploymentStrategy, props) : null
      }
    </div>
  );
}
