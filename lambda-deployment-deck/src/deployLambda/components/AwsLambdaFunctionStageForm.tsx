// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React, { useState } from 'react';
import classNames from 'classnames';

import {
  FormikFormField,
  HelpField,
  IFormikStageConfigInjectedProps,
  MapEditorInput,
  NumberInput,
  ReactSelectInput,
  TextInput,
} from '@spinnaker/core';

import {
  BasicSettingsForm,
  ExecutionRoleForm,
  LambdaAtEdgeForm, 
  NetworkForm,
  TriggerEventsForm,
} from '.';

import {
  AliasForm
} from './addAlias';


export function AwsLambdaFunctionStageForm(props: IFormikStageConfigInjectedProps) {
  const { values, errors } = props.formik; 

  const className = classNames({
    well: true,
    'alert-danger': !!errors.functionName,
    'alert-info': !errors.functionName,
  }); 

  return (
    <div className="form-horizontal">

      <div className={className}>
        <strong>Your function will be named: </strong>
        <HelpField id="aws.function.name" />
        <span>
          { values.functionName ? values.functionName : props.application.applicationName }
        </span>
        <FormikFormField name="functionName" input={() => null} />
      </div>

      <h4>Basic Settings</h4>
      < BasicSettingsForm {...props} />
      
      <h4> Execution Role </h4>
      < ExecutionRoleForm {...props} />

      <h4> Environment </h4>
      <FormikFormField
        name="envVariables"
        label="Env Variables"
        input={props => <MapEditorInput {...props} allowEmptyValues={true} addButtonLabel="Add" />}
      />
      <FormikFormField
        name="encryptionKMSKeyArn"
        label="Key ARN"
        help={<HelpField id="aws.function.kmsKeyArn" />}
        input={props => <TextInput {...props} />}
      />
      <h4> Tags </h4>
      <FormikFormField
        name="tags"
        input={props => <MapEditorInput {...props} allowEmptyValues={false} addButtonLabel="Add" />}
      />
      <h4> Settings </h4>
      <FormikFormField name="description" label="Description" input={props => <TextInput {...props} />} />
      <FormikFormField
        name="memorySize" 
        label="Memory (MB)"
        help={<HelpField id="aws.functionBasicSettings.memorySize" />}
        input={props => <NumberInput {...props} min={128} max={3008} />}
      />
      <FormikFormField
        name="timeout"
        label="Timeout (seconds)"
        help={<HelpField id="aws.functionBasicSettings.timeout" />}
        input={props => <NumberInput {...props} min={1} max={900} />}
      />
      <FormikFormField name="targetGroups" label="Target Group Name" input={props => <TextInput {...props} />} />

      <h4> Lambda@Edge </h4>
      < LambdaAtEdgeForm {...props} />
      
      <h4> Network </h4>
      < NetworkForm {...props} />

      <h4> Event Triggers </h4>
      < TriggerEventsForm {...props} />      
      {/* 
      <h4> Aliases </h4>
      < AliasForm {...props} />
      */}
      <h4> Debugging and Error Handling </h4>
      Dead Letter Config
      <FormikFormField
        name="deadLetterConfig.targetArn"
        label="Target ARN"
        help={<HelpField id="aws.function.deadletterqueue" />}
        input={props => <TextInput {...props} />}
      />
      X-Ray Tracing
      <FormikFormField
        name="tracingConfig.mode"
        label="Mode"
        help={<HelpField id="aws.function.tracingConfig.mode" />}
        input={props => <ReactSelectInput {...props} stringOptions={['Active', 'PassThrough']} clearable={true} />}
      />
    </div>
  );
}
