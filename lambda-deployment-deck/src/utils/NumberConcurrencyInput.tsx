import React from 'react';

import {
  IFormInputProps,
  OmitControlledInputPropsFrom,
  useInternalValidator,
  orEmptyString,
  validationClassName,
  IValidator,
  composeValidators,
} from '@spinnaker/core';

import {
  Validators
} from './MyValidators'

interface INumberInputProps extends IFormInputProps, OmitControlledInputPropsFrom<React.InputHTMLAttributes<any>> {
  inputClassName?: string;
}

const isNumber = (val: any): val is number => typeof val === 'number';

export function NumberConcurrencyInput(props: INumberInputProps) {
  const { value, validation, inputClassName, ...otherProps } = props;

  const minMaxValidator: IValidator = (val: any, label?: string) => {
    const minValidator = isNumber(props.min) ? Validators.minValue(props.min) : undefined;
    const maxValidator = isNumber(props.max) ? Validators.maxValue(props.max) : undefined;
    const validator = composeValidators([minValidator, maxValidator]);
    return validator ? validator(val, label) : null;
  };

  useInternalValidator(validation, minMaxValidator);

  const className = `NumberInput form-control ${orEmptyString(inputClassName)} ${validationClassName(validation)}`;
  return <input className={className} type="number" value={orEmptyString(value)} {...otherProps} />;
}