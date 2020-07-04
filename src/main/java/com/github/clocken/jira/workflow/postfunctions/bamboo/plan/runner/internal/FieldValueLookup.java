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

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.export.ExportableSystemField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This class finds JIRA field values from an issue by <i>System field ID</i>, <i>Custom field ID</i> or
 * <i>Custom field name</i>. Ambiguous custom field names get rejected.
 */
public class FieldValueLookup extends StrLookup {

    private static final Logger LOG = LoggerFactory.getLogger(FieldValueLookup.class);

    private final I18nHelper i18nHelper;
    private final Issue issue;

    /**
     * Constructs a new {@link FieldValueLookup} instance.
     *
     * @param i18nHelper The I18n helper service for localization of the error message for unresolvable fields
     * @param issue      The issue to lookup field values from
     */
    public FieldValueLookup(I18nHelper i18nHelper,
                            Issue issue) {
        this.i18nHelper = i18nHelper;
        this.issue = issue;
    }

    /**
     * Looks up a <i>System field ID</i>, <i>Custom field ID</i> or <i>Custom field name</i> to the corresponding value
     * in the issue given at construction time.
     *
     * @param key The System field ID, Custom field ID or Custom field name to lookup
     * @return The value of the field in the issue, if found, an empty string if the value is <code>null</code> or a
     * localized error text, if the field is not found
     * @throws IllegalArgumentException if <code>key</code> is an ambiguous custom field name
     */
    @Override
    public String lookup(String key) {
        return customFieldValue(key)
                .orElseGet(() -> {
                    LOG.warn("Custom field '{}' not resolved. Trying as system field.", key);
                    return systemFieldValue(key)
                            .orElseGet(() -> {
                                LOG.error("Field '{}' is not resolvable.", key);
                                return i18nHelper.getText("bamboo-plan-runner.postfunction.field.value.unresolved");
                            });
                });
    }

    private Optional<String> customFieldValue(String key) {
        CustomField customField = ComponentAccessor.getCustomFieldManager()
                .getCustomFieldObject(key);
        if (customField == null) {
            Collection<CustomField> customFieldObjects = ComponentAccessor.getCustomFieldManager()
                    .getCustomFieldObjectsByName(key);
            if (customFieldObjects.isEmpty()) {
                LOG.warn("Custom field '{}' not found.", key);
                return Optional.empty();
            } else if (customFieldObjects.size() > 1) {
                throw new IllegalArgumentException(
                        MessageFormat.format("Ambiguous custom field name {0}. Use the custom field ID instead.", key));
            }
            customField = customFieldObjects
                    .iterator()
                    .next();
        }

        if (customField.getValueFromIssue(issue) == null) {
            LOG.warn("Value of custom field '{}' resolved to null. Returning empty string.", key);
            return Optional.of(StringUtils.EMPTY);
        }

        return Optional.of(customField.getValueFromIssue(issue));
    }

    private Optional<String> systemFieldValue(String key) {
        Field field = ComponentAccessor.getFieldManager()
                .getField(key);
        if (field == null) {
            LOG.warn("System field '{}' not found.", key);
            return Optional.empty();
        }
        if (!(field instanceof ExportableSystemField)) {
            LOG.warn("Value of system field '{}' is not exportable.", key);
            return Optional.empty();
        }

        final List<String> fieldValues = new ArrayList<>();
        ((ExportableSystemField) field)
                .getRepresentationFromIssue(issue)
                .getPartWithId(key)
                .ifPresent(fieldExportPart ->
                        fieldExportPart.getValues().forEach(fieldValues::add));

        return Optional.of(StringUtils.join(fieldValues, ", "));
    }
}
