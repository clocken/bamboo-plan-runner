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

package com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;

import java.util.List;
import java.util.Optional;

/**
 * A helper service to access JIRA fields and their values.
 */
public interface FieldAccessor {

    /**
     * Gets a list of all JIRA fields that are exportable (see {@link com.atlassian.jira.issue.export.ExportableSystemField}
     * and {@link com.atlassian.jira.issue.export.customfield.ExportableCustomFieldType}).
     *
     * @return The list of exportable JIRA fields
     * @throws FieldException in case of a field access error
     */
    List<Field> getAllExportableJiraFields() throws FieldException;

    /**
     * Tries to get a field value from the given issue by interpreting the given identifier as a <i>Custom field ID</i> or
     * <i>Custom field name</i> (ID precedes name).
     *
     * @param issue      The issue to get the field value from
     * @param identifier <i>Custom field ID</i> or <i>Custom field name</i>
     * @return The field value wrapped in an {@link Optional}.<br>
     * An empty {@link Optional}, if the field could not be found, is not an exportable custom field
     * (see {@link com.atlassian.jira.issue.export.customfield.ExportableCustomFieldType}
     * or an ambiguous <i>Custom field name</i> was given.
     */
    Optional<String> getCustomFieldValueFromIssue(Issue issue, String identifier);

    /**
     * Tries to get a field value from the given issue by interpreting the given identifier as a <i>System field ID</i>.
     *
     * @param issue      The issue to get the field value from
     * @param identifier <i>System field ID</i>
     * @return The field value wrapped in an {@link Optional}.<br>
     * An empty {@link Optional}, if the field could not be found or is not an exportable field
     * (see {@link com.atlassian.jira.issue.export.ExportableSystemField}).
     */
    Optional<String> getSystemFieldValueFromIssue(Issue issue, String identifier);
}
