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

package com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.export.ExportableSystemField;
import com.atlassian.jira.issue.export.customfield.ExportableCustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.FieldAccessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public final class FieldAccessorImpl implements FieldAccessor {

    private static final Logger LOG = LoggerFactory.getLogger(FieldAccessorImpl.class);
    private static final Map<String, String> VALUE_REPRESENTATION_IDS_BY_FIELD_ID;

    static {
        Map<String, String> valueRepresentationIdsByFieldId = new HashMap<>();
        valueRepresentationIdsByFieldId.put("issuekey", "key");
        valueRepresentationIdsByFieldId.put("project", "projectName");
        VALUE_REPRESENTATION_IDS_BY_FIELD_ID = Collections.unmodifiableMap(valueRepresentationIdsByFieldId);
    }

    private final FieldManager fieldManager;
    private final CustomFieldManager customFieldManager;
    private final I18nHelper i18nHelper;

    public FieldAccessorImpl(@ComponentImport FieldManager fieldManager,
                             @ComponentImport CustomFieldManager customFieldManager,
                             @ComponentImport I18nHelper i18nHelper) {
        this.fieldManager = fieldManager;
        this.customFieldManager = customFieldManager;
        this.i18nHelper = i18nHelper;
    }

    @Override
    public List<Field> getAllExportableJiraFields() throws FieldException {
        List<Field> fields = fieldManager.getAllAvailableNavigableFields().stream()
                .filter(
                        navigableField ->
                                !navigableField.getName().startsWith("?")
                                        && (navigableField instanceof ExportableSystemField
                                        || (navigableField instanceof CustomField && ((CustomField) navigableField).getCustomFieldType() instanceof ExportableCustomFieldType))
                )
                .sorted((field1, field2) ->
                        StringUtils.compare(i18nHelper.getText(field1.getNameKey()), i18nHelper.getText(field2.getNameKey())))
                .collect(Collectors.toList());

        return Collections.unmodifiableList(fields);
    }

    @Override
    public Optional<String> getCustomFieldValueFromIssue(Issue issue, String identifier) {
        CustomField customField = customFieldManager.getCustomFieldObject(identifier);
        if (customField == null) {
            Collection<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjectsByName(identifier);
            if (customFieldObjects.isEmpty()) {
                LOG.warn("Custom field '{}' not found.", identifier);
                return Optional.empty();
            } else if (customFieldObjects.size() > 1) {
                LOG.error("Ambiguous custom field name {}. Use the custom field ID instead.", identifier);
                return Optional.empty();
            }
            customField = customFieldObjects
                    .iterator()
                    .next();
        }
        if (!(customField.getCustomFieldType() instanceof ExportableCustomFieldType)) {
            LOG.warn("Value of custom field '{}' is not exportable.", identifier);
            return Optional.empty();
        }

        final List<String> fieldValues = new ArrayList<>();
        ((ExportableCustomFieldType) customField.getCustomFieldType())
                .getRepresentationFromIssue(issue, new CustomFieldExportContext(customField, i18nHelper))
                .getParts()
                .stream()
                .filter(
                        fieldExportPart ->
                                StringUtils.equals(fieldExportPart.getId(), VALUE_REPRESENTATION_IDS_BY_FIELD_ID.getOrDefault(identifier, identifier))
                                        || StringUtils.equals(fieldExportPart.getItemLabel(), identifier)
                )
                .findAny()
                .ifPresent(fieldExportPart ->
                        fieldExportPart.getValues().forEach(fieldValues::add));

        return Optional.of(StringUtils.join(fieldValues, ", "));
    }

    @Override
    public Optional<String> getSystemFieldValueFromIssue(Issue issue, String identifier) {
        Field field = fieldManager.getField(identifier);
        if (field == null) {
            LOG.warn("System field '{}' not found.", identifier);
            return Optional.empty();
        }
        if (!(field instanceof ExportableSystemField)) {
            LOG.warn("Value of system field '{}' is not exportable.", identifier);
            return Optional.empty();
        }

        final List<String> fieldValues = new ArrayList<>();
        ((ExportableSystemField) field)
                .getRepresentationFromIssue(issue)
                .getPartWithId(VALUE_REPRESENTATION_IDS_BY_FIELD_ID.getOrDefault(identifier, identifier))
                .ifPresent(fieldExportPart ->
                        fieldExportPart.getValues().forEach(fieldValues::add));

        return Optional.of(StringUtils.join(fieldValues, ", "));
    }

    private static final class CustomFieldExportContext implements com.atlassian.jira.issue.export.customfield.CustomFieldExportContext {

        private final CustomField customField;
        private final I18nHelper i18nHelper;

        public CustomFieldExportContext(CustomField customField, I18nHelper i18nHelper) {
            this.customField = customField;
            this.i18nHelper = i18nHelper;
        }

        @Override
        public CustomField getCustomField() {
            return customField;
        }

        @Override
        public I18nHelper getI18nHelper() {
            return i18nHelper;
        }

        @Override
        public String getDefaultColumnHeader() {
            return i18nHelper.getText(customField.getColumnHeadingKey(), customField.getName());
        }
    }
}
