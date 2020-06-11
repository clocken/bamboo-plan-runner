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

package com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.impl;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.ReadOnlyApplicationLink;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.BambooRestApi;
import com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.Plan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.Plan.Builder.aPlan;

// TODO: Use Jackson for JSON mapping?
@Named
public final class BambooRestApiImpl implements BambooRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(BambooRestApiImpl.class);

    private static final String REST_API_BASE_URL = "/rest/api/latest";
    private static final String PLAN_API_URL = REST_API_BASE_URL + "/plan";
    private static final String PLAN_API_VARIABLE_QUERY_PARAMETER = "expand=variableContext";

    private static final String HTTP_HEADER_APPLICATION_JSON = "application/json";

    @Override
    public List<Plan> plans(ReadOnlyApplicationLink bambooAppLink) throws CredentialsRequiredException, ResponseException {
        final List<Plan> plans = new ArrayList<>();
        final List<URL> planLinks = new ArrayList<>();

        try {
            // TODO: Does this work with every user? Possibly not! And what about those Access tokens?
            bambooAppLink.createAuthenticatedRequestFactory()
                    .createRequest(Request.MethodType.GET, PLAN_API_URL)
                    .addHeader("Accept", HTTP_HEADER_APPLICATION_JSON)
                    .execute(
                            response -> {
                                if (!response.isSuccessful()) {
                                    LOG.error("Request for {} was unsuccessful. Status code is {}", PLAN_API_URL, response.getStatusCode());
                                    return;
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
                                    LOG.error("Error parsing response: {}", e.getMessage());
                                    LOG.error("Exception: ", e);
                                }
                            });

            for (URL planLink : planLinks) {
                bambooAppLink.createAuthenticatedRequestFactory()
                        .createRequest(Request.MethodType.GET, planLink.getPath() + '?' + PLAN_API_VARIABLE_QUERY_PARAMETER)
                        .addHeader("Accept", HTTP_HEADER_APPLICATION_JSON)
                        .execute(response -> {
                            if (!response.isSuccessful()) {
                                LOG.error("Request for {} was unsuccessful. Status code is {}", planLink.getPath() + PLAN_API_VARIABLE_QUERY_PARAMETER, response.getStatusCode());
                                return;
                            }

                            try {
                                List<String> planVariables = new ArrayList<>();
                                JSONObject jsonResponse = new JSONObject(response.getResponseBodyAsString());
                                JSONArray jsonPlanVariables = jsonResponse
                                        .getJSONObject("variableContext")
                                        .getJSONArray("variable");

                                for (int i = 0; i < jsonPlanVariables.length(); i++) {
                                    JSONObject jsonPlanVariable = jsonPlanVariables.getJSONObject(i);
                                    planVariables.add(jsonPlanVariable.getString("key"));
                                }

                                plans.add(aPlan()
                                        .withKey(jsonResponse.getString("key"))
                                        .withName(jsonResponse.getString("shortName"))
                                        .thatIsEnabled(jsonResponse.getBoolean("enabled"))
                                        // TODO: Handle optional description
                                        .withLink(planLink)
                                        .withVariables(planVariables).build());
                            } catch (JSONException e) {
                                LOG.error("Error parsing response: {}", e.getMessage());
                                LOG.error("Exception: ", e);
                            }
                        });
            }
        } catch (CredentialsRequiredException ex) {
            LOG.error("No credentials stored for the context user. Bamboo plans cannot be fetched.");
            throw ex;
        } catch (ResponseException ex) {
            LOG.error("Request for {} was unsuccessful.", PLAN_API_URL);
            throw ex;
        }

        return Collections.unmodifiableList(plans);
    }
}
