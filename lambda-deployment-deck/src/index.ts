import { IDeckPlugin } from '@spinnaker/core';
import { lambdaDeploymentStage, initialize } from './LambdaDeploymentStage';

export const plugin: IDeckPlugin = {
  initialize,
  stages: [lambdaDeploymentStage],
};
