// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  IFormikStageConfigInjectedProps,
} from '@spinnaker/core';

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
