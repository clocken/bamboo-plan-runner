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

import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Bamboo build plan. Use {@link Plan.Builder} to create a new instance of this class.
 */
public final class Plan {

    private String key;
    private String name;
    private String description = StringUtils.EMPTY;
    private URL link;
    private boolean enabled;
    private List<String> variables;

    private Plan() {
        // only instantiable via the Builder
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public URL getLink() {
        return link;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getVariables() {
        return variables;
    }

    /**
     * The Builder for plans
     */
    public static final class Builder {

        private final Plan newPlan = new Plan();

        private Builder() {
            // no direct instantiation
        }

        public static Builder aPlan() {
            return new Builder();
        }

        public Builder withKey(String key) {
            newPlan.key = key;
            return this;
        }

        public Builder withName(String name) {
            newPlan.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            newPlan.description = description;
            return this;
        }

        public Builder withLink(URL link) {
            newPlan.link = link;
            return this;
        }

        public Builder thatIsEnabled(boolean enabled) {
            newPlan.enabled = enabled;
            return this;
        }

        public Builder withVariables(List<String> variables) {
            newPlan.variables = Collections.unmodifiableList(variables);
            return this;
        }

        public Plan build() {
            // TODO: Check for mandatory parameters
            return newPlan;
        }
    }
}
