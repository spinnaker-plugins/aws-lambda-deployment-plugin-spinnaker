// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  FormikFormField,
  IFormikStageConfigInjectedProps,
  TextInput,
} from '@spinnaker/core';

export function ExecutionRoleForm( props: IFormikStageConfigInjectedProps ) {
  return(
    <FormikFormField
      name="role"
      label="Role ARN"
      input={props => <TextInput {...props} placeholder="Enter role ARN" name="role" />}
      required={true}
    />
  );
}
