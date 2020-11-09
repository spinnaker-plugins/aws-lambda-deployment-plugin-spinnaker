// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import React from 'react';

import {
  ArtifactTypePatterns,
  excludeAllTypesExcept,
  FormikFormField,
  IArtifact,
  IExpectedArtifact,
  IFormInputProps, 
  IFormikStageConfigInjectedProps,
  NumberInput,
  StageArtifactSelectorDelegate,
} from '@spinnaker/core';

export function InvokeLambdaOperation(props: IFormikStageConfigInjectedProps) {
  const { values, errors } = props.formik;

  const excludedArtifactTypes = excludeAllTypesExcept(
    ArtifactTypePatterns.BITBUCKET_FILE,
    ArtifactTypePatterns.CUSTOM_OBJECT,
    ArtifactTypePatterns.EMBEDDED_BASE64,
    ArtifactTypePatterns.GCS_OBJECT,
    ArtifactTypePatterns.GITHUB_FILE,
    ArtifactTypePatterns.GITLAB_FILE,
    ArtifactTypePatterns.S3_OBJECT,
    ArtifactTypePatterns.HTTP_FILE,
  );

  const onPayloadChange = ( fieldValue: any): void => {
    props.formik.setFieldValue("lambdaPayload", fieldValue);
  };

  const onOutputChange = ( fieldValue: any): void => {
    props.formik.setFieldValue("lambdaOutput", fieldValue);
  };

  const onTemplateArtifactEdited = (artifact: IArtifact, name: string) => {
    props.formik.setFieldValue(`${name}.id`, null);
    props.formik.setFieldValue(`${name}.artifact`, artifact);
    props.formik.setFieldValue(`${name}.account`, artifact.artifactAccount);
  };

  const onTemplateArtifactSelected = (id: string, name: string) => {
    props.formik.setFieldValue(`${name}.id`, id);
    props.formik.setFieldValue(`${name}.artifact`, null);
  };

  const getInputArtifact = (stage: any, name: string) => {
    if (!stage[name]) {
      return {
        account: '',
        id: '',
      };
    } else {
      return stage[name];
    }
  };

  return (
    <div>
      <FormikFormField
        name="timeout"
        label="Timeout"
        input={props => <NumberInput {...props} min={0} max={900} />}
      />
      <FormikFormField
        name="executionCount"
        label="Execution Count"
        input={props => <NumberInput {...props} min={0} max={100} />}
      />

      <StageArtifactSelectorDelegate
        artifact={getInputArtifact(values, 'payloadArtifact').artifact}
        excludedArtifactTypePatterns={excludedArtifactTypes}
        expectedArtifactId={getInputArtifact(values, 'payloadArtifact').id}
        label="Payload Artifact"
        onArtifactEdited={artifact => {
          onTemplateArtifactEdited(artifact, 'payloadArtifact');
        }}
        helpKey={''}
        onExpectedArtifactSelected={(artifact: IExpectedArtifact) =>
          onTemplateArtifactSelected(artifact.id, 'payloadrtifact')
        }
        pipeline={props.pipeline}
        stage={values}
      />
      <StageArtifactSelectorDelegate
        artifact={getInputArtifact(values, 'outputArtifact').artifact}
        excludedArtifactTypePatterns={excludedArtifactTypes}
        expectedArtifactId={getInputArtifact(values, 'outputArtifact').id}
        label="Output Artifact"
        onArtifactEdited={artifact => {
          onTemplateArtifactEdited(artifact, 'outputArtifact');
        }}
        helpKey={''}
        onExpectedArtifactSelected={(artifact: IExpectedArtifact) =>
          onTemplateArtifactSelected(artifact.id, 'outputArtifact')
        }
        pipeline={props.pipeline}
        stage={values}
      />

    </div>
  )
}