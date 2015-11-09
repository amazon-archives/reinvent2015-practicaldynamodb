/*******************************************************************************
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *******************************************************************************/
package com.amazonaws.reinvent2015.practicaldynamodb.datasetinit;

import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.SCORE_ID;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.SCORE_TABLE_NAME;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.dynamodb;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import com.amazonaws.services.dynamodbv2.model.StreamViewType;

public class CreateScoresTable {

    public static void main(String[] args) {
        dynamodb.createTable(new CreateTableRequest()
                .withTableName(SCORE_TABLE_NAME)
                .withKeySchema(new KeySchemaElement(SCORE_ID, KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition(SCORE_ID, ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput(20L, 20L))
                .withStreamSpecification(new StreamSpecification()
                        .withStreamEnabled(true)
                        .withStreamViewType(StreamViewType.NEW_IMAGE))
                );
    }
}
