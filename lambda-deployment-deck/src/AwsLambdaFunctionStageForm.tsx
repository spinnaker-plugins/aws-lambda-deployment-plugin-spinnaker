import React, { useState } from 'react';
import classNames from 'classnames';

import {
  AccountService,
  CheckboxInput,
  FormikFormField,
  HelpField,
  IArtifact,
  IAccount,
  IExpectedArtifact,
  IFormInputProps,
  IFormikStageConfigInjectedProps,
  IgorService,
  MapEditorInput,
  NumberInput,
  ReactSelectInput,
  StageArtifactSelector,
  TextInput,
  useData,
  YamlEditor,
} from '@spinnaker/core';

import NetworkForm from './NetworkForm';

const availableRuntimes = [
  'nodejs10.x',
  'nodejs12.x',
  'java8',
  'java11',
  'python2.7',
  'python3.6',
  'python3.7',
  'python3.8',
  'dotnetcore2.1',
  'dotnetcore3.1',
  'go1.x',
  'ruby2.5',
  'ruby2.7',
  'provided',
];

export function AwsLambdaFunctionStageForm(props: IFormikStageConfigInjectedProps) {
  const { values, errors } = props.formik;
  
  const { result: fetchAccountsResult, status: fetchAccountsStatus } = useData(
    () => AccountService.listAccounts('aws'),
    [],
    [],
  );

  const onFieldChange = (fieldName: string, fieldValue: any): void => {
    props.formik.setFieldValue(fieldName, fieldValue);
  };

  const className = classNames({
    well: true,
    'alert-danger': !!errors.functionName,
    'alert-info': !errors.functionName,
  });

  return (
    <div className="form-horizontal">
      {values.isNew && (
        <div className={className}>
          <strong>Your function will be named: </strong>
          <HelpField id="aws.function.name" />
          <span>
            {props.application.applicationName}-{values.functionName}
          </span>
          <FormikFormField name="functionName" input={() => null} />
        </div>
      )}
      <h4>Basic Settings</h4>
      <FormikFormField
        label="Account"
        name="account"
        input={(inputProps: IFormInputProps) => (
          <ReactSelectInput
            {...inputProps}
            clearable={false} 
            isLoading={fetchAccountsStatus === 'PENDING'}
            stringOptions={fetchAccountsResult.map((acc: IAccount) => acc.name)}
          />
        )}
      />
      <FormikFormField
        name="functionName"
        label="Function Name"
        help={<HelpField id="aws.function.name" />}
        input={props => <TextInput {...props} />}
      />  
      <FormikFormField
        name="runtime"
        label="Runtime"
        help={<HelpField id="aws.function.runtime" />}
        input={props => <ReactSelectInput {...props} stringOptions={availableRuntimes} clearable={true} />}
      />
      <FormikFormField
        name="s3bucket"
        label="S3 Bucket"
        help={<HelpField id="aws.function.s3bucket" />}
        input={props => <TextInput {...props} placeholder="S3 bucket name" />}
      />
      <FormikFormField
        name="s3key"
        label="S3 Key"
        help={<HelpField id="aws.function.s3key" />}
        input={props => <TextInput {...props} placeholder="object.zip" />}
      />
      <FormikFormField
        name="handler"
        label="Handler"
        help={<HelpField id="aws.function.handler" />}
        input={props => <TextInput {...props} placeholder="filename.method" />}
      />
      <FormikFormField name="publish" label="Publish" input={props => <CheckboxInput {...props} />} />
      <h4> Execution Role </h4>
      <FormikFormField
        name="role"
        label="Role ARN"
        input={props => <TextInput {...props} placeholder="Enter role ARN" name="role" />}
        required={true}
      />
      <h4> Environment </h4>
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
      <h4> Network </h4>
      
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
