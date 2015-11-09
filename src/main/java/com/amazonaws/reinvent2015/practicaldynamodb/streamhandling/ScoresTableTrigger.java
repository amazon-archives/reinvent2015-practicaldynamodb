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
package com.amazonaws.reinvent2015.practicaldynamodb.streamhandling;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.reinvent2015.practicaldynamodb.converter.DataTransformer;
import com.amazonaws.reinvent2015.practicaldynamodb.internal.InternalCalls;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;

/*
 * For more information on handling DynamoDB events in AWS Lambda, see
 * "Building NoSQL Database Triggers with Amazon DynamoDB and AWS Lambda"
 * on the AWS Compute Blog:
 * https://aws.amazon.com/blogs/compute/619/
 *
 * For information on building and publishing Java Lambda functions with the
 * AWS Toolkit for Eclipse, see the Toolkit's User Guide:
 * http://docs.aws.amazon.com/AWSToolkitEclipse/latest/GettingStartedGuide/lambda-tutorial.html
 */
public class ScoresTableTrigger implements RequestHandler<DynamodbEvent, Object> {
    @Override
    public Object handleRequest(DynamodbEvent input, Context context) {
        context.getLogger().log("Input: " + input);

        DynamoDB dynamodb = new DynamoDB(Regions.US_WEST_2);

        for (DynamodbStreamRecord record : input.getRecords()) {
            Map<String, AttributeValue> newData = record.getDynamodb().getNewImage();
            if (newData == null) continue;  // ignore deletes

            Item item = Item.fromMap(InternalCalls.toSimpleMapValue(newData));
            DataTransformer.PLAYER_STATS_TRANSFORMER.transform(item, dynamodb);
        }

        return true;
    }
}
