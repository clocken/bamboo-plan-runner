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

package ut.mock.fields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.export.ExportableSystemField;
import com.atlassian.jira.issue.export.FieldExportParts;
import com.atlassian.jira.issue.export.FieldExportPartsBuilder;
import com.atlassian.jira.mock.issue.fields.MockNavigableField;

/**
 * {@inheritDoc}
 * <p>
 * Mocks the {@link com.atlassian.jira.issue.fields.DescriptionSystemField}.
 */
public class MockDescriptionSystemField extends MockNavigableField implements ExportableSystemField {

    private static final String NAME = "Description";

    public MockDescriptionSystemField() {
        super("description");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public FieldExportParts getRepresentationFromIssue(Issue issue) {
        return FieldExportPartsBuilder.buildSinglePartRepresentation(getId(), NAME, issue.getDescription());
    }
}
