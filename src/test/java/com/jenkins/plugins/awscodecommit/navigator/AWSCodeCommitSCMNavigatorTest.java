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
import jenkins.scm.api.trait.SCMNavigatorRequest;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.PrintStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;


public class AWSCodeCommitSCMNavigatorTest {


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    // detect unused stubs
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void given__getRegion__when__null_url__then_null() throws Exception {
        AWSCodeCommitSCMNavigator nav = new AWSCodeCommitSCMNavigator();
        assertThat(nav.getRegion(), is(nullValue()));
    }


    @Test
    public void given__getRegion__when__no__matcher__then_null() throws Exception {
        AWSCodeCommitSCMNavigator nav = new AWSCodeCommitSCMNavigator();
        nav.setAwsCodeCommitURL("no-matchers");

        assertThat(nav.getRegion(), is(nullValue()));
    }


    @Test
    public void given__getRegion__when__matcher__then_region() throws Exception {
        AWSCodeCommitSCMNavigator nav = new AWSCodeCommitSCMNavigator();
        nav.setAwsCodeCommitURL("https://codecommit.us-west-2.amazonaws.com");
        assertThat(nav.getRegion(), is("us-west-2"));
    }

    @Test
    public void given__processRepository__when_listRepositories_then__process() throws Exception {
        //prepare
        final String scenarioName = "1000-repositories";
        final String nextTokenState = "state-1000-repositories";

        final AWSCodeCommit codeCommitClient = AwCodeCommitUtils.getAwsCodeCommit(wireMockRule);

        final AWSCodeCommitSCMNavigator nav = new AWSCodeCommitSCMNavigator();

        final AWSCodeCommitSCMNavigatorRequest requestMock = mock(AWSCodeCommitSCMNavigatorRequest.class);
        final SCMNavigatorRequest.Witness witnessMock = mock(SCMNavigatorRequest.Witness.class);
        final SourceFactory sourceFactoryMock = mock(SourceFactory.class);
        final PrintStream loggerMock = mock(PrintStream.class);

        //the API
        givenThat(post(anyUrl())
                        //scenario
                        .inScenario(scenarioName).willSetStateTo(nextTokenState).whenScenarioStateIs(STARTED)
                        .withRequestBody(containing("{}"))
                        .willReturn(aResponse().withBodyFile("list-repositories-1.json")));


        givenThat(post(anyUrl())
                .inScenario(scenarioName).whenScenarioStateIs(nextTokenState)
                .withRequestBody(containing("{\"nextToken\":\"1000-repositories\"}"))
                .willReturn(aResponse().withBodyFile("list-repositories-2.json")));



        //test
        nav.processRepositories(codeCommitClient, requestMock, witnessMock, sourceFactoryMock, loggerMock);

        verify(requestMock).process("repo-1", sourceFactoryMock, null, witnessMock);
        verify(requestMock).process("repo-1000", sourceFactoryMock, null, witnessMock);
        verify(requestMock).process("repo-1001", sourceFactoryMock, null, witnessMock);
        verify(requestMock).process("repo-1002", sourceFactoryMock, null, witnessMock);

    }


    @Test
    public void given__processRepository__when_listRepositories_with_regex_then__process() throws Exception {
        //prepare
        final String scenarioName = "1000-repositories";
        final String nextTokenState = "state-1000-repositories";

        final AWSCodeCommit codeCommitClient = AwCodeCommitUtils.getAwsCodeCommit(wireMockRule);

        //regex on one application
        final AWSCodeCommitSCMNavigator nav = new AWSCodeCommitSCMNavigator();
        nav.setAwsCodeCommitURL("https://codecommit.us-west-2.amazonaws.com");
        nav.setPattern("repo-1000");


        final AWSCodeCommitSCMNavigatorRequest requestMock = mock(AWSCodeCommitSCMNavigatorRequest.class);
        final SCMNavigatorRequest.Witness witnessMock = mock(SCMNavigatorRequest.Witness.class);
        final SourceFactory sourceFactoryMock = mock(SourceFactory.class);
        final PrintStream loggerMock = mock(PrintStream.class);

        //the API
        givenThat(post(anyUrl())
                //scenario
                .inScenario(scenarioName).willSetStateTo(nextTokenState).whenScenarioStateIs(STARTED)
                .withRequestBody(containing("{}"))
                .willReturn(aResponse().withBodyFile("list-repositories-1.json")));


        givenThat(post(anyUrl())
                .inScenario(scenarioName).whenScenarioStateIs(nextTokenState)
                .withRequestBody(containing("{\"nextToken\":\"1000-repositories\"}"))
                .willReturn(aResponse().withBodyFile("list-repositories-2.json")));



        //test
        nav.processRepositories(codeCommitClient, requestMock, witnessMock, sourceFactoryMock, loggerMock);

        verify(requestMock, never()).process("repo-1", sourceFactoryMock, null, witnessMock);
        verify(requestMock).process("repo-1000", sourceFactoryMock, null, witnessMock);
        verify(requestMock, never()).process("repo-1001", sourceFactoryMock, null, witnessMock);
        verify(requestMock, never()).process("repo-1002", sourceFactoryMock, null, witnessMock);

    }
}