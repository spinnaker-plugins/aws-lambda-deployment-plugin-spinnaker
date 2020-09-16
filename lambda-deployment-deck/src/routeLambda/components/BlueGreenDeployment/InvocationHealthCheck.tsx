// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  CheckboxInput,
  FormikFormField,
  HelpField,
  IFormInputProps, 
  IFormikStageConfigInjectedProps,
  NumberInput,
  JsonEditor,
} from '@spinnaker/core';

export function InvokeLambdaHealthCheck(props: IFormikStageConfigInjectedProps) {
  const { values, errors } = props.formik;

  const onPayloadChange = ( fieldValue: any): void => {
    props.formik.setFieldValue("lambdaPayload", fieldValue);
  };

  const onOutputChange = ( fieldValue: any): void => {
    props.formik.setFieldValue("lambdaOutput", fieldValue);
  };

  return (
    <div>
      <FormikFormField
        name="destroyOnFail"
        label="On Fail"
        input={props =>
          <CheckboxInput text={"Destroy latest lambda version on fail."} {...props} />
        }
      />
      <FormikFormField
        name="timeout"
        label="Timeout"
        input={props => <NumberInput {...props} min={0} max={900} />}
      />
      <FormikFormField
        name="lambdaPayload"
        label="Payload"
        input={(inputProps: IFormInputProps) => (
          <JsonEditor value={values.lambdaPayload} onChange={onPayloadChange} />
        )}
      />
      <FormikFormField
        name="lambdaOutput"
        label="Expected Output"
        input={(inputProps: IFormInputProps) => (
          <JsonEditor value={values.lambdaOutput} onChange={onOutputChange} />
        )}
      />
    </div>
  )
}
