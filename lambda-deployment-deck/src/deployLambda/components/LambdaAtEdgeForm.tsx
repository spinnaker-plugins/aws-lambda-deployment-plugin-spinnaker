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
  "lambdaAtEdge": "",
  "distribution": "The CloudFront distribution that will send events to your Lambda function.",
  "cloudfrontEvent": "The CloudFront distribution that will send events to your Lambda function.",
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
        label="Enable Lambda@Edge"
        help={<HelpField content={helpFieldContent.lambdaAtEdge} />}
        input={props => <CheckboxInput {...props} />}
      />
      <FormikFormField
        name="edgeDistribution"
        label="Distribution ARN"
        help={<HelpField content={helpFieldContent.distribution} />}
        input={props => <TextInput {...props} />}
      />
      <FormikFormField
        label="cloudfrontEvent"
        name="CloudFront Event"
        help={<HelpField content={helpFieldContent.cloudfrontEvent} />}
        input={(inputProps: IFormInputProps) => (
          <ReactSelectInput
            {...inputProps}
            clearable={false}
            options={cloudFrontEvents}
          />
        )}
      />
    </div>
  )
}
