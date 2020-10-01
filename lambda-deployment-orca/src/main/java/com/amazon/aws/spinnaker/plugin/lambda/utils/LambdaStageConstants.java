/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.amazon.aws.spinnaker.plugin.lambda.utils;

import java.util.List;

public class LambdaStageConstants {

    public static final String lambaCreatedKey = "lambdaCreatedTaskUrl";
    public static final String lambaCodeUpdatedKey = "lambdaCodeUpdatedTaskUrl";
    public static final String lambaConfigurationUpdatedKey = "lambdaConfigUpdatedTaskUrl";
    public static final String lambaVersionPublishedKey = "lambdaVersionPublishedTaskUrl";
    public static final String lambaPutConcurrencyKey = "lambdaPutConcurrencyTaskUrl";

    public static final String createdUrlKey = "createdUrl";
    public static final String updateCodeUrlKey = "updateCodeUrl";
    public static final String updateConfigUrlKey = "updateConfigUrl";
    public static final String updateEventUrlKey = "updateEventUrl";
    public static final String publishVersionUrlKey = "publishVersionUrl";
    public static final String eventTaskKey = "eventConfigUrlList";
    public static final String aliasTaskKey = "updateAliasesUrlList";
    public static final String lambdaObjectKey = "lambdaObject";
    public static final String originalRevisionIdKey = "originalRevisionId";
    public static final String newRevisionIdKey = "newRevisionId";

    public static List<String> allUrlKeys = List.of(createdUrlKey, updateCodeUrlKey, updateConfigUrlKey, updateEventUrlKey, publishVersionUrlKey, lambaPutConcurrencyKey);
}
