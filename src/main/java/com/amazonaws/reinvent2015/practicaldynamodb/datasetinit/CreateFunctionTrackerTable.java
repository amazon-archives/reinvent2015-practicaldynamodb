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

import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.FUNCTION_TRACKER_TABLE_NAME;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.SEGMENT;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.dynamodb;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

public class CreateFunctionTrackerTable {

    public static void main(String[] args) {
        dynamodb.createTable(new CreateTableRequest()
                .withTableName(FUNCTION_TRACKER_TABLE_NAME)
                .withKeySchema(new KeySchemaElement(SEGMENT, KeyType.HASH))
                .withAttributeDefinitions(
		        		       new AttributeDefinition(SEGMENT, ScalarAttributeType.N))
                .withProvisionedThroughput(new ProvisionedThroughput(15L, 15L))
                );
        System.out.println("Table created: " + FUNCTION_TRACKER_TABLE_NAME);
    }
}
