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

import hudson.util.FormValidation;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class DescriptorImplTest {


    @Test
    public void given__doCheckAwsCodeCommitURL__when__good_url__then_ok() throws Exception {
        final AWSCodeCommitSCMNavigator.DescriptorImpl descriptor = new AWSCodeCommitSCMNavigator.DescriptorImpl();
        assertThat(descriptor.doCheckAwsCodeCommitURL(AWSCodeCommitSCMNavigator.DescriptorImpl.DEFAULT_SERVER_URL), is(FormValidation.ok()));
    }


    @Test
    public void given__doCheckAwsCodeCommitURL__when__bad_url__then_error() throws Exception {
        final AWSCodeCommitSCMNavigator.DescriptorImpl descriptor = new AWSCodeCommitSCMNavigator.DescriptorImpl();
        assertThat(descriptor.doCheckAwsCodeCommitURL("bad").kind, is(FormValidation.Kind.ERROR));
    }


    @Test
    public void given__doCheckAwsCredentialsId__when__empty__then_error() throws Exception {
        final AWSCodeCommitSCMNavigator.DescriptorImpl descriptor = new AWSCodeCommitSCMNavigator.DescriptorImpl();
        assertThat(descriptor.doCheckAwsCredentialsId("").kind, is(FormValidation.Kind.ERROR));
    }


    @Test
    public void given__doCheckAwsCredentialsId__when__with__blank__then_error() throws Exception {
        final AWSCodeCommitSCMNavigator.DescriptorImpl descriptor = new AWSCodeCommitSCMNavigator.DescriptorImpl();
        assertThat(descriptor.doCheckAwsCredentialsId("    ").kind, is(FormValidation.Kind.ERROR));
    }

    @Test
    public void given__doCheckAwsCredentialsId__when__good__then_ok() throws Exception {
        final AWSCodeCommitSCMNavigator.DescriptorImpl descriptor = new AWSCodeCommitSCMNavigator.DescriptorImpl();
        assertThat(descriptor.doCheckAwsCredentialsId("id credentials"), is(FormValidation.ok()));
    }


    @Test
    public void given__doCheckPattern__when__good_regex__then_ok() throws Exception {
        final AWSCodeCommitSCMNavigator.DescriptorImpl descriptor = new AWSCodeCommitSCMNavigator.DescriptorImpl();
        assertThat(descriptor.doCheckPattern(".*"), is(FormValidation.ok()));
    }


    @Test
    public void given__doCheckPattern__when__bad_regex__then_error() throws Exception {
        final AWSCodeCommitSCMNavigator.DescriptorImpl descriptor = new AWSCodeCommitSCMNavigator.DescriptorImpl();
        assertThat(descriptor.doCheckPattern("*").kind, is(FormValidation.Kind.ERROR));
    }
}