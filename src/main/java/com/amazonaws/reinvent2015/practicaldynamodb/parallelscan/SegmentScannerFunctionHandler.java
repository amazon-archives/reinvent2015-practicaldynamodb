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
package com.amazonaws.reinvent2015.practicaldynamodb.parallelscan;

import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.*;

import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.regions.Regions;
import com.amazonaws.reinvent2015.practicaldynamodb.converter.DataTransformer;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.common.util.concurrent.RateLimiter;

/**
 * For more information about using Lambda to scale for expensive workloads, see
 * "Building Scalable and Responsive Big Data Interfaces with AWS Lambda"
 * on the AWS Big Data Blog:
 * https://blogs.aws.amazon.com/bigdata/post/Tx3KH6BEUL2SGVA/
 *
 * This blog post shows a batch processing use case for searching genome data
 * in parallel, powered by AWS Lambda:
 * http://benchling.engineering/crispr-aws-lambda/?hn
 */
public class SegmentScannerFunctionHandler implements RequestHandler<SegmentScannerInput, Object> {

	private static final int REMAINING_TIME_CUTOFF = 10000;
	private static final int MAX_PAGE_SIZE = 100;
	private static final int MAX_RESULT_SIZE = 50;

    @Override
    public Object handleRequest(SegmentScannerInput input, Context context) {
        context.getLogger().log("Input: " + input.toJson() + "\n");
        context.getLogger().log("Start scanning segment " + input.getSegment() + "\n");

        DynamoDB dynamodb = new DynamoDB(Regions.US_WEST_2);

        // update tracking table in DynamoDB stating that we're in progress
		dynamodb.getTable(FUNCTION_TRACKER_TABLE_NAME).putItem(
				new Item().withPrimaryKey(SEGMENT, input.getSegment())
				.withString(STATUS, STATUS_IN_PROGRESS));

        ScanSpec scanSpec = new ScanSpec()
                .withMaxPageSize(MAX_PAGE_SIZE)
                .withSegment(input.getSegment())
                .withTotalSegments(input.getTotalSegments())
                .withConsistentRead(true)
                .withMaxResultSize(MAX_RESULT_SIZE)
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

        // if resuming an in-progress segment, specify the start key here
		if (input.getStartScore() != null) {
        	scanSpec.withExclusiveStartKey(SCORE_ID, input.getStartScore());
        }

        RateLimiter rateLimiter = RateLimiter.create(input.getMaxConsumedCapacity());

        Map<String, AttributeValue> lastEvaluatedKey = null;
        Table scoresTable = dynamodb.getTable(SCORE_TABLE_NAME);

		for (Page<Item, ScanOutcome> scanResultPage : scoresTable.scan(scanSpec).pages()) {
            // process items
            for (Item item : scanResultPage) {
                DataTransformer.HIGH_SCORES_BY_DATE_TRANSFORMER.transform(item, dynamodb);
            }

            /*
			 * After reading each page, we acquire the consumed capacity from
			 * the RateLimiter.
			 *
			 * For more information on using RateLimiter with DynamoDB scans,
			 * see "Rate Limited Scans in Amazon DynamoDB"
			 * on the AWS Java Development Blog:
			 * https://java.awsblog.com/post/Tx3VAYQIZ3Q0ZVW
			 */
            ScanResult scanResult = scanResultPage.getLowLevelResult().getScanResult();
            lastEvaluatedKey = scanResult.getLastEvaluatedKey();
            double consumedCapacity = scanResult.getConsumedCapacity().getCapacityUnits();
            rateLimiter.acquire((int)Math.round(consumedCapacity));

			// forego processing additional pages if we're running out of time
			if (context.getRemainingTimeInMillis() < REMAINING_TIME_CUTOFF) {
    			break;
    		}
        }

		if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
			Entry<String, AttributeValue> entry = lastEvaluatedKey.entrySet()
					.iterator().next();
			String lastScoreId = entry.getValue().getS();

			dynamodb.getTable(FUNCTION_TRACKER_TABLE_NAME).putItem(
					new Item()
							.withPrimaryKey(SEGMENT, input.getSegment())
							.withString(STATUS, STATUS_INCOMPLETE)
							.withString(LAST_SCORE_ID, lastScoreId));
			return false;
		}

        // update tracking table in DynamoDB stating that we're done
		dynamodb.getTable(FUNCTION_TRACKER_TABLE_NAME).putItem(
				new Item().withPrimaryKey(SEGMENT, input.getSegment())
				.withString(STATUS, STATUS_DONE));

        context.getLogger().log("Finish scanning segment " + input.getSegment() + "\n");
        return true;
    }

}
