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

package com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal;

import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.*;

@Named
public final class FunctionDescriptorUtilsImpl implements FunctionDescriptorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionDescriptorUtilsImpl.class);

    @Override
    public List<String> parseListFromFunctionDescriptor(FunctionDescriptor functionDescriptor, String key) {
        String descriptorParam = (String) functionDescriptor.getArgs().get(key);
        if (StringUtils.isEmpty(descriptorParam)) {
            LOG.warn("No entry for {} found in {}", key, functionDescriptor);
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(createListFromString(descriptorParam));
    }

    @Override
    public List<String> createListFromString(String list) {
        if (StringUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<String> parsedList = new ArrayList<>();
        for (String listEntry :
                list.replace("[", "")
                        .replace("]", "")
                        .replace(" ", "")
                        .split(",")) {
            if (StringUtils.isNotEmpty(listEntry)) {
                parsedList.add(listEntry);
            }
        }

        return Collections.unmodifiableList(parsedList);
    }

    @Override
    public Map<String, String> parseMapFromFunctionDescriptor(FunctionDescriptor functionDescriptor, String key) {
        String descriptorParam = (String) functionDescriptor.getArgs().get(key);
        if (StringUtils.isEmpty(descriptorParam)) {
            LOG.warn("No entry for {} found in {}", key, functionDescriptor);
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(createMapFromString(descriptorParam));
    }

    @Override
    public Map<String, String> createMapFromString(String map) {
        if (StringUtils.isEmpty(map)) {
            return Collections.emptyMap();
        }

        Map<String, String> parsedMap = new HashMap<>();
        for (String mapEntry :
                map.replace("{", "")
                        .replace("}", "")
                        .replace(" ", "")
                        .split(",")) {
            if (StringUtils.isNotEmpty(mapEntry)) {
                parsedMap.put(StringUtils.substringBefore(mapEntry, "="),
                        StringUtils.substringAfter(mapEntry, "="));
            }
        }

        return Collections.unmodifiableMap(parsedMap);
    }
}
