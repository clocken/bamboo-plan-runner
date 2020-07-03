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

package com.github.clocken.jira.workflow.postfunctions.bamboo.plan.runner.internal.api.bamboo;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.ReadOnlyApplicationLink;
import com.atlassian.sal.api.net.ResponseException;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * A service wrapper for selected Bamboo REST API endpoints needed by the Bamboo Plan Runner.
 */
public interface BambooRestApi {

    /**
     * Fetches a list of all plans in a Bamboo instance.
     * See also: <a href=https://docs.atlassian.com/atlassian-bamboo/REST/latest/#d2e2338>https://docs.atlassian.com/atlassian-bamboo/REST/latest/#d2e2338</a>
     *
     * @param bambooApplink a {@link ReadOnlyApplicationLink} to the Bamboo instance
     * @return A list of all plans in the given Bamboo instance
     * @throws CredentialsRequiredException in case authentication fails
     * @throws ResponseException            in case something went wrong during the request
     */
    @NotNull
    List<Plan> plans(ReadOnlyApplicationLink bambooApplink) throws CredentialsRequiredException, ResponseException;

    /**
     * Enqueues a new build for the given plan on the given Bamboo instance.
     * See also: <a href=https://docs.atlassian.com/atlassian-bamboo/REST/latest/#d2e1256>https://docs.atlassian.com/atlassian-bamboo/REST/latest/#d2e1256</a>
     *
     * @param bambooApplink    a {@link ReadOnlyApplicationLink} to the Bamboo instance
     * @param planKey          the key of the plan to build. Should be in the form of '{projectKey}-{buildKey}'
     * @param valuesByVariable the (optional) mapping of variables with values for the plan to run
     * @throws CredentialsRequiredException in case authentication fails
     * @throws ResponseException            in case something went wrong during the request
     */
    void queueBuild(ReadOnlyApplicationLink bambooApplink, String planKey, Map<String, String> valuesByVariable) throws CredentialsRequiredException, ResponseException;
}
