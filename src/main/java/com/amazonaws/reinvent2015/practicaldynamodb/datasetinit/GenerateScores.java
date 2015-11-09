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

import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.DATE;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.GAME_LENGTH;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.PLAYER_NAME;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.SCORE;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.SCORE_ID;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.SCORE_TABLE_NAME;
import static com.amazonaws.reinvent2015.practicaldynamodb.datasetinit.Constants.documentAPI;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

public class GenerateScores {

	private static Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        while(true) {
            putNewScore();
            Thread.sleep(1_000);
        }
    }

    /*
     * For more information on using the Document API to access DynamoDB,
     * see "Introducing DynamoDB Document API Part 1"
     * on the AWS Java Development Blog:
     * https://java.awsblog.com/post/Tx1DDAWQGXWITSG
     */
    private static void putNewScore() {
    	Table table = documentAPI.getTable(SCORE_TABLE_NAME);

    	Item item = new Item()
    		.withString(SCORE_ID, UUID.randomUUID().toString())
    		.withString(PLAYER_NAME, randomPlayerName())
    		.withString(DATE, randomDate())
    		.withInt(SCORE, randomScore())
    		.withInt(GAME_LENGTH, randomGameLength());
		table.putItem(item);

        System.out.println("Added score " + item);
    }

    private static final String[] PLAYER_NAMES = new String[]
    		{"Jason F", "Shuo H", "Hanson C", "Mani S", "Andrew S", "Jonathan B", "Jessie Z"};

    private static String randomPlayerName() {
        int i = random.nextInt(PLAYER_NAMES.length);
        return PLAYER_NAMES[i];
    }

    private static String randomDate() {
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTime(new Date());

    	return calendar.get(Calendar.YEAR) + "/"
    		+ (calendar.get(Calendar.MONTH) + 1) + "/"
    		+ (random.nextInt(28) + 1);
    }

    private static int randomScore() {
        return random.nextInt(1000) + 1;
    }

    private static int randomGameLength() {
        return random.nextInt(5000) + 1;
    }
}
