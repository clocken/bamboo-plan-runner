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

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.I18nHelper;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.FieldAccessor;
import org.apache.commons.lang.text.StrLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class finds JIRA field values from an issue by <i>System field ID</i>, <i>Custom field ID</i> or
 * <i>Custom field name</i>. Ambiguous custom field names get rejected.
 */
public class FieldValueLookup extends StrLookup {

    private static final Logger LOG = LoggerFactory.getLogger(FieldValueLookup.class);

    private final I18nHelper i18nHelper;
    private final FieldAccessor fieldAccessor;
    private final Issue issue;

    /**
     * Constructs a new {@link FieldValueLookup} instance.
     *
     * @param i18nHelper    The {@link I18nHelper} service for localization of the error message for unresolvable fields
     * @param fieldAccessor The {@link FieldAccessor} helper service to access field values from issues
     * @param issue         The JIRA {@link Issue} to lookup field values from
     */
    public FieldValueLookup(I18nHelper i18nHelper,
                            FieldAccessor fieldAccessor,
                            Issue issue) {
        this.i18nHelper = i18nHelper;
        this.fieldAccessor = fieldAccessor;
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
        return fieldAccessor.getCustomFieldValueFromIssue(issue, key)
                .orElseGet(() -> {
                    LOG.warn("Custom field '{}' not resolved. Trying as system field.", key);
                    return fieldAccessor.getSystemFieldValueFromIssue(issue, key)
                            .orElseGet(() -> {
                                LOG.error("Field '{}' is not resolvable.", key);
                                return i18nHelper.getText("bamboo-plan-runner.postfunction.field.value.unresolved");
                            });
                });
    }
}
