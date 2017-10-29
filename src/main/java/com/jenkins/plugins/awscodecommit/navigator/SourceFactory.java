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
import com.amazonaws.services.codecommit.model.GetRepositoryRequest;
import com.amazonaws.services.codecommit.model.GetRepositoryResult;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMNavigatorRequest;

import java.io.IOException;

/**
 * The main goal of this class is to build a {@link SCMSource}
 */
public class SourceFactory implements SCMNavigatorRequest.SourceLambda {

    /**
     * Separator for ID.
     */
    public static final String SEPARATOR_ID = "::";

    /**
     * the id used like prefix for id scm source.
     */
    private final String id;

    /**
     * the request contains information on SCMSource.
     */
    private final AWSCodeCommitSCMNavigatorRequest request;

    /**
     * Constructor
     *
     * @param id      the id used like prefix for id scm source.
     * @param request the request contains information on SCMSource.
     */
    public SourceFactory(String id, AWSCodeCommitSCMNavigatorRequest request) {
        this.id = id;
        this.request = request;
    }

    @NonNull
    @Override
    public SCMSource create(@NonNull String projectName) throws IOException, InterruptedException {
        final String remote = getRemote(request.getAwsCodeCommit(), projectName, request.isCloneSsh());

        final String idSCMSource = new StringBuilder(id).append(SEPARATOR_ID).append(projectName).toString();
        return new AWSCodeCommitSCMSourceBuilder(idSCMSource, projectName)
                .withRequest(request)
                .withRemote(remote)
                .build();
    }


    /**
     * get the remote url on code commit for git.
     *
     * @param awsCodeCommit the client on API Code commit
     * @param projectName   the project name in code commit
     * @param cloneSsh      the type of clone
     * @return the remote
     */
    protected String getRemote(AWSCodeCommit awsCodeCommit, @NonNull String projectName, boolean cloneSsh) {
        final GetRepositoryRequest getRepositoryRequest = new GetRepositoryRequest().withRepositoryName(projectName);
        final GetRepositoryResult result = awsCodeCommit.getRepository(getRepositoryRequest);

        final String remote;
        if (cloneSsh) {
            remote = result.getRepositoryMetadata().getCloneUrlSsh();
        } else {
            remote = result.getRepositoryMetadata().getCloneUrlHttp();
        }
        return remote;
    }
}
