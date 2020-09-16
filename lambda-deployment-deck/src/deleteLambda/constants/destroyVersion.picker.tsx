// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import { IFormikStageConfigInjectedProps } from '@spinnaker/core';

import {
  IDestroyVersionConstant,
  DestroyVersionList,
} from './destroyVersion.constants';

export interface IVersionPickerProps{
  config: IFormikStageConfigInjectedProps;
  value: string;
  showingDetails: boolean;
}

export interface IVersionPickerState{
  value: string;
  label: string;
  description: string;
}

export class DestroyVersionPicker extends React.Component<IVersionPickerProps, IVersionPickerState> {
  constructor(props: IVersionPickerProps) {
    super(props);

    const { value } = this.props;

    const versionDetails = DestroyVersionList
        .filter((v: IDestroyVersionConstant) => v.value === value)[0]

    this.state = {
      label: versionDetails.label,
      value: versionDetails.value,
      description: versionDetails.description
    }
  }

  public render() {
    return(
      <div>
        <b>  { this.state.label }  </b>
        <br/>
        <small> { this.state.description } </small>
      </div>
    )
  }
}
