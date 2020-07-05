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
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.ResponseException;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.Base64EncodedHashMap;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.FieldAccessor;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.FunctionDescriptorUtils;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.bamboo.BambooRestApi;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.bamboo.Plan;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The factory class responsible for dealing with the UI of the Bamboo Plan Runner.
 */
public class BambooPlanRunnerFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {

    public static final String FIELD_APPLINKS = "applinks";
    public static final String FIELD_SELECTED_APPLINK = "selected_applink";
    public static final String FIELD_PLANS_BY_APPLINK = "plans_by_applink";
    public static final String FIELD_SELECTED_PLAN_FOR = "selected_plan_for_";
    public static final String FIELD_FIELDS = "fields";
    public static final String FIELD_SELECTED_VALUE_TYPES_BY_VARIABLE = "selected_value_types_by_variable";
    public static final String FIELD_SELECTED_VALUES_BY_VARIABLE = "selected_values_by_variable";
    public static final String FIELD_VARIABLES_TO_USE = "variables_to_use";

    private static final Logger LOG = LoggerFactory.getLogger(BambooPlanRunnerFactory.class);
    private static final Pattern KEY_PREFIX_PATTERN = Pattern.compile("(.*)_for.*");
    private static final Map<String, String> VARIABLE_VALUE_KEY_PREFIX;


    static {
        Map<String, String> tmpMap = new HashMap<>();
        tmpMap.put("use_field", "selected_field_for");
        tmpMap.put("use_custom_value", "custom_value_for");
        VARIABLE_VALUE_KEY_PREFIX = Collections.unmodifiableMap(tmpMap);
    }

    private final ReadOnlyApplicationLinkService applicationLinkService;
    private final FieldManager fieldManager;
    private final I18nHelper i18nHelper;
    private final BambooRestApi bambooRestApi;
    private final FunctionDescriptorUtils functionDescriptorUtils;
    private final FieldAccessor fieldAccessor;
    private final Map<ApplicationId, List<Plan>> plansByApplink = new HashMap<>();

