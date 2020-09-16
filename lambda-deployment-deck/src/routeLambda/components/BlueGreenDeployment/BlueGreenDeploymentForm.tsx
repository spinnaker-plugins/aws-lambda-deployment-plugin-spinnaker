// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  FormikFormField,
  HelpField,
  IFormikStageConfigInjectedProps,
  IFormInputProps,
  JsonEditor,
  NumberInput,
  ReactSelectInput,
} from '@spinnaker/core';

import {
  IAmazonFunctionSourceData,
} from '@spinnaker/amazon';

import {
  IVersionConstant,
  VersionList,
} from '../../constants';

import {
  IHealthConstant,
  HealthCheckList,
} from './health.constants';

import { VersionPicker } from '../VersionPicker';

import { retrieveHealthCheck } from './HealthCheckStrategy';

export function BlueGreenDeploymentForm(props: IFormikStageConfigInjectedProps) {
  const { values, errors } = props.formik;
  const { functions } = props.application; 

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

