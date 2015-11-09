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

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;

import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.*;

public class FunctionInvoker {

    private static AWSLambdaClient lambda;
    private static DynamoDB dynamodb;

    static {
        lambda = new AWSLambdaClient(new ProfileCredentialsProvider(LOCAL_CRED_PROFILE_NAME));
        lambda.configureRegion(Regions.US_WEST_2);
        dynamodb = new DynamoDB(Regions.US_WEST_2);
    }

    public static void main(String[] args) {
        int totalSegments = 5;
        long maxConsumedCapacityPerSegment = 1;

		FunctionInvoker invoker = new FunctionInvoker(
				totalSegments, maxConsumedCapacityPerSegment);
        for (int i = 0; i < invoker.totalSegments; i++) {
			invoker.functionTrackerTable.deleteItem(SEGMENT, i);
			invoker.invokeScannerOnSegment(i, null);
		}
		invoker.monitorScanners();
    }

    private final int totalSegments;
    private final long maxConsumedCapacityPerSegment;
	private final Table functionTrackerTable = dynamodb
			.getTable(FUNCTION_TRACKER_TABLE_NAME);

    FunctionInvoker(int totalSegments, long maxConsumedCapacityPerSegment) {
        this.totalSegments = totalSegments;
        this.maxConsumedCapacityPerSegment = maxConsumedCapacityPerSegment;
    }

	/*
	 * Monitors the progress of active scanners.
	 */
	private void monitorScanners() {
		boolean incomplete = true;

		while (incomplete) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}

			incomplete = false;
			for (int i = 0; i < totalSegments; i++) {
				Item item = functionTrackerTable.getItem(SEGMENT, i);
				if (item != null) {
					String status = item.getString(STATUS);
					if (!status.equals(STATUS_DONE)) {
						incomplete = true;
					}
					if (status.equals(STATUS_INCOMPLETE)) {
						String startScoreId = item.getString(LAST_SCORE_ID);
						invokeScannerOnSegment(i, startScoreId);
					}
				} else {
					incomplete = true;
					break;
				}
			}
		}
	}

    /*
     * For more information about invoking Lambda functions from Java
     * applications using the LambdaInvokerFactory in the SDK, see
     * "Invoking AWS Lambda Functions from Java"
     * on the AWS Java Development Blog:
     * https://java.awsblog.com/post/Tx2J2LPKTTVU93H
     */
	private void invokeScannerOnSegment(final int segment, final String startScore) {
		new Thread() {
			@Override
			public void run() {
				SegmentScanner scanner = LambdaInvokerFactory.build(
						SegmentScanner.class, lambda);

				SegmentScannerInput input = new SegmentScannerInput();
				input.setSegment(segment);
				input.setTotalSegments(totalSegments);
				input.setMaxConsumedCapacity(maxConsumedCapacityPerSegment);
				input.setStartScore(startScore);

				scanner.scanSegment(input);
			}
		}.start();
	}
}