    @Inject
    public BambooPlanRunnerFactory(@ComponentImport ReadOnlyApplicationLinkService applicationLinkService,
                                   @ComponentImport FieldManager fieldManager,
                                   @ComponentImport I18nHelper i18nHelper,
                                   BambooRestApi bambooRestApi,
                                   FunctionDescriptorUtils functionDescriptorUtils,
                                   FieldAccessor fieldAccessor) {
        this.applicationLinkService = applicationLinkService;
        this.fieldManager = fieldManager;
        this.i18nHelper = i18nHelper;
        this.bambooRestApi = bambooRestApi;
        this.functionDescriptorUtils = functionDescriptorUtils;
        this.fieldAccessor = fieldAccessor;
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        // TODO: Order the applinks
        // TODO: The ApplicationId might change... When does that happen and how to deal with that?
        Iterable<ReadOnlyApplicationLink> bambooApplinks = applicationLinkService.getApplicationLinks(BambooApplicationType.class);
        velocityParams.put(FIELD_APPLINKS, bambooApplinks);

        bambooApplinks.forEach(bambooApplink -> {
            try {
                plansByApplink.put(bambooApplink.getId(), bambooRestApi.plans(bambooApplink));
                velocityParams.put(FIELD_PLANS_BY_APPLINK, plansByApplink);
            } catch (CredentialsRequiredException | ResponseException e) {
                // TODO: implement user feedback for this
                LOG.error("Error while fetching Bamboo plans: {}", e.getMessage());
                LOG.error("Exception: ", e);
            }
        });

        try {
            velocityParams.put(FIELD_FIELDS, fieldAccessor.getAllExportableStringBasedJiraFields());
        } catch (FieldException e) {
            // TODO: implement user feedback for this
            LOG.error("Error while fetching JIRA fields: {}", e.getMessage());
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
        velocityParams.put(FIELD_SELECTED_APPLINK, selectedApplink);
        LOG.debug("Selected Applink is: {}", selectedApplink);

        String selectedPlanForApplink = StringUtils.trimToEmpty((String) functionDescriptor.getArgs().get(FIELD_SELECTED_PLAN_FOR + selectedApplink));
        velocityParams.put(FIELD_SELECTED_PLAN_FOR + selectedApplink, selectedPlanForApplink);
        LOG.debug("Selected Plan is {}", selectedPlanForApplink);

        List<String> variablesToUse = functionDescriptorUtils.parseListFromFunctionDescriptor(functionDescriptor, FIELD_VARIABLES_TO_USE);
        velocityParams.put(FIELD_VARIABLES_TO_USE, variablesToUse);
        LOG.debug("Variables to use {}", variablesToUse);

        Map<String, String> selectedValueTypesByVariable = functionDescriptorUtils.parseMapFromFunctionDescriptor(functionDescriptor, FIELD_SELECTED_VALUE_TYPES_BY_VARIABLE);
        velocityParams.put(FIELD_SELECTED_VALUE_TYPES_BY_VARIABLE, selectedValueTypesByVariable);
        LOG.debug("Selected types for variables {}", selectedValueTypesByVariable);

        Map<String, String> selectedValuesByVariable = functionDescriptorUtils.parseMapFromFunctionDescriptor(functionDescriptor, FIELD_SELECTED_VALUES_BY_VARIABLE);
        velocityParams.put(FIELD_SELECTED_VALUES_BY_VARIABLE, selectedValuesByVariable);
        LOG.debug("Selected values for variables {}", selectedValuesByVariable);
    }

    public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
        Map<String, Object> params = new HashMap<>();

        String selectedApplink = extractSingleParam(formParams, FIELD_SELECTED_APPLINK);
        params.put(FIELD_SELECTED_APPLINK, selectedApplink);

        String selectedPlanForApplink = extractSingleParam(formParams, FIELD_SELECTED_PLAN_FOR + selectedApplink);
        params.put(FIELD_SELECTED_PLAN_FOR + selectedApplink, selectedPlanForApplink);

        List<String> variablesToUse = new ArrayList<>();
        Map<String, String> selectedValueTypesByVariable = new Base64EncodedHashMap();
        Map<String, String> selectedValuesByVariable = new Base64EncodedHashMap();
        plansByApplink.get(new ApplicationId(selectedApplink)).forEach(plan -> {
            if (StringUtils.endsWith(selectedPlanForApplink, plan.getKey())) {

                plan.getVariables().forEach(variable -> {
                    String useVariableForPlanKey =
                            MessageFormat.format("use_{0}_{1}_{2}", selectedApplink, plan.getKey(), variable);

                    if (formParams.containsKey(useVariableForPlanKey)) {
                        variablesToUse.add(useVariableForPlanKey);

                        String variableValueTypeKey =
                                MessageFormat.format("variable_value_type_for_{0}_{1}_{2}", selectedApplink, plan.getKey(), variable);
                        selectedValueTypesByVariable.put(variableValueTypeKey,
                                extractSingleParam(formParams, variableValueTypeKey));

                        String selectedValueForVariableKey =
                                getSelectedValueForVariableKey(extractSingleParam(formParams, variableValueTypeKey), selectedApplink, plan.getKey(), variable);
                        if (StringUtils.isNotEmpty(selectedValueForVariableKey)) {
                            selectedValuesByVariable.put(selectedValueForVariableKey,
                                    extractSingleParam(formParams, selectedValueForVariableKey));
                        }

                        LOG.debug("Using variable {} with type {} and value {}", useVariableForPlanKey,
                                extractSingleParam(formParams, variableValueTypeKey),
                                extractSingleParam(formParams, selectedValueForVariableKey));
                    }
                });
            }
        });
        params.put(FIELD_VARIABLES_TO_USE, variablesToUse);
        params.put(FIELD_SELECTED_VALUE_TYPES_BY_VARIABLE, selectedValueTypesByVariable);
        params.put(FIELD_SELECTED_VALUES_BY_VARIABLE, selectedValuesByVariable);

        return params;
    }

    private String getSelectedValueForVariableKey(String selectedValueType, String selectedApplink, String planKey, String variable) {
        Matcher keyPrefixMatcher = KEY_PREFIX_PATTERN.matcher(selectedValueType);
        if (keyPrefixMatcher.matches()) {
            return MessageFormat.format("{0}_{1}_{2}_{3}", VARIABLE_VALUE_KEY_PREFIX.get(keyPrefixMatcher.group(1)), selectedApplink, planKey, variable);
        }
        return StringUtils.EMPTY;
    }
}
