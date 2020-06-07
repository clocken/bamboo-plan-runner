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

package com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This is the post-function class that gets executed at the end of the transition.
 * Any parameters that were saved in your factory class will be available in the transientVars Map.
 */
public class BambooPlanRunner extends AbstractJiraFunctionProvider {

    public static final String FIELD_SELECTED_APPLINK = "selectedAppLink";

    private static final Logger LOG = LoggerFactory.getLogger(BambooPlanRunner.class);

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);
        String selectedAppLink = (String) args.get(FIELD_SELECTED_APPLINK);

        if (null == selectedAppLink) {
            selectedAppLink = "";
        }

        LOG.debug("Appending '{}' to the Description of issue {}", selectedAppLink, issue.getKey());
        issue.setDescription(issue.getDescription() + selectedAppLink);
    }
}