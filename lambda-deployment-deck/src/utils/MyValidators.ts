import { isNumber, isEmpty } from 'lodash';

import type { IValidator } from '@spinnaker/core';

const THIS_FIELD = 'This field';

const minValue = (min: number, message?: string): IValidator => {
  return function minValue(val: number, label = THIS_FIELD) {
    if (!isNumber(val)) {
      return message || `${label} must be a number - Empty or invalid inputs will perform a DELETE`;
    } else if (val < min) {
      const minText = min === 0 ? 'cannot be negative' : `cannot be less than ${min}`;
      return message || `${label} ${minText}`;
    } else if (val === 0 && label == "Reserved Concurrency") {
      return message || `Your function will be throttled.`;
    } else if (val === 0) {
      return message || `0 Will perform a DELETE - The minimum provisioned concurrency value allowed is 1.`;
    }
    return null;
  };
};

const maxValue = (max: number, message?: string): IValidator => {
  return function maxValue(val: number, label = THIS_FIELD) {
    if (val > max) {
      const maxText = `cannot be greater than ${max}`;
      return message || `${label} ${maxText}`;
    }
    return null;
  };
};

export const Validators = {
  maxValue,
  minValue,
};
