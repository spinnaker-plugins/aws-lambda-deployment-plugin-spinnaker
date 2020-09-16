// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  FormikFormField,
  HelpField,
  IFormikStageConfigInjectedProps,
  MapEditorInput,
  TextInput,
} from '@spinnaker/core';

export function ExecutionRoleForm( props: IFormikStageConfigInjectedProps ) {
  return(
    <div>
      <FormikFormField
        name="envVariables"
        label="Env Variables"
        input={props => <MapEditorInput {...props} allowEmptyValues={true} addButtonLabel="Add" />}
      />
      <FormikFormField
        name="KMSKeyArn"
        label="Key ARN"
        help={<HelpField id="aws.function.kmsKeyArn" />}
        input={props => <TextInput {...props} />}
      />
    </div>
  );
}
