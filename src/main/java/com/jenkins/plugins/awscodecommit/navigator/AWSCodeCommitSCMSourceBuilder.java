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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.trait.SCMSourceBuilder;

/**
 * Builder of {@link GitSCMSource}
 */
public class AWSCodeCommitSCMSourceBuilder extends SCMSourceBuilder<AWSCodeCommitSCMSourceBuilder, GitSCMSource> {


    /**
     * Id of {@link GitSCMSource}
     */
    @CheckForNull
    private final String id;

    /**
     * Credentials for git
     */
    @CheckForNull
    private String credentialsId;


    /**
     * Remote for git
     */
    @CheckForNull
    private String remote;


    /**
     * Initialize the builder.
     *
     * @param id          Id of {@link GitSCMSource}
     * @param projectName the project name
     */
    public AWSCodeCommitSCMSourceBuilder(final String id, final String projectName) {
        super(GitSCMSource.class, projectName);
        this.id = id;
    }

    @NonNull
    @Override
    public GitSCMSource build() {
        final GitSCMSource result = new GitSCMSource(remote);
        result.setId(id);
        result.setCredentialsId(credentialsId);
        result.setTraits(traits());
        return result;
    }

    /**
     * The request with traits of git and credentials
     *
     * @param request the request
     * @return the builder
     */
    public AWSCodeCommitSCMSourceBuilder withRequest(@NonNull AWSCodeCommitSCMNavigatorRequest request) {
        super.withRequest(request);
        this.credentialsId = request.getCodeCommitCredentialsId();
        return this;
    }


    /**
     * Set the remote of git
     *
     * @param remote the remote of get
     * @return the builder
     */
    public AWSCodeCommitSCMSourceBuilder withRemote(@NonNull String remote) {
        this.remote = remote;
        return this;
    }

}
