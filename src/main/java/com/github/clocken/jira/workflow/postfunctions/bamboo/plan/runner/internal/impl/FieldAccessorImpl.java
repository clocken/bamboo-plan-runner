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

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.export.ExportableSystemField;
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
import java.text.MessageFormat;
import java.util.*;

@Named
public final class FieldAccessorImpl implements FieldAccessor {

    private static final Logger LOG = LoggerFactory.getLogger(FieldAccessorImpl.class);
    private static final List<String> FIELDS_TO_EXCLUDE;

    static {
        List<String> tmpList = new ArrayList<>();
        tmpList.add("customfield_10100");
        tmpList.add("customfield_10001");
        tmpList.add("customfield_10002");
        tmpList.add("thumbnail");
        tmpList.add("issuelinks");
        tmpList.add("progress");
        tmpList.add("customfield_10000");
        tmpList.add("customfield_10005");
        tmpList.add("customfield_10006");
        tmpList.add("subtasks");
        tmpList.add("aggregateprogress");
        FIELDS_TO_EXCLUDE = Collections.unmodifiableList(tmpList);
    }

    private final FieldManager fieldManager;
    private final I18nHelper i18nHelper;

    public FieldAccessorImpl(@ComponentImport FieldManager fieldManager,
                             @ComponentImport I18nHelper i18nHelper) {
        this.fieldManager = fieldManager;
        this.i18nHelper = i18nHelper;
    }

    @Override
    public List<Field> getAllExportableStringBasedJiraFields() throws FieldException {
        List<Field> fields = new ArrayList<>();
        fieldManager.getAllAvailableNavigableFields().forEach(navigableField -> {
            if (!navigableField.getName().startsWith("?")
                    && !FIELDS_TO_EXCLUDE.contains(navigableField.getId())) {
                fields.add(navigableField);
            }
        });
        fields.sort((field1, field2) -> StringUtils.compare(i18nHelper.getText(field1.getNameKey()),
                i18nHelper.getText(field2.getNameKey())));
        return Collections.unmodifiableList(fields);
    }

    @Override
    public Optional<String> getCustomFieldValueFromIssue(Issue issue, String identifier) {
        CustomField customField = ComponentAccessor.getCustomFieldManager()
                .getCustomFieldObject(identifier);
        if (customField == null) {
            Collection<CustomField> customFieldObjects = ComponentAccessor.getCustomFieldManager()
                    .getCustomFieldObjectsByName(identifier);
            if (customFieldObjects.isEmpty()) {
                LOG.warn("Custom field '{}' not found.", identifier);
                return Optional.empty();
            } else if (customFieldObjects.size() > 1) {
                throw new IllegalArgumentException(
                        MessageFormat.format("Ambiguous custom field name {0}. Use the custom field ID instead.", identifier));
            }
            customField = customFieldObjects
                    .iterator()
                    .next();
        }

        if (customField.getValueFromIssue(issue) == null) {
            LOG.warn("Value of custom field '{}' resolved to null. Returning empty string.", identifier);
            return Optional.of(StringUtils.EMPTY);
        }

        return Optional.of(customField.getValueFromIssue(issue));
    }

    @Override
    public Optional<String> getSystemFieldValueFromIssue(Issue issue, String identifier) {
        Field field = ComponentAccessor.getFieldManager()
                .getField(identifier);
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
                .getPartWithId(identifier)
                .ifPresent(fieldExportPart ->
                        fieldExportPart.getValues().forEach(fieldValues::add));

        return Optional.of(StringUtils.join(fieldValues, ", "));
    }
}
