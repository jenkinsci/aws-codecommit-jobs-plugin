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
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.trait.SCMNavigatorRequest;

/**
 * The {@link SCMNavigatorRequest} for aws code commit.
 */
public class AWSCodeCommitSCMNavigatorRequest extends SCMNavigatorRequest {


    /**
     * Credianttals for code commit
     */
    @CheckForNull
    private final String codeCommitCredentialsId;

    //TODO manage the client in better way
    @CheckForNull
    private final AWSCodeCommit awsCodeCommit;

    private boolean cloneSsh;

    
    /**
     * Constructor.
     *
     * @param source   the source.
     * @param context  the context.
     * @param observer the observer.
     */
    protected AWSCodeCommitSCMNavigatorRequest(@NonNull SCMNavigator source,
                                               @NonNull AWSCodeCommitSCMNavigatorContext context,
                                               @NonNull SCMSourceObserver observer) {
        super(source, context, observer);
        this.codeCommitCredentialsId = context.getCodeCommitCredentialsId();
        this.awsCodeCommit = context.getAwsCodeCommit();
        this.cloneSsh = context.isCloneSsh();
    }

    public String getCodeCommitCredentialsId() {
        return codeCommitCredentialsId;
    }

    public AWSCodeCommit getAwsCodeCommit() {
        return awsCodeCommit;
    }

    public boolean isCloneSsh() {
        return cloneSsh;
    }
}
