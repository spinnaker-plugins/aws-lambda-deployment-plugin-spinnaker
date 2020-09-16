// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import { Option } from 'react-select';

import {
  FormikFormField,
  HelpField,
  IFormikStageConfigInjectedProps,
  IFormInputProps,
  ISecurityGroup,
  IStage,
  ISubnet,
  IVpc,
  NetworkReader,
  ReactInjector,
  ReactSelectInput,
  SecurityGroupReader,
  SubnetReader,
  TetheredSelect,
  useData,
} from '@spinnaker/core';

import { uniqBy } from 'lodash';

const toSubnetOption = (value: ISubnet): Option<string> => {
  return { value: value.id, label: value.id };
};

function AliasTable() {
  return (
    <div >
      <table className="table table-condensed packed">
        <thead>
          <tr>
            <th></th>
            <th>Alias</th>
            <th>Provisioned Concurrency</th>
            <th>Trigger ARN</th>

            <th style={{width: '58px'}}>Actions</th>
          </tr>
        </thead>
        <tfoot>
          <tr>
            <td colSpan={5}>
              <button type="button" className="btn btn-block btn-sm add-new" onClick={() => console.log("Hello")}>
                <span className="glyphicon glyphicon-plus-sign" />
                {"Add Function Alias"}
              </button>
            </td>
          </tr>
        </tfoot>
      </table>
    </div>
  )
}

export function AliasForm(props: IFormikStageConfigInjectedProps) {

  return (
    <FormikFormField
      name="aliases" 
      input={(inputProps: IFormInputProps) => (
        < AliasTable />
      )}
      required={false}
    />
  )
} 

