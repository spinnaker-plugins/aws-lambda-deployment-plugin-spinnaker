import React, {useState} from 'react';

import {
  FormikFormField,
  HelpField,
  IFormikStageConfigInjectedProps,
  IFormInputProps,
  IStage,
  ISubnet,
  IVpc,
  NetworkReader,
  ReactSelectInput,
  SecurityGroupReader,
  SubnetReader,
  useData,
} from '@spinnaker/core';

import { uniqBy } from 'lodash';


export function NetworkForm( props: IFormikStageConfigInjectedProps ) { 
  const { values, errors } = props.formik; 
 
  const { result: fetchVpcsResult, status: fetchVpcsStatus } = useData(
    () => NetworkReader.listNetworksByProvider('aws'),
    [],
    [],
  );

  const { result: fetchSubnetsResult, status: fetchSubnetsStatus } = useData(
    () => SubnetReader.listSubnetsByProvider('aws'), 
    [],
    [],
  );

//  const { result: fetchSGsResult, status: fetchSGsStatus } = useData(
//    () => SecurityGroupReader.loadSecurityGroups(),
//    [],
//    [],
//  );
  
//  console.log(fetchSGsResult); 
  const dedupedSubnets = uniqBy(
    fetchSubnetsResult
      .filter((s: ISubnet) => s.vpcId === values.vpcId),
    "id"
  ) 
 
  return( 
    <div>
      <FormikFormField
        name="vpcId"
        label="VPC Id"
        help={<HelpField id="aws.function.vpc.id" />}
        input={(inputProps: IFormInputProps) => (
          <ReactSelectInput
            {...inputProps}
            isLoading={fetchVpcsStatus === 'PENDING'}
            stringOptions={fetchVpcsResult
              .filter((v: IVpc) => v.account === values.account)
              .filter((v: IVpc) => v.deprecated === false)
              .map((v: IVpc) => v.id)}
            clearable={true}
          />
        )} 
        required={false}
      />      
      <FormikFormField
        name="subnets"
        label="Subnet Id"
        help={<HelpField id="aws.function.vpc.id" />}
        input={(inputProps: IFormInputProps) => (
          <ReactSelectInput
            {...inputProps}
            isLoading={fetchSubnetsStatus === 'PENDING'}
            stringOptions={dedupedSubnets
              .map((s: ISubnet) => s.id)
            } 
            clearable={true}
          />
        )} 
        required={false}
      />
    </div>
  );
} 
