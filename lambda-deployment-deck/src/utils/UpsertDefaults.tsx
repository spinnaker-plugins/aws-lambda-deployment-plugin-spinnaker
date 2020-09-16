// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

export function upsertDefaults(initialValues: any, defaultValues: any) {
  Object.entries(defaultValues).forEach(([key, value]) => {
    if (!initialValues[key]){
      initialValues[key] = value;
    }
  })
  return initialValues;
}
