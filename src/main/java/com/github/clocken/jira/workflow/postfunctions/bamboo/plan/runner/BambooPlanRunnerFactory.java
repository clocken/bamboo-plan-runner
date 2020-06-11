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
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.api.Plan;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webwork.action.ActionContext;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the factory class responsible for dealing with the UI for the post-function.
 * This is typically where you put default values into the velocity context and where you store user input.
 */
public class BambooPlanRunnerFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {

    public static final String FIELD_APPLINKS = "applinks";
    public static final String FIELD_SELECTED_APPLINK = "selected_applink";
    public static final String FIELD_PLANS_BY_APPLINK = "plans_by_applink";
    public static final String FIELD_SELECTED_PLAN_FOR = "selected_plan_for_";
    public static final String FIELD_FIELDS = "fields";
    public static final String FIELD_SELECTED_FIELDS_BY_VARIABLE = "selected_fields_by_variable";
    public static final String FIELD_VARIABLES_TO_USE = "variables_to_use";

    private static final Logger LOG = LoggerFactory.getLogger(BambooPlanRunnerFactory.class);

    private final ReadOnlyApplicationLinkService applicationLinkService;
    private final FieldManager fieldManager;
    private final I18nHelper i18nHelper;
    private final BambooRestApi bambooRestApi;

    private final Map<ApplicationId, List<Plan>> plansByApplink = new HashMap<>();

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

        // TODO: Order the applinks
        Iterable<ReadOnlyApplicationLink> bambooApplinks = applicationLinkService.getApplicationLinks(BambooApplicationType.class);
        velocityParams.put(FIELD_APPLINKS, bambooApplinks);

        // TODO: The ApplicationId might change... How to deal with that?
        bambooApplinks.forEach(bambooApplink -> {
            try {
                plansByApplink.put(bambooApplink.getId(), bambooRestApi.plans(bambooApplink));
                velocityParams.put(FIELD_PLANS_BY_APPLINK, plansByApplink);
            } catch (CredentialsRequiredException | ResponseException e) {
                LOG.error("Error while fetching Bamboo plans: {}", e.getMessage());
                LOG.error("Exception: ", e);
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
            LOG.error("Could not fetch fields: {}", e.getMessage());
            LOG.error("Exception: ", e);
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

        String selectedApplink = StringUtils.trimToEmpty((String) functionDescriptor.getArgs().get(FIELD_SELECTED_APPLINK));
        LOG.warn("Selected Applink is: {}", selectedApplink);
        velocityParams.put(FIELD_SELECTED_APPLINK, selectedApplink);

        String selectedPlanForApplink = StringUtils.trimToEmpty((String) functionDescriptor.getArgs().get(FIELD_SELECTED_PLAN_FOR + selectedApplink));
        LOG.warn("Selected Plan is {}", selectedPlanForApplink);
        velocityParams.put(FIELD_SELECTED_PLAN_FOR + selectedApplink, selectedPlanForApplink);

        Map<String, String> selectedFieldsByVariable = new HashMap<>();
        String selectedFieldsDescriptorParam = StringUtils.trimToEmpty((String) functionDescriptor.getArgs().get(FIELD_SELECTED_FIELDS_BY_VARIABLE));
        for (String selectedFieldByVariable :
                selectedFieldsDescriptorParam
                        .replace("{", "")
                        .replace("}", "")
                        .replace(" ", "")
                        .split(",")) {
            if (StringUtils.isNotEmpty(selectedFieldByVariable)) {
                selectedFieldsByVariable.put(selectedFieldByVariable.split("=")[0], selectedFieldByVariable.split("=")[1]);
            }
        }
        LOG.warn("Selected fields by variable {}", selectedFieldsByVariable);
        velocityParams.put(FIELD_SELECTED_FIELDS_BY_VARIABLE, selectedFieldsByVariable);

        List<String> variablesToUse = new ArrayList<>();
        String variablesToUseDescriptorParam = StringUtils.trimToEmpty((String) functionDescriptor.getArgs().get(FIELD_VARIABLES_TO_USE));
        for (String variableToUse :
                variablesToUseDescriptorParam
                        .replace("[", "")
                        .replace("]", "")
                        .replace(" ", "")
                        .split(",")) {
            if (StringUtils.isNotEmpty(variableToUse)) {
                variablesToUse.add(variableToUse);
            }
        }
        LOG.warn("Variables to use {}", variablesToUse);
        velocityParams.put(FIELD_VARIABLES_TO_USE, variablesToUse);
    }


    public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
        Map params = new HashMap();

        String selectedApplink = extractSingleParam(formParams, FIELD_SELECTED_APPLINK);
        params.put(FIELD_SELECTED_APPLINK, selectedApplink);

        String selectedPlanForApplink = extractSingleParam(formParams, FIELD_SELECTED_PLAN_FOR + selectedApplink);
        params.put(FIELD_SELECTED_PLAN_FOR + selectedApplink, selectedPlanForApplink);

        List<String> variablesToUse = new ArrayList<>();
        Map<String, String> selectedFieldsByVariable = new HashMap<>();
        plansByApplink.get(new ApplicationId(selectedApplink)).forEach(plan -> {
            if (selectedPlanForApplink.endsWith(plan.getKey())) {
                plan.getVariables().forEach(variable -> {
                    String useVariableForPlan = MessageFormat.format("use_{0}_{1}_{2}", selectedApplink, plan.getKey(), variable);
                    if (formParams.containsKey(useVariableForPlan)) {
                        String selectedFieldVorVariable = MessageFormat.format("selected_field_for_{0}_{1}_{2}", selectedApplink, plan.getKey(), variable);
                        LOG.warn("Selected field for variable {} is {}", selectedFieldVorVariable,
                                extractSingleParam(formParams, selectedFieldVorVariable));
                        variablesToUse.add(useVariableForPlan);
                        selectedFieldsByVariable.put(selectedFieldVorVariable,
                                extractSingleParam(formParams, selectedFieldVorVariable));
                    }
                });
            }
        });
        params.put(FIELD_VARIABLES_TO_USE, variablesToUse);
        params.put(FIELD_SELECTED_FIELDS_BY_VARIABLE, selectedFieldsByVariable);

        return params;
    }

}