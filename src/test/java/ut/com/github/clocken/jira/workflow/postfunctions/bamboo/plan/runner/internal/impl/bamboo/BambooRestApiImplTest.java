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
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.bamboo.Plan;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl.bamboo.BambooRestApiImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ut.mocks.applinks.MockApplicationLinkRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.bamboo.Plan.Builder.aPlan;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BambooRestApiImplTest {

    private static final String BAMBOO_BASE_URL = "http://mock";

    private static String planApiJsonResponse;
    private static String planVariableTestJsonResponse;
    private static Plan playgVarPlan;
    private ApplicationLinkRequestFactory applicationLinkRequestFactory;
    private ReadOnlyApplicationLink bambooApplicationLink;
    private BambooRestApi bambooRestApi;

    public BambooRestApiImplTest() throws MalformedURLException {
    }

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
        playgVarPlan = aPlan()
                .withKey("PLAYG-VAR")
                .withName("variable-test")
                .withLink(new URL(BAMBOO_BASE_URL + "/rest/api/latest/plan/PLAYG-VAR"))
                .thatIsEnabled(true)
                .withVariables(Arrays.asList("VARIABLE_TWO", "VARIABLE_ONE")).build();
    }

    @Before
    public void setUp() throws URISyntaxException {
        applicationLinkRequestFactory = mock(ApplicationLinkRequestFactory.class);
        bambooApplicationLink = mock(ReadOnlyApplicationLink.class);
        when(bambooApplicationLink.getRpcUrl())
                .thenReturn(new URI(BAMBOO_BASE_URL));
        when(bambooApplicationLink.createAuthenticatedRequestFactory())
                .thenReturn(applicationLinkRequestFactory);
        bambooRestApi = new BambooRestApiImpl();
    }

    @Test
    public void should_return_plans_from_bamboo_instance() throws CredentialsRequiredException, ResponseException {
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
        List<Plan> plans = bambooRestApi.plans(bambooApplicationLink);

        assertTrue(1 == plans.size()
                && playgVarPlan.equals(plans.get(0)));
    }

    @Test
    public void should_queue_build_for_plan() throws CredentialsRequiredException {
        when(applicationLinkRequestFactory
                .createRequest(Request.MethodType.POST, "/rest/api/latest/queue/PLAYG-VAR"))
                .thenReturn(new MockApplicationLinkRequest(
                        StringUtils.EMPTY,
                        200,
                        true
                ));

        try {
            bambooRestApi.queueBuild(bambooApplicationLink, "PLAYG-VAR", Collections.emptyMap());
        } catch (ResponseException | CredentialsRequiredException e) {
            fail(MessageFormat.format("Exception {0} should not have been thrown.", e));
        }
    }

    @Test(expected = ResponseException.class)
    public void should_handle_an_unsuccessful_request_for_plans() throws CredentialsRequiredException, ResponseException {
        when(applicationLinkRequestFactory
                .createRequest(Request.MethodType.GET, "/rest/api/latest/plan"))
                .thenReturn(new MockApplicationLinkRequest(
                        StringUtils.EMPTY,
                        500,
                        false));
        bambooRestApi.plans(bambooApplicationLink);
    }

    @Test(expected = ResponseException.class)
    public void should_handle_an_unsuccessful_request_for_plan_details() throws CredentialsRequiredException, ResponseException {
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
                        500,
                        false
                ));
        bambooRestApi.plans(bambooApplicationLink);
    }

    @Test(expected = ResponseException.class)
    public void should_handle_an_unsuccessful_request_for_build() throws CredentialsRequiredException, ResponseException {
        when(applicationLinkRequestFactory
                .createRequest(Request.MethodType.POST, "/rest/api/latest/queue/PLAYG-VAR"))
                .thenReturn(new MockApplicationLinkRequest(
                        StringUtils.EMPTY,
                        500,
                        false
                ));
        bambooRestApi.queueBuild(bambooApplicationLink, "PLAYG-VAR", Collections.emptyMap());
    }
}
