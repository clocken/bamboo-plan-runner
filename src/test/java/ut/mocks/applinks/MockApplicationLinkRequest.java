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

package ut.mocks.applinks;

import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.sal.api.net.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A mock for an {@link ApplicationLinkRequest}.<br>
 * The response for this mocked request can be set at creation time.
 */
public class MockApplicationLinkRequest implements ApplicationLinkRequest {

    private final Map<String, List<String>> headers = new HashMap<>();
    private final String responseBody;
    private final int statusCode;
    private final boolean mockSuccessfulRequest;

    public MockApplicationLinkRequest(String responseBody,
                                      int statusCode,
                                      boolean mockSuccessfulRequest) {
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.mockSuccessfulRequest = mockSuccessfulRequest;
    }

    @Override
    public <R> R execute(ApplicationLinkResponseHandler<R> responseHandler) throws ResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ApplicationLinkRequest setConnectionTimeout(int connectionTimeout) {
        return this;
    }

    @Override
    public ApplicationLinkRequest setSoTimeout(int soTimeout) {
        return this;
    }

    @Override
    public ApplicationLinkRequest setUrl(String url) {
        return this;
    }

    @Override
    public ApplicationLinkRequest setRequestBody(String requestBody) {
        return this;
    }

    @Override
    public ApplicationLinkRequest setRequestBody(String requestBody, String contentType) {
        return this;
    }

    @Override
    public ApplicationLinkRequest setFiles(List<RequestFilePart> files) {
        return this;
    }

    @Override
    public ApplicationLinkRequest setEntity(Object entity) {
        return this;
    }

    @Override
    public ApplicationLinkRequest addRequestParameters(String... params) {
        return this;
    }

    @Override
    public ApplicationLinkRequest addBasicAuthentication(String hostname, String username, String password) {
        return this;
    }

    @Override
    public ApplicationLinkRequest addHeader(String headerName, String headerValue) {
        headers.put(headerName, Collections.singletonList(headerValue));
        return this;
    }

    @Override
    public ApplicationLinkRequest setHeader(String headerName, String headerValue) {
        return this;
    }

    @Override
    public ApplicationLinkRequest setFollowRedirects(boolean follow) {
        return this;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public void execute(ResponseHandler<? super Response> responseHandler) throws ResponseException {
        responseHandler.handle(new MockResponse(responseBody, statusCode, mockSuccessfulRequest, new HashMap<>()));
    }

    @Override
    public String execute() throws ResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <RET> RET executeAndReturn(ReturningResponseHandler<? super Response, RET> responseHandler) throws ResponseException {
        throw new UnsupportedOperationException();
    }
}
