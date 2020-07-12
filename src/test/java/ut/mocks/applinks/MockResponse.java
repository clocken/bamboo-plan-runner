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

import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A mock for {@link Response}.<br>
 * The response parameters can be set at creation time.
 */
public class MockResponse implements Response {

    private final String responseBody;
    private final int statusCode;
    private final boolean mockSuccessfulRequest;
    private final Map<String, String> responseHeaders;

    public MockResponse(String responseBody,
                        int statusCode,
                        boolean mockSuccessfulRequest,
                        Map<String, String> responseHeaders) {
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.mockSuccessfulRequest = mockSuccessfulRequest;
        this.responseHeaders = Objects.requireNonNull(responseHeaders);
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getResponseBodyAsString() throws ResponseException {
        return StringUtils.defaultIfEmpty(responseBody, StringUtils.EMPTY);
    }

    @Override
    public InputStream getResponseBodyAsStream() throws ResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getEntity(Class<T> entityClass) throws ResponseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStatusText() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSuccessful() {
        return mockSuccessfulRequest;
    }

    @Override
    public String getHeader(String name) {
        return responseHeaders.get(name);
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(responseHeaders);
    }
}
