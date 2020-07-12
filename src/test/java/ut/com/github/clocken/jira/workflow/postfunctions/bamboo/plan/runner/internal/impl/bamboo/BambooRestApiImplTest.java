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

package ut.com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl.bamboo;

import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.ReadOnlyApplicationLink;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.bamboo.BambooRestApi;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl.bamboo.BambooRestApiImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ut.mocks.applinks.MockApplicationLinkRequest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BambooRestApiImplTest {

    private static String planApiJsonResponse;
    private static String planVariableTestJsonResponse;
    private ReadOnlyApplicationLink bambooApplicationLink;
    private ApplicationLinkRequestFactory applicationLinkRequestFactory;
    private BambooRestApi bambooRestApi;

    @BeforeClass
    public static void initialize() throws Exception {
        planApiJsonResponse = Files.lines(
                Paths.get("src", "test", "resources", "bamboo", "bamboo-plan-api-response.json"))
                .map(StringUtils::stripToEmpty)
                .collect(Collectors.joining());
        planVariableTestJsonResponse = Files.lines(
                Paths.get("src", "test", "resources", "bamboo", "plans", "variable-test.json"))
                .map(StringUtils::stripToEmpty)
                .collect(Collectors.joining());
    }

    @Before
    public void setUp() throws Exception {
        applicationLinkRequestFactory = mock(ApplicationLinkRequestFactory.class);
        when(applicationLinkRequestFactory
                .createRequest(Request.MethodType.GET, "/rest/api/latest/plan"))
                .thenReturn(new MockApplicationLinkRequest(
                        planApiJsonResponse,
                        200,
                        true));
        when(applicationLinkRequestFactory
                .createRequest(Request.MethodType.GET, "/rest/api/latest/plan/PLAYG-VAR?expand=variableContext"))
                .thenReturn(new MockApplicationLinkRequest(
                        planVariableTestJsonResponse,
                        200,
                        true
                ));

        bambooApplicationLink = mock(ReadOnlyApplicationLink.class);
        when(bambooApplicationLink.createAuthenticatedRequestFactory())
                .thenReturn(applicationLinkRequestFactory);

        bambooRestApi = new BambooRestApiImpl();
    }

    @Test
    public void should_return_plans_from_bamboo_instance() throws CredentialsRequiredException, ResponseException {
        assertEquals(1, bambooRestApi.plans(bambooApplicationLink).size());
    }
}
