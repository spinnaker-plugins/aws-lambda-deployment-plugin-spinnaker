// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

"use strict"

import {
  iamRoleValidator,
  s3BucketNameValidator,
  awsArnValidator,
  awsTagsValidator,
} from '../aws.validators';

test('A Valid IAM Role string is validated', () => {
  const label = "IAM Role";
  const value = "arn:aws:iam::111122223333:role/testRoleName";

  expect(iamRoleValidator(value, label)).toBeUndefined();
});

test('An invalid IAM Role string is invalid', () => {
  const label = "IAM Role";
  const value = "testIamRole invalid !@#";

  expect(iamRoleValidator(value, label)).toBe( 
    "Invalid role.  IAM Role must match regular expression: arn:aws:iam::d{12}:role/?[a-zA-Z_0-9+=,.@-_/]+"
  )
});

test('A valid S3 bucket name is validated', () => {
  const label = "S3 Bucket";
  const value = "Valid-S3-Bucket-Name123";

  expect(s3BucketNameValidator(value, label)).toBeUndefined();
});

test('An invalid S3 bucket name is invalid', () => {
  const label = "S3 Bucket";
  const value = "Invalid_S3_Bucket_Name123";

  expect(s3BucketNameValidator(value, label)).toBe(
    "Invalid S3 Bucket name.  S3 Bucket must match regular expression: [0-9A-Za-z.-]*[^.]$"
  );
});

test('A valid ARN is validated', () => {
  const label = "Test SQS";
  const value = "arn:aws:sqs:us-east-1:111122223333:testSQS";

  expect(awsArnValidator(value, label)).toBeUndefined();
});

test('An invalid ARN is invalid', () => {
  const label = "Test SQS";
  const value = "arn:foo:bar:111122223333:foobar";

  expect(awsArnValidator(value, label)).toBe(
    "Invalid ARN.  Test SQS must match regular expression: /^arn:aws[a-zA-Z-]?:[a-zA-Z_0-9.-]+:./"
  )
});

test('A single tag is validated', () => {
  const label = "Test Tags";
  const value = { foo: "bar" };

  expect(awsTagsValidator(value, label)).toBeUndefined();
});

test('No tags is invalid', () => {
  const label = "Test Tags";
  const value = { };

  expect(awsTagsValidator(value, label)).toBe(
    "At least one Test Tags is required"
  )

});
