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
package com.amazonaws.reinvent2015.practicaldynamodb.converter;

import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.DATE;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.GAME_LENGTH;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.HIGH_SCORES_BY_DATE_TABLE_NAME;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.MAX_SCORE;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.PLAYER_NAME;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.PLAYER_STATS_TABLE_NAME;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.SCORE;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.TOTAL_GAMEPLAY;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.TOTAL_GAMES;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.TOTAL_SCORE;
import static com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder.N;
import static com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder.attribute_not_exists;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.amazonaws.services.dynamodbv2.xspec.UpdateItemExpressionSpec;

public abstract class DataTransformer {

    public abstract void transform(Item newItem, DynamoDB dynamodb);
    
    /**
	 * Transforms a game score from the Scores table into stats aggregated by player in the
	 * PlayerStats table.
	 */
    public static final DataTransformer PLAYER_STATS_TRANSFORMER = new DataTransformer() {
        @Override
        public void transform(Item scoreItem, DynamoDB dynamodb) {
            String playerName = scoreItem.getString(PLAYER_NAME);
            int score = scoreItem.getInt(SCORE);
            int gameLength = scoreItem.getInt(GAME_LENGTH);
            
            /*
             * The XSpec API allows you to use DynamoDB's expression language
             * to execute expressions on the service-side.
             *  
             * https://java.awsblog.com/post/TxBG87QOQZRZJF/-DynamoDB-XSpec-API  
             */
            Table viewTable = dynamodb.getTable(PLAYER_STATS_TABLE_NAME);
            UpdateItemExpressionSpec incrementTotalOrder = new ExpressionSpecBuilder()
                    .addUpdate(N(TOTAL_SCORE).add(score))
                    .addUpdate(N(TOTAL_GAMEPLAY).add(gameLength))
                    .addUpdate(N(TOTAL_GAMES).add(1))
                    .buildForUpdate();
            viewTable.updateItem(PLAYER_NAME, playerName, incrementTotalOrder);
        }
    };
    
    /**
	 * Transforms a game score from the Scores table into stats aggregated by
	 * date in the HighScoresByDate table (hash: S("player"); range: S("date"))
	 * 
	 * For retrieving data from this table efficiently for the per-day
	 * leaderboards, we'll use a GSI that's built with 
	 * key schema: date(HASH) | highScore(RANGE).
	 */
    public static final DataTransformer HIGH_SCORES_BY_DATE_TRANSFORMER = new DataTransformer() {
        @Override
        public void transform(Item scoreItem, DynamoDB dynamodb) {
            String playerName = scoreItem.getString(PLAYER_NAME);
            int score         = scoreItem.getInt(SCORE);
            String date       = scoreItem.getString(DATE);
            
            Table table = dynamodb.getTable(HIGH_SCORES_BY_DATE_TABLE_NAME);
            
            // Use conditional write to update max score
            UpdateItemExpressionSpec updateMax = new ExpressionSpecBuilder()
                    .withCondition(N(MAX_SCORE).lt(score)
                            .or(attribute_not_exists(MAX_SCORE)))
                    .addUpdate(N(MAX_SCORE).set(score))
                    .buildForUpdate();
            try {
                table.updateItem(PLAYER_NAME, playerName, DATE, date, updateMax);
            } catch (ConditionalCheckFailedException ccfe) {}
        }
    };
}
