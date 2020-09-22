// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

export interface IDestroyVersionConstant {
  description: string;
  label: string;
  value: string;
}

export const DestroyVersionList: IDestroyVersionConstant[] = [ 
  {
    label: 'Newest Function Version',
    value: '$LATEST',
    description: 'Destroy the most recently deployed function version when this stage starts.',
  },
  {
    label: 'Previous Function Version',
    value: '$PREVIOUS',
    description: 'Destroy the second-most recently deployed function version when this stage starts.',
  },
  {
    label: 'Older Than N',
    value: '$MOVING',
    description: 'Destroy all version but the N most recent versions.', 
  },
  {
    label: 'Provide Version Number',
    value: '$PROVIDED',
    description: 'Provide a specific version number to destroy.',
  },
  {
    label: 'All Function Versions',
    value: '$ALL',
    description: 'Destroy all function versions and function infrastructure.',
  },
]
