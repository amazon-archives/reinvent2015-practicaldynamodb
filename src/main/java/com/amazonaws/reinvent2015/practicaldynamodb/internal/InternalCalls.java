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

package com.amazonaws.reinvent2015.practicaldynamodb.internal;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class InternalCalls {

	/**
	 * This method is calling an internal API which could be changed over time.
	 * It is only for use of this demo and not expected for external use.
	 */
	public static Map<String, Object> toSimpleMapValue(
			Map<String, AttributeValue> values) {
		return InternalUtils.toSimpleMapValue(values);
	}

}
