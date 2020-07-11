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

package ut.com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl;

import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.Base64EncodedHashMap;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.FunctionDescriptorUtils;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl.FunctionDescriptorUtilsImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class FunctionDescriptorUtilsImplTest {

    private static final Map<String, String> BASE64_ENCODED_TEST_MAP;
    private static final Map<String, String> TEST_MAP;
    private static final List<String> TEST_LIST;
    private static final String TEST_KEY_1 = "testKey1";
    private static final String TEST_VALUE_1 = "testValue1";
    private static final String TEST_KEY_2 = "testKey2";
    private static final String TEST_VALUE_2 = "testValue2";

    static {
        Map<String, String> base64EncodedTestMap = new Base64EncodedHashMap();
        base64EncodedTestMap.put(TEST_KEY_1, TEST_VALUE_1);
        base64EncodedTestMap.put(TEST_KEY_2, TEST_VALUE_2);
        BASE64_ENCODED_TEST_MAP = Collections.unmodifiableMap(base64EncodedTestMap);

        Map<String, String> testMap = new HashMap<>();
        testMap.put(TEST_KEY_1, TEST_VALUE_1);
        testMap.put(TEST_KEY_2, TEST_VALUE_2);
        TEST_MAP = Collections.unmodifiableMap(testMap);

        List<String> testList = new ArrayList<>();
        testList.add(TEST_VALUE_1);
        testList.add(TEST_VALUE_2);
        TEST_LIST = Collections.unmodifiableList(testList);
    }

    private FunctionDescriptorUtils functionDescriptorUtils;

    @Before
    public void setUp() {
        functionDescriptorUtils = new FunctionDescriptorUtilsImpl();
    }

    @Test
    public void should_create_map_from_base64_encoded_string_representation() {
        Map<String, String> decodedMapFromBase64String = functionDescriptorUtils
                .createDecodedMapFromBase64String(BASE64_ENCODED_TEST_MAP.toString());

        assertTrue(TEST_MAP.entrySet().containsAll(decodedMapFromBase64String.entrySet())
                && decodedMapFromBase64String.entrySet().containsAll(TEST_MAP.entrySet()));
    }

    @Test
    public void should_create_list_from_string_representation() {
        List<String> listFromString = functionDescriptorUtils.createListFromString(TEST_LIST.toString());

        assertTrue(TEST_LIST.containsAll(listFromString)
                && listFromString.containsAll(TEST_LIST));
    }
}
