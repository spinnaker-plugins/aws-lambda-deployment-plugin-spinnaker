// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  FormikFormField,
  IFormikStageConfigInjectedProps,
  IFormInputProps,
  ReactSelectInput,
} from '@spinnaker/core';

import {
  HealthCheckList,
} from './health.constants';

import { retrieveHealthCheck } from './HealthCheckStrategy';

export function BlueGreenDeploymentForm(props: IFormikStageConfigInjectedProps) {
  const { values, errors } = props.formik;

  return (
    <div>
      <FormikFormField
        label="Health Check Type"
        name="healthCheckType"
        input={(inputProps: IFormInputProps) => (
          <ReactSelectInput
            {...inputProps}
            clearable={false}
            options={
              HealthCheckList 
            }
          />
        )}
      />
      {  values.healthCheckType ?
        retrieveHealthCheck(values.healthCheckType, props) : null
      }
    </div>
  )
}

