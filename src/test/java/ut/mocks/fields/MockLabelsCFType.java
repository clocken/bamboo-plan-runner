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

package ut.mocks.fields;

import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.export.FieldExportParts;
import com.atlassian.jira.issue.export.FieldExportPartsBuilder;
import com.atlassian.jira.issue.export.customfield.CustomFieldExportContext;
import com.atlassian.jira.issue.export.customfield.ExportableCustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.Label;

import java.util.Set;

/**
 * {@inheritDoc}
 * <p>
 * Mocks the {@link com.atlassian.jira.issue.customfields.impl.LabelsCFType}.
 */
public class MockLabelsCFType extends MockCustomFieldType implements ExportableCustomFieldType {

    public Set<Label> getValueFromIssue(CustomField field, Issue issue) {
        return issue.getLabels();
    }

    @Override
    public FieldExportParts getRepresentationFromIssue(Issue issue, CustomFieldExportContext customFieldExportContext) {
        return FieldExportPartsBuilder.buildSinglePartRepresentation(
                customFieldExportContext.getCustomField().getId(),
                customFieldExportContext.getCustomField().getName(),
                getValueFromIssue(customFieldExportContext.getCustomField(), issue)
                        .stream()
                        .map(Label::getLabel)
                        .sorted());
    }
}
