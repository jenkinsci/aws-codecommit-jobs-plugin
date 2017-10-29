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
package com.jenkins.plugins.awscodecommit.navigator;

import com.amazonaws.services.codecommit.AWSCodeCommit;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jenkins.plugins.awscodecommit.AwCodeCommitUtils;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class SourceFactoryTest {


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());


    @Test
    public void given__getRemote__cloneSsh_is_true__then__getCloneUrlSsh() throws Exception {
        //prepare
        final AWSCodeCommit codeCommitClient = AwCodeCommitUtils.getAwsCodeCommit(wireMockRule);

        final SourceFactory sourceFactory = new SourceFactory(null, null);

        //the API
        givenThat(post(anyUrl()).withRequestBody(containing("{\"repositoryName\":\"repository-name-1\"}"))
                .willReturn(aResponse().withBodyFile("get-repository.json")));

        //action
        final String actual = sourceFactory.getRemote(codeCommitClient, "repository-name-1", true);

        //test
        assertThat(actual, is("ssh://git-codecommit.us-east-1.amazonaws.com/v1/repos/repository-name-1"));

    }


    @Test
    public void given__getRemote__cloneSsh_is_false__then__getCloneUrlhttp() throws Exception {
        //prepare
        final AWSCodeCommit codeCommitClient = AwCodeCommitUtils.getAwsCodeCommit(wireMockRule);

        final SourceFactory sourceFactory = new SourceFactory(null, null);


        //the API
        givenThat(post(anyUrl()).withRequestBody(containing("{\"repositoryName\":\"repository-name-1\"}"))
                .willReturn(aResponse().withBodyFile("get-repository.json")));


        //action
        final String actual = sourceFactory.getRemote(codeCommitClient, "repository-name-1", false);

        //test
        assertThat(actual, is("https://git-codecommit.us-east-1.amazonaws.com/v1/repos/repository-name-1"));

    }


}