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

import java.util.ArrayList;
import java.util.List;

public class FieldStrLookup extends StrLookup {

    private static final Logger LOG = LoggerFactory.getLogger(FieldStrLookup.class);

    private final Issue issue;
    private final I18nHelper i18nHelper;

    public FieldStrLookup(I18nHelper i18nHelper,
                          Issue issue) {
        this.i18nHelper = i18nHelper;
        this.issue = issue;
    }

    @Override
    public String lookup(String key) {
        CustomField customField = ComponentAccessor.getCustomFieldManager()
                .getCustomFieldObject(key);
        if (customField != null) {
            if (customField.getValueFromIssue(issue) == null) {
                LOG.warn("Field '{}' resolved to null. Returning empty string.", key);
                return StringUtils.EMPTY;
            }

            return customField.getValueFromIssue(issue);
        }

        Field field = ComponentAccessor.getFieldManager()
                .getField(key);
        if (field == null) {
            LOG.error("Field '{}' not found.", key);
            return i18nHelper.getText("bamboo-plan-runner.postfunction.field.value.unresolved");
        }
        if (!(field instanceof ExportableSystemField)) {
            LOG.error("Value of field '{}' is not exportable.", key);
            return i18nHelper.getText("bamboo-plan-runner.postfunction.field.value.unresolved");
        }

        final List<String> fieldValues = new ArrayList<>();
        ((ExportableSystemField) field)
                .getRepresentationFromIssue(issue)
                .getPartWithId(key)
                .ifPresent(fieldExportPart ->
                        fieldExportPart.getValues().forEach(fieldValues::add));

        return StringUtils.join(fieldValues, ", ");
    }
}
