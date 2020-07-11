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

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.MockCustomField;

/**
 * {@inheritDoc}
 * <p>
 * The {@link MockCustomField} only supports stubbing an ID and name.<br>
 * This class also supports stubbing a <i>nameKey</i> for the mock.
 */
public class MockCustomFieldWithNameKey extends MockCustomField {

    private final String nameKey;

    public MockCustomFieldWithNameKey(String id, String name, String nameKey, CustomFieldType customFieldType) {
        super(id, name, customFieldType);
        this.nameKey = nameKey;
    }

    @Override
    public String getNameKey() {
        return nameKey;
    }
}
