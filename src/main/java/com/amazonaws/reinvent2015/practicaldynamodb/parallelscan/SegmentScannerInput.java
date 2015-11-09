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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SegmentScannerInput {
    private int totalSegments;
    private int segment;
    private long maxConsumedCapacity;
    private String startScore;

    public int getTotalSegments() {
        return totalSegments;
    }
    public void setTotalSegments(int totalSegments) {
        this.totalSegments = totalSegments;
    }
    public int getSegment() {
        return segment;
    }
    public void setSegment(int segment) {
        this.segment = segment;
    }
    public long getMaxConsumedCapacity() {
        return maxConsumedCapacity;
    }
    public void setMaxConsumedCapacity(long maxConsumedCapacity) {
        this.maxConsumedCapacity = maxConsumedCapacity;
    }
    public String getStartScore() {
    	return startScore;
    }
    public void setStartScore(String startScore) {
    	this.startScore = startScore;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
