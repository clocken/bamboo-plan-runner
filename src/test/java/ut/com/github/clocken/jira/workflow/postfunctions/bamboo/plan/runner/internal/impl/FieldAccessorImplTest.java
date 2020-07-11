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

package ut.com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl;

import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.managers.MockCustomFieldManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.I18nHelper;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.FieldAccessor;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl.FieldAccessorImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import ut.mocks.fields.MockCustomFieldWithNameKey;
import ut.mocks.fields.MockDescriptionSystemField;
import ut.mocks.fields.MockLabelsCFType;
import ut.mocks.fields.MockNavigableFieldWithName;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class FieldAccessorImplTest {

    private static final long ISSUE_ID = 123;
    private static final String NON_EXPORTABLE_SYSTEM_FIELD_ID = "nonexportable";
    private static final String NON_EXPORTABLE_SYSTEM_FIELD_NAME = "Not exportable";
    private static final String NON_EXPORTABLE_CUSTOM_FIELD_ID = "customfield_10000";
    private static final String NON_EXPORTABLE_CUSTOM_FIELD_NAME = "My non-exportabel custom field";
    private static final String NON_EXPORTABLE_CUSTOM_FIELD_NAME_KEY = "custom.field.key." + NON_EXPORTABLE_CUSTOM_FIELD_ID;
    private static final String DESCRIPTION_FIELD_ID = "description";
    private static final String ISSUE_DESCRIPTION = "This is a test issue description.";
    private static final String LABELS_CUSTOM_FIELD_ID = "customfield_10001";
    private static final String LABELS_CUSTOM_FIELD_NAME = "Labels";
    private static final String LABELS_CUSTOM_FIELD_NAME_KEY = "custom.field.key." + LABELS_CUSTOM_FIELD_ID;
    private static final String LABEL_NAME_1 = "Label1";
    private static final String LABEL_NAME_2 = "Label2";
    private static final Label LABEL_1 = new Label(1L, ISSUE_ID, LABEL_NAME_1);
    private static final Label LABEL_2 = new Label(2L, ISSUE_ID, LABEL_NAME_2);
    private static final String AMBIGUOUS_CUSTOM_FIELD_ID_1 = "customfield_10002";
    private static final String AMBIGUOUS_CUSTOM_FIELD_NAME = "Ambiguous field";
    private static final String AMBIGUOUS_CUSTOM_FIELD_NAME_KEY_1 = "custom.field.key." + AMBIGUOUS_CUSTOM_FIELD_ID_1;
    private static final String AMBIGUOUS_CUSTOM_FIELD_ID_2 = "customfield_10003";
    private static final String AMBIGUOUS_CUSTOM_FIELD_NAME_KEY_2 = "custom.field.key." + AMBIGUOUS_CUSTOM_FIELD_ID_2;

    private static final MockDescriptionSystemField DESCRIPTION_SYSTEM_FIELD = new MockDescriptionSystemField();
    private static final MockNavigableFieldWithName NON_EXPORTABLE_SYSTEM_FIELD =
            new MockNavigableFieldWithName(
                    NON_EXPORTABLE_SYSTEM_FIELD_ID,
                    NON_EXPORTABLE_SYSTEM_FIELD_NAME);

    private static final MockCustomFieldWithNameKey LABELS_CUSTOM_FIELD = new MockCustomFieldWithNameKey(
            LABELS_CUSTOM_FIELD_ID,
            LABELS_CUSTOM_FIELD_NAME,
            LABELS_CUSTOM_FIELD_NAME_KEY,
            new MockLabelsCFType());
    private static final MockCustomFieldWithNameKey NON_EXPORTABLE_CUSTOM_FIELD = new MockCustomFieldWithNameKey(
            NON_EXPORTABLE_CUSTOM_FIELD_ID,
            NON_EXPORTABLE_CUSTOM_FIELD_NAME,
            NON_EXPORTABLE_CUSTOM_FIELD_NAME_KEY,
            new MockCustomFieldType());
    private static final MockCustomFieldWithNameKey AMBIGUOUS_CUSTOM_FIELD_1 = new MockCustomFieldWithNameKey(
            AMBIGUOUS_CUSTOM_FIELD_ID_1,
            AMBIGUOUS_CUSTOM_FIELD_NAME,
            AMBIGUOUS_CUSTOM_FIELD_NAME_KEY_1,
            new MockCustomFieldType());
    private static final MockCustomFieldWithNameKey AMBIGUOUS_CUSTOM_FIELD_2 = new MockCustomFieldWithNameKey(
            AMBIGUOUS_CUSTOM_FIELD_ID_2,
            AMBIGUOUS_CUSTOM_FIELD_NAME,
            AMBIGUOUS_CUSTOM_FIELD_NAME_KEY_2,
            new MockCustomFieldType());

    private FieldAccessor fieldAccessor;
    private MockIssue issue;

    @Before
    public void setUp() throws Exception {
        issue = new MockIssue(ISSUE_ID);
        issue.setDescription(ISSUE_DESCRIPTION);
        issue.setLabels(new HashSet<>(Arrays.asList(LABEL_1, LABEL_2)));

        Set<Field> allFields = new HashSet<>(Arrays.asList(
                DESCRIPTION_SYSTEM_FIELD,
                NON_EXPORTABLE_SYSTEM_FIELD,
                LABELS_CUSTOM_FIELD,
                NON_EXPORTABLE_CUSTOM_FIELD,
                AMBIGUOUS_CUSTOM_FIELD_1,
                AMBIGUOUS_CUSTOM_FIELD_2
        ));

        MockFieldManager fieldManager = spy(new MockFieldManager());
        doAnswer(invocation -> fieldManager.getNavigableFields())
                .when(fieldManager)
                .getAllAvailableNavigableFields();
        allFields.forEach(fieldManager::addField);

        MockCustomFieldManager customFieldManager = new MockCustomFieldManager();
        allFields
                .stream()
                .filter(field -> field instanceof CustomField)
                .map(field -> (CustomField) field)
                .forEach(customFieldManager::addCustomField);

        fieldAccessor = new FieldAccessorImpl(fieldManager,
                customFieldManager,
                mock(I18nHelper.class));
    }

    @Test
    public void should_get_all_exportable_jira_fields() throws FieldException {
        assertEquals(2,
                fieldAccessor.getAllExportableJiraFields().size());
    }

    @Test
    public void should_get_system_field_value_from_issue() {
        assertEquals(ISSUE_DESCRIPTION,
                fieldAccessor.getSystemFieldValueFromIssue(issue, DESCRIPTION_FIELD_ID).orElse(StringUtils.EMPTY));
    }

    @Test
    public void should_get_custom_field_value_from_issue_by_id() {
        assertEquals(StringUtils.join(Arrays.asList(LABEL_NAME_1, LABEL_NAME_2), ", "),
                fieldAccessor.getCustomFieldValueFromIssue(issue, LABELS_CUSTOM_FIELD_ID).orElse(StringUtils.EMPTY));
    }

    @Test
    public void should_get_custom_field_value_from_issue_by_name() {
        assertEquals(StringUtils.join(Arrays.asList(LABEL_1, LABEL_2), ", "),
                fieldAccessor.getCustomFieldValueFromIssue(issue, LABELS_CUSTOM_FIELD_NAME).orElse(StringUtils.EMPTY));
    }

    @Test
    public void should_return_empty_value_for_ambiguous_custom_field() {
        assertFalse(fieldAccessor.getCustomFieldValueFromIssue(issue, AMBIGUOUS_CUSTOM_FIELD_NAME).isPresent());
    }
}