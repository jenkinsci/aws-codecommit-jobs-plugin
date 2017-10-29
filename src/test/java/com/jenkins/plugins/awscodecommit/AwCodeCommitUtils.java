/**
 * The MIT License
 * Copyright © 2016 Stephane Jeandeaux and all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.jenkins.plugins.awscodecommit;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.codecommit.AWSCodeCommit;
import com.amazonaws.services.codecommit.AWSCodeCommitClient;
import com.github.tomakehurst.wiremock.junit.WireMockRule;


public class AwCodeCommitUtils {


    /**
     * the host for AWS API.
     */
    private static final String LOCALHOST = "localhost:";
    /**
     * the region for AWS API.
     */
    private static final String REGION = "groland";

    /**
     * Generate client for code commit API
     *
     * @return the client to call API Code Commit
     */
    public static AWSCodeCommit getAwsCodeCommit(WireMockRule wireMockRule) {
        String serviceEndpoint = new StringBuilder(LOCALHOST).append(wireMockRule.port()).toString();
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, REGION);
        ClientConfiguration config = new ClientConfiguration().withProtocol(Protocol.HTTP);


        return AWSCodeCommitClient.builder()
                .withEndpointConfiguration(endpointConfiguration)
                .withClientConfiguration(config)
                .build();
    }
}
