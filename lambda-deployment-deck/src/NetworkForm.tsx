import React from 'react';

import {
  FormikFormField,
  HelpField,
  IFormikStageConfigInjectedProps,
  IVpc,
  ReactSelectInput,
  useData,
} from '@spinnaker/core';

import { VpcReader } from '@spinnaker/amazon';

export default function NetworkForm( props: IFormikStageConfigInjectedProps) {
  const { values, errors } = props.formik;
  
  const { result: fetchVpcsResult, status: fetchVpcsStatus } = useData(
    () => VpcReader.listVpcs(),
    [],
    [],
  );

  const setVpc = (vpcId: string): void => {
    console.log("selected");
  }

  return( 
    <div className="form-group">
      {values.credentials && (
        <FormikFormField
          name="vpcId"
          label="VPC Id"
          help={<HelpField id="aws.function.vpc.id" />}
          input={props => (
            <ReactSelectInput
              {...props}
              stringOptions={fetchVpcsResult
                .filter((v: IVpc) => v.account === values.credentials)
                .map((v: IVpc) => v.id)}
              clearable={true}
            />
          )}
          onChange={setVpc}
          required={false}
        />
      )} 
    </div>
    );
} 
