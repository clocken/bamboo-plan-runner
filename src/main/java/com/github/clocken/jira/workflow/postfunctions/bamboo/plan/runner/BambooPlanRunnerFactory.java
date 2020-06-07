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

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.ReadOnlyApplicationLink;
import com.atlassian.applinks.api.ReadOnlyApplicationLinkService;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.ResponseException;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.api.BambooRestApi;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webwork.action.ActionContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the factory class responsible for dealing with the UI for the post-function.
 * This is typically where you put default values into the velocity context and where you store user input.
 */
public class BambooPlanRunnerFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {

    public static final String FIELD_APP_LINKS = "appLinks";
    public static final String FIELD_SELECTED_APPLINK = "selectedAppLink";
    public static final String FIELD_PLANS = "plans";
    public static final String FIELD_SELECTED_PLAN = "selectedPlan";
    public static final String FIELD_FIELDS = "fields";

    private static final Logger LOG = LoggerFactory.getLogger(BambooPlanRunnerFactory.class);

    private final ReadOnlyApplicationLinkService applicationLinkService;
    private final FieldManager fieldManager;
    private final I18nHelper i18nHelper;
    private final BambooRestApi bambooRestApi;

    @Inject
    public BambooPlanRunnerFactory(@ComponentImport ReadOnlyApplicationLinkService applicationLinkService,
                                   @ComponentImport FieldManager fieldManager,
                                   @ComponentImport I18nHelper i18nHelper,
                                   BambooRestApi bambooRestApi) {
        this.applicationLinkService = applicationLinkService;
        this.fieldManager = fieldManager;
        this.i18nHelper = i18nHelper;
        this.bambooRestApi = bambooRestApi;
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        Map<String, String[]> myParams = ActionContext.getParameters();

        Iterable<ReadOnlyApplicationLink> bambooAppLinks = applicationLinkService.getApplicationLinks(BambooApplicationType.class);
        velocityParams.put(FIELD_APP_LINKS, bambooAppLinks);

        bambooAppLinks.forEach(bambooAppLink -> {
            try {
                velocityParams.put(FIELD_PLANS, bambooRestApi.plans(bambooAppLink));
            } catch (CredentialsRequiredException | ResponseException ex) {
                LOG.error("Error while fetching Bamboo plans: {}", ex.getMessage());
                LOG.error("Exception: ", ex);
            }
        });

        try {
            List<Field> fields = new ArrayList<>();
            fieldManager.getAllAvailableNavigableFields().forEach(navigableField -> {
                if (!navigableField.getName().startsWith("?")) {
                    fields.add(navigableField);
                }
            });
            fields.sort((field1, field2) -> StringUtils.compare(i18nHelper.getText(field1.getNameKey()), i18nHelper.getText(field2.getNameKey())));

            velocityParams.put(FIELD_FIELDS, fields);
        } catch (FieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsForInput(velocityParams);
        getVelocityParamsForView(velocityParams, descriptor);
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        if (!(descriptor instanceof FunctionDescriptor)) {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }

        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        String selectedAppLink = (String) functionDescriptor.getArgs().get(FIELD_SELECTED_APPLINK);
        LOG.warn("Selected AppLink is: {}", selectedAppLink);
        velocityParams.put(FIELD_SELECTED_APPLINK, selectedAppLink);

        String selectedPlan = (String) functionDescriptor.getArgs().get(FIELD_SELECTED_PLAN);
        LOG.warn("Selected Plan is {}", selectedPlan);
        velocityParams.put(FIELD_SELECTED_PLAN, selectedPlan);
    }


    public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
        Map params = new HashMap();

        String selectedAppLink = extractSingleParam(formParams, FIELD_SELECTED_APPLINK);
        params.put(FIELD_SELECTED_APPLINK, selectedAppLink);

        String selectedPlan = extractSingleParam(formParams, FIELD_SELECTED_PLAN);
        params.put(FIELD_SELECTED_PLAN, selectedPlan);

        return params;
    }

}