// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  CheckboxInput,
  FormikFormField,
  HelpField,
  IFormikStageConfigInjectedProps,
  IFormInputProps,
  ReactSelectInput, 
  TextInput,
} from '@spinnaker/core';

const helpFieldContent = {
  "lambdaAtEdge": "Validate AWS Lambda function configuration against Lambda@Edge requirements. This will not enable Lambda@Edge on this function. ",
}

const cloudFrontEvents = [
  { label: "Origin request", value: "originRequest"},
  { label: "Origin response", value: "originResponse"},
  { label: "Viewer request", value: "viewerRequest"},
  { label: "Viewer response", value: "viewerResponse"},
]

export function LambdaAtEdgeForm( props: IFormikStageConfigInjectedProps ) {
  const { values, errors } = props.formik;
  if (values.region !== "us-east-1") {
    return( 
      <div className="horizontal center">
        Lambda@Edge is only available in region us-east-1. 
      </div>
    )
  }
  return (
    <div>
      <FormikFormField
        name="enableLambdaAtEdge"
        label="Enable Lambda@Edge Validation"
        help={<HelpField content={helpFieldContent.lambdaAtEdge} />}
        input={props => <CheckboxInput {...props} />}
      />
    </div>
  )
}
