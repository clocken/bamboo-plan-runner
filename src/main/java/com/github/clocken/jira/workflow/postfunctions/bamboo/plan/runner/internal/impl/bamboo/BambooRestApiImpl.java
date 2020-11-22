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

package com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl.bamboo;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.ReadOnlyApplicationLink;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.bamboo.BambooRestApi;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.bamboo.Plan;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.bamboo.Plan.Builder.aPlan;

@Named
public final class BambooRestApiImpl implements BambooRestApi {

    private static final String REST_API_BASE = "/rest/api/latest";
    private static final String PLAN_API = REST_API_BASE + "/plan";
    private static final String PLAN_API_VARIABLE_QUERY_PARAMETER = "expand=variableContext";
    private static final String QUEUE_API = REST_API_BASE + "/queue";

    private static final String HTTP_HEADER_ACCEPT = "Accept";
    private static final String HTTP_HEADER_APPLICATION_JSON = "application/json";

    @Override
    public List<Plan> plans(ReadOnlyApplicationLink bambooApplink) throws CredentialsRequiredException, ResponseException {
        return getPlans(bambooApplink);
    }

    @Override
    public void queueBuild(ReadOnlyApplicationLink bambooApplink, String planKey, Map<String, String> valuesByVariable) throws CredentialsRequiredException, ResponseException {
        final List<String> planVariablesWithValues = new ArrayList<>();
        valuesByVariable.forEach((variable, value) -> {
            planVariablesWithValues.add("bamboo.variable." + variable);
            planVariablesWithValues.add(value);
        });

        bambooApplink.createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.POST, QUEUE_API + "/" + planKey)
                .addRequestParameters(planVariablesWithValues.toArray(new String[0]))
                .addHeader(HTTP_HEADER_ACCEPT, HTTP_HEADER_APPLICATION_JSON)
                .execute(response -> {
                    if (!response.isSuccessful()) {
                        throw new ResponseException(
                                MessageFormat.format("Request for {0} was unsuccessful. Status code is {1}",
                                        QUEUE_API + "/" + planKey,
                                        response.getStatusCode())
                        );
                    }
                });
    }

    private List<Plan> getPlans(ReadOnlyApplicationLink bambooApplink) throws ResponseException, CredentialsRequiredException {
        final List<Plan> plans = new ArrayList<>();
        for (URL planLink : getPlanLinks(bambooApplink)) {
            // Allow for context paths in the plan link
            String planLinkPath = StringUtils.removeStart(planLink.toString(), bambooApplink.getRpcUrl().toString());

            bambooApplink.createAuthenticatedRequestFactory()
                    .createRequest(Request.MethodType.GET, planLinkPath + '?' + PLAN_API_VARIABLE_QUERY_PARAMETER)
                    .addHeader(HTTP_HEADER_ACCEPT, HTTP_HEADER_APPLICATION_JSON)
                    .execute(response -> {
                        if (!response.isSuccessful()) {
                            throw new ResponseException(
                                    MessageFormat.format("Request for {0} was unsuccessful. Status code is {1}",
                                            planLink.toString() + '?' + PLAN_API_VARIABLE_QUERY_PARAMETER,
                                            response.getStatusCode()));
                        }
                        try {
                            JSONObject jsonResponse = new JSONObject(response.getResponseBodyAsString());
                            JSONArray jsonPlanVariables = jsonResponse
                                    .getJSONObject("variableContext")
                                    .getJSONArray("variable");

                            List<String> planVariables = new ArrayList<>();
                            for (int i = 0; i < jsonPlanVariables.length(); i++) {
                                JSONObject jsonPlanVariable = jsonPlanVariables.getJSONObject(i);
                                planVariables.add(jsonPlanVariable.getString("key"));
                            }

                            String planDescription = StringUtils.EMPTY;
                            try { // NOSONAR - nested block for optional parameter ok here
                                planDescription = jsonResponse.getString("description");
                            } catch (JSONException e) {
                                // This is an optional parameter
                            }
                            plans.add(aPlan()
                                    .withKey(jsonResponse.getString("key"))
                                    .withName(jsonResponse.getString("shortName"))
                                    .thatIsEnabled(jsonResponse.getBoolean("enabled"))
                                    .withDescription(planDescription)
                                    .withLink(planLink)
                                    .withVariables(planVariables).build());
                        } catch (JSONException e) {
                            throw new ResponseException(
                                    MessageFormat.format("Error parsing response from {0}", planLink),
                                    e);
                        }
                    });
        }
        return Collections.unmodifiableList(plans);
    }

    private List<URL> getPlanLinks(ReadOnlyApplicationLink bambooApplink) throws ResponseException, CredentialsRequiredException {
        List<URL> planLinks = new ArrayList<>();
        bambooApplink.createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.GET, PLAN_API)
                .addHeader(HTTP_HEADER_ACCEPT, HTTP_HEADER_APPLICATION_JSON)
                .execute(
                        response -> {
                            if (!response.isSuccessful()) {
                                throw new ResponseException(
                                        MessageFormat.format("Request for {0} was unsuccessful. Status code is {1}",
                                                PLAN_API,
                                                response.getStatusCode()));
                            }
                            try {
                                JSONObject jsonResponse = new JSONObject(response.getResponseBodyAsString());
                                JSONArray jsonPlans = jsonResponse
                                        .getJSONObject("plans")
                                        .getJSONArray("plan");

                                for (int i = 0; i < jsonPlans.length(); i++) {
                                    JSONObject jsonPlan = jsonPlans.getJSONObject(i);
                                    planLinks.add(new URL(jsonPlan
                                            .getJSONObject("link")
                                            .getString("href")));
                                }
                            } catch (JSONException | MalformedURLException e) {
                                throw new ResponseException(
                                        MessageFormat.format("Error parsing response from {0}", PLAN_API),
                                        e);
                            }
                        });
        return Collections.unmodifiableList(planLinks);
    }
}
