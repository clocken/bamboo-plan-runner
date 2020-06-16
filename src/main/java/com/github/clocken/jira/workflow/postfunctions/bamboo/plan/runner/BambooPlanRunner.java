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

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.ReadOnlyApplicationLink;
import com.atlassian.applinks.api.ReadOnlyApplicationLinkService;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.ResponseException;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.FunctionDescriptorUtils;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.BambooRestApi;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the post-function class that gets executed at the end of the transition.
 * Any parameters that were saved in your factory class will be available in the transientVars Map.
 */
public class BambooPlanRunner extends AbstractJiraFunctionProvider {

    public static final String FIELD_SELECTED_APPLINK = "selected_applink";
    public static final String FIELD_SELECTED_PLAN_FOR = "selected_plan_for_";
    public static final String FIELD_SELECTED_VALUES_BY_VARIABLE = "selected_values_by_variable";

    private static final Logger LOG = LoggerFactory.getLogger(BambooPlanRunner.class);

    private final ReadOnlyApplicationLinkService applicationLinkService;
    private final FieldManager fieldManager;
    private final BambooRestApi bambooRestApi;
    private final FunctionDescriptorUtils functionDescriptorUtils;

    @Inject
    public BambooPlanRunner(@ComponentImport ReadOnlyApplicationLinkService applicationLinkService,
                            @ComponentImport FieldManager fieldManager,
                            BambooRestApi bambooRestApi,
                            FunctionDescriptorUtils functionDescriptorUtils) {
        this.applicationLinkService = applicationLinkService;
        this.fieldManager = fieldManager;
        this.bambooRestApi = bambooRestApi;
        this.functionDescriptorUtils = functionDescriptorUtils;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        String selectedApplinkId = (String) args.get(FIELD_SELECTED_APPLINK);
        if (StringUtils.isEmpty(selectedApplinkId)) {
            LOG.error("No Application link selected. Not running any plan!");
            return;
        }

        String selectedPlan = StringUtils.remove(
                (String) args.get(FIELD_SELECTED_PLAN_FOR + selectedApplinkId),
                selectedApplinkId + "_");
        if (StringUtils.isEmpty(selectedPlan)) {
            LOG.error("No plan selected for the Application Link {}. Not running any plan!", selectedApplinkId);
            return;
        }

        final Map<String, String> selectedValuesByVariable = new HashMap<>();
        functionDescriptorUtils.createMapFromString((String) args.get(FIELD_SELECTED_VALUES_BY_VARIABLE))
                .forEach((variable, value) -> {
                    selectedValuesByVariable.put(RegExUtils.removeFirst(variable,
                            MessageFormat.format(".*for_{0}_{1}_", selectedApplinkId, selectedPlan)),
                            value);
                });

        ReadOnlyApplicationLink selectedApplink = applicationLinkService.getApplicationLink(new ApplicationId(selectedApplinkId));
        if (selectedApplink == null) {
            LOG.error("No Application link found for ID {}. Not running any plan!", selectedApplinkId);
            return;
        }

        try {
            bambooRestApi.queueBuild(selectedApplink, selectedPlan, selectedValuesByVariable);
        } catch (CredentialsRequiredException | ResponseException e) {
            // TODO: implement user feedback for this
            LOG.error("Error running plan {}: {}", selectedPlan, e.getMessage());
            LOG.error("Exception: ", e);
        }
    }
}