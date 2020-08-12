import { IDeckPlugin } from '@spinnaker/core';
import { randomWaitStage, initialize } from './RandomWaitStage';

export const plugin: IDeckPlugin = {
  initialize,
  stages: [randomWaitStage],
};
