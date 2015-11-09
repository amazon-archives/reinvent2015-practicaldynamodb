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

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public class Constants {

    public static final String LOCAL_CRED_PROFILE_NAME = "default";

    // Source table
    public static final String SCORE_TABLE_NAME = "Scores";
    public static final String SCORE_ID = "scoreId";
    public static final String PLAYER_NAME = "playerName";
    public static final String SCORE = "score";
    public static final String GAME_LENGTH = "gameLength";
    public static final String TOTAL_SCORE = "totalScore";
    public static final String DATE = "date";

    // PlayerStats table
    public static final String PLAYER_STATS_TABLE_NAME = "PlayerStats";
    public static final String TOTAL_GAMEPLAY = "totalGameplay";
    public static final String TOTAL_GAMES = "totalGames";
    public static final String MAX_SCORE = "maxScore";

    // Second Materialized View
    public static final String HIGH_SCORES_BY_DATE_TABLE_NAME = "HighScoresByDate";

    // Lambda function tracking table
    public static final String FUNCTION_TRACKER_TABLE_NAME = "FunctionTracker";
    public static final String SEGMENT = "segment";
    public static final String STATUS = "status";
    public static final String LAST_SCORE_ID = "lastScoreId";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_INCOMPLETE = "INCOMPLETE";
    public static final String STATUS_DONE = "DONE";

    public static final AmazonDynamoDB dynamodb;
	public static final DynamoDB documentAPI;

    static {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(
                new ProfileCredentialsProvider(LOCAL_CRED_PROFILE_NAME));
        client.configureRegion(Regions.US_WEST_2);
        dynamodb = client;

        documentAPI = new DynamoDB(dynamodb);
    }

}
