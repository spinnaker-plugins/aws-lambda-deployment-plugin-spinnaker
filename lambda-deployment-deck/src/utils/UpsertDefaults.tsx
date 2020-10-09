// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {isEmpty} from 'lodash';

export function upsertDefaults(initialValues: any, defaultValues: any) {
  Object.entries(defaultValues).forEach(([key, value]) => {
    if (!initialValues[key] && !isEmpty(value)){
      initialValues[key] = value;
    }
  })
  return initialValues;
}
