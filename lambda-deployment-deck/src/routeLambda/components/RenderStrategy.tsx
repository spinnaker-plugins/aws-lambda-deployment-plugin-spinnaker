// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import { IFormikStageConfigInjectedProps } from '@spinnaker/core';

import { WeightedDeploymentForm } from './WeightedDeploymentForm';
import { SimpleDeploymentForm } from './SimpleDeploymentForm';
import { BlueGreenDeploymentForm } from './BlueGreenDeployment';

export function retrieveComponent(value: string, props: IFormikStageConfigInjectedProps) {
  switch(value) {
    case "$SIMPLE":
      return < SimpleDeploymentForm {...props} />; 
    case "$WEIGHTED":
      return < WeightedDeploymentForm {...props} />;
    case "$BLUEGREEN":
      return < BlueGreenDeploymentForm {...props} />; 
    default:
      return null;
  }
}
