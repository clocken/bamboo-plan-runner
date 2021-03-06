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

import com.opensymphony.workflow.loader.FunctionDescriptor;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Utilities for the {@link FunctionDescriptor}.<br>
 * These utilities may be used to work around the fact that the contents of the {@link FunctionDescriptor} somehow get
 * saved as string values only by the OpenSymphony Workflow engine.
 */
public interface FunctionDescriptorUtils {
    /**
     * Reads a {@link List} from the given {@link FunctionDescriptor}'s key
     *
     * @param functionDescriptor the {@link FunctionDescriptor} to read from
     * @param key                the key in the {@link FunctionDescriptor}, that contains the list as a string representation
     * @return the (unmodifiable) {@link List}
     */
    List<String> parseListFromFunctionDescriptor(FunctionDescriptor functionDescriptor, String key);

    /**
     * Creates a {@link List} from the given string. The string should look like the result from {@link List#toString()}
     *
     * @param list the string to create the list from
     * @return the (unmodifiable) {@link List}
     */
    List<String> createListFromString(String list);

    /**
     * Reads a {@link Map} from the given {@link FunctionDescriptor}'s key
     *
     * @param functionDescriptor the {@link FunctionDescriptor} to read from
     * @param key                the key in the {@link FunctionDescriptor}, that contains the list as a string representation
     * @return the (unmodifiable) {@link Map}
     */
    Map<String, String> parseMapFromFunctionDescriptor(FunctionDescriptor functionDescriptor, String key);

    /**
     * Creates a {@link Map} from the given string.<br>
     * The string should look like the result from {@link Map#toString()} and the keys and values should be
     * Base64 encoded without padding according to the standard encoding by {@link Base64.Encoder#withoutPadding()}.
     *
     * @param map the string to create the map from. Keys and values should be Base64 encoded.
     * @return the (unmodifiable) {@link Map}. Keys and values are Base64 decoded.
     */
    Map<String, String> createDecodedMapFromBase64String(String map);
}
