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

import com.atlassian.jira.mock.issue.fields.MockNavigableField;

/**
 * {@inheritDoc}
 * <p>
 * The {@link MockNavigableField} only supports stubbing an ID.<br>
 * This class also supports stubbing a <i>name</i> for the mock. If unset {@link MockNavigableField#getName()} is used.<br>
 * Also, this class does not mock an {@link com.atlassian.jira.issue.export.ExportableSystemField} -
 * see {@link MockDescriptionSystemField} for that.
 */
public class MockNavigableFieldWithName extends MockNavigableField {

    private final String name;

    public MockNavigableFieldWithName(String id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public String getName() {
        return name == null ? super.getName() : name;
    }
}
