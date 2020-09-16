// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React, {useState} from 'react';

import { Option } from 'react-select';

import {
  AccountService,
  CheckboxInput,
  FormikFormField,
  HelpField,
  IAccount,
  IAccountDetails,
  IFormikStageConfigInjectedProps,
  IFormInputProps,
  IRegion,
  ISecurityGroup,
  IStage,
  ISubnet,
  IVpc,
  NameUtils,
  NetworkReader,
  ReactInjector,
  ReactSelectInput,
  SecurityGroupReader,
  SubnetReader,
  TetheredCreatable,
  TetheredSelect,
  TextInput,
  useData,
} from '@spinnaker/core';

import { uniqBy } from 'lodash';

import {
  availableRuntimes,
  lambdaHelpFields,
} from './function.constants';

export function BasicSettingsForm( props: IFormikStageConfigInjectedProps ) { 
  const { values, errors } = props.formik; 

  const setFunctionName = () => {
    const ns = NameUtils.getClusterName( props.application.applicationName, values.stackName, values.detailName );
    const fn = values.functionUid;

    props.formik.setFieldValue("functionName", `${ns}-${fn}`)
  }

  const onAliasChange = (o: Option, field: any) => {
    props.formik.setFieldValue(field, o.value);
  }

  const onFunctionUidChange = (fieldValue: string) => {
    props.formik.setFieldValue('functionUid', fieldValue);
    setFunctionName();
  }

  const onStackNameChange = (fieldValue: string) => {
    props.formik.setFieldValue('stackName', fieldValue);
    setFunctionName();
  }

  const onDetailChange = (fieldValue: string) => {
    props.formik.setFieldValue('detailName', fieldValue);
    setFunctionName();
  }

  const { result: fetchAccountsResult, status: fetchAccountsStatus } = useData(
    () => AccountService.listAccounts('aws'),
    [],
    [],
  );

  return( 
    <div>
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
        label="Region"
        name="region"
        input={(inputProps: IFormInputProps) => (
          <ReactSelectInput
            {...inputProps}
            clearable={false}
            isLoading={fetchAccountsStatus === 'PENDING'}
            stringOptions={fetchAccountsResult
              .filter((acc: IAccountDetails) => acc.name === values.account)
              .flatMap((acc: IAccountDetails) => acc.regions)
              .map((reg: IRegion) => reg.name)
            }
          />
        )}
      />

      <FormikFormField
        name="functionUid"
        label="Function Name"
        onChange={onFunctionUidChange}
        help={<HelpField id="aws.function.name" />}
        input={props => <TextInput {...props} />}
      />

      <FormikFormField
        name="stackName"
        label="Stack"
        help={< HelpField content={lambdaHelpFields.stack}/>} 
        onChange={onStackNameChange}
        input={props => <TextInput {...props} />}
      />
      <FormikFormField
        name="detailName"
        label="Detail"
        help={< HelpField content={lambdaHelpFields.detail}/>}
        onChange={onDetailChange}
        input={props => <TextInput {...props} />}
      />
      { /*
      <FormikFormField
        name="aliasNames"
        label="Alias Name"
        help={<HelpField content="The resource ARNs for Lambda event trigger sources. Input the entire ARN and select `Create option TRIGGER-ARN-INPUT` to add the ARN." />}
        input={(inputProps: IFormInputProps) => (
          <TetheredCreatable
            {...inputProps}
            multi={false}
            clearable={false}
            placeholder={
              "Input Alias Name..."
            }
            onChange={(e: Option) => {
                onAliasChange(e, 'aliases');
            }}
            value={values.aliases ?
              ({value: values.aliases, label: values.aliases}) :
              null 
            }
          />
        )}
        required={true}
      />
      */ }
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
      <FormikFormField
        name="publish"
        label="Publish"
        help={<HelpField id="aws.function.publish" />}
        input={props => <CheckboxInput {...props} />}
      />

    </div>
  );
} 
