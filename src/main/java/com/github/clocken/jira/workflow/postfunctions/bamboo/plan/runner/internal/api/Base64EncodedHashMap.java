/*
 * Copyright 2020 clocken
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api;

import java.util.Base64;
import java.util.HashMap;

public class Base64EncodedHashMap extends HashMap<String, String> {

    @Override
    public String put(String key, String value) {
        return super.put(
                Base64.getEncoder()
                        .withoutPadding()
                        .encodeToString(key.getBytes()),
                Base64.getEncoder()
                        .withoutPadding()
                        .encodeToString(value.getBytes()));
    }
}
