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
import com.amazonaws.services.codecommit.AWSCodeCommitClient;
import com.amazonaws.services.codecommit.model.ListRepositoriesRequest;
import com.amazonaws.services.codecommit.model.ListRepositoriesResult;
import com.amazonaws.services.codecommit.model.RepositoryNameIdPair;
import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.google.common.collect.Lists;
import com.jenkins.plugins.awscodecommit.Messages;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.GitSCMSourceContext;
import jenkins.plugins.git.traits.BranchDiscoveryTrait;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.trait.SCMNavigatorRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.impl.form.NamedArrayList;
import jenkins.scm.impl.trait.Discovery;
import jenkins.scm.impl.trait.Selection;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Navigate into repositiories aws code commit.
 */
public class AWSCodeCommitSCMNavigator extends SCMNavigator {

    /**
     * Check the URL codecommit to get the region.
     */
    private static final Pattern PATTERN_CODE_COMMIT_URL = Pattern.compile("https:\\/\\/codecommit\\.(.*)\\.amazonaws\\.com");


    /**
     * Separator for ID.
     */
    public static final String SEPARATOR_ID = "::";

    /**
     * Credentials matcher for AMI
     */
    public static final CredentialsMatcher CREDENTIALS_MATCHER = CredentialsMatchers.instanceOf(AmazonWebServicesCredentials.class);


    /**
     * The server url of AWS.
     */
    private String awsCodeCommitURL;

    /**
     * The regex
     */
    private String pattern;

    /**
     * Credianttals for the API
     */
    @CheckForNull
    private String awsCredentialsId;

    /**
     * Credianttals for code commit
     */
    @CheckForNull
    private String codeCommitCredentialsId;

    /**
     * Behaviours on SCM. (Pattern, authentication,...)
     */
    private List<SCMTrait<? extends SCMTrait<?>>> traits;


    @DataBoundConstructor
    public AWSCodeCommitSCMNavigator() {}

    @NonNull
    @Override
    protected String id() {
        return new StringBuilder().append(awsCodeCommitURL).append(SEPARATOR_ID).append(awsCredentialsId).toString();
    }


    @Override
    public void visitSources(@NonNull SCMSourceObserver scmSourceObserver) throws IOException, InterruptedException {
        final TaskListener listener = scmSourceObserver.getListener();

        final AmazonWebServicesCredentials credentialsForAPI = getCredentials(scmSourceObserver.getContext(), awsCredentialsId, AmazonWebServicesCredentials.class, CREDENTIALS_MATCHER);
        final StandardUsernameCredentials credentialsForCodeCommit = getCredentials(scmSourceObserver.getContext(), codeCommitCredentialsId, StandardUsernameCredentials.class, GitClient.CREDENTIALS_MATCHER);
        final String region = getRegion();

        final PrintStream logger = listener.getLogger();
        if (region == null || credentialsForCodeCommit == null) {
            logger.printf("visitSources - with null value(s): '%s' '%s' '%s' '%s'%n", awsCodeCommitURL, awsCredentialsId, codeCommitCredentialsId, region);
        } else {
            logger.println("visitSources - start to checkout the code");
            if (traits == null) {
                logger.println("visitSources - null traits");
                this.traits = Collections.emptyList();
            } else {
                logger.printf("visitSources - traits length %d%n", traits.size());
            }

            final AWSCodeCommit client = AWSCodeCommitClient.builder()
                    .withRegion(region)
                    .withCredentials(credentialsForAPI)
                    .build();
            try (final AWSCodeCommitSCMNavigatorRequest request = new AWSCodeCommitSCMNavigatorContext()
                    .withTraits(traits)
                    .withAwsCodeCommit(client)
                    .withCodeCommitCredentialsId(codeCommitCredentialsId)
                    .withCloneSsh(credentialsForCodeCommit instanceof SSHUserPrivateKey)
                    .newRequest(this, scmSourceObserver)) {

                final SCMNavigatorRequest.Witness loggerWitness = new LoggerWitness(listener.getLogger());
                final SourceFactory sourceFactory = new SourceFactory(getId(), request);
                processRepositories(client, request, loggerWitness, sourceFactory, logger);
            }
            logger.println("visitSources - end to checkout the code");
        }
    }



    /**
     * Generate
     *
     * @param client the client AWS
     * @param request the request to process the repository
     * @param loggerWitness the logger
     * @param sourceFactory the source factory to create project
     * @param logger logger for jenkins
     * @throws IOException              if there is an I/O error.
     * @throws InterruptedException     if the operation was interrupted.
     */
    protected void processRepositories(AWSCodeCommit client, AWSCodeCommitSCMNavigatorRequest request, SCMNavigatorRequest.Witness loggerWitness, SourceFactory sourceFactory, PrintStream logger) throws IOException, InterruptedException {
        logger.printf("pattern %s%n", pattern);

        final ListRepositoriesRequest listRequest = new ListRepositoriesRequest();

        final Pattern patternValue = pattern != null ? Pattern.compile(pattern) : null;

        do {
            final ListRepositoriesResult allRepositories = client.listRepositories(listRequest);

            for (RepositoryNameIdPair repository : allRepositories.getRepositories()) {
                if(patternValue == null || patternValue.matcher(repository.getRepositoryName()).matches()){
                    logger.printf("%s - matches%n",repository);
                    request.process(repository.getRepositoryName(), sourceFactory, null, loggerWitness);
                } else {
                    logger.printf("%s - no matches%n",repository);
                }
            }
            listRequest.setNextToken(allRepositories.getNextToken());
        } while (listRequest.getNextToken() != null);
    }

    /**
     * Get the region in awsCodeCommitURL
     *
     * @return return the region
     */
    public String getRegion() {
        if (awsCodeCommitURL == null) {
            return null;
        }
        final Matcher matcher = PATTERN_CODE_COMMIT_URL.matcher(awsCodeCommitURL);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;

    }


    /**
     * Get the credentials AWS with id awsCredentialsId
     *
     * @param context            the context jenkins
     * @param credentialsId      the identifier of AWS Credentials
     * @param credentialsMatcher
     * @return the AmazonWebServicesCredentials with id == awsCredentialsId
     */
    private <T extends StandardCredentials> T getCredentials(@CheckForNull SCMSourceOwner context,
                                                             @CheckForNull String credentialsId, Class<T> lookupWhat, CredentialsMatcher credentialsMatcher) {
        //TODO see how can see add test
        if (StringUtils.isNotBlank(credentialsId) && context != null) {
            return CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            lookupWhat,
                            context,
                            context instanceof Queue.Task
                                    ? ((Queue.Task) context).getDefaultAuthentication()
                                    : ACL.SYSTEM,
                            URIRequirementBuilder.create().build()
                    ),
                    CredentialsMatchers.allOf(
                            CredentialsMatchers.withId(credentialsId),
                            credentialsMatcher
                    )
            );
        }
        return null;
    }

    @NonNull
    @SuppressWarnings("unused") // stapler form binding
    public final String getAwsCodeCommitURL() {
        return awsCodeCommitURL;
    }

    @SuppressWarnings("unused") // stapler form binding
    public final String getAwsCredentialsId() {
        return awsCredentialsId;
    }


    @DataBoundSetter
    @SuppressWarnings("unused") // stapler form binding
    public final void setAwsCodeCommitURL(@NonNull String awsCodeCommitURL) {
        this.awsCodeCommitURL = awsCodeCommitURL;
    }


    @DataBoundSetter
    @SuppressWarnings("unused") // stapler form binding
    public final void setAwsCredentialsId(String awsCredentialId) {
        this.awsCredentialsId = awsCredentialId;
    }

    @DataBoundSetter
    @SuppressWarnings("unused") // stapler form binding
    public final void setTraits(@NonNull List<SCMTrait<? extends SCMTrait<?>>> traits) {
        if(traits == null){
            this.traits = Collections.emptyList();
        } else {
            this.traits = Collections.unmodifiableList(traits);
        }
    }

    @NonNull
    @SuppressWarnings("unused") // stapler form binding
    public final List<SCMTrait<? extends SCMTrait<?>>> getTraits() {
        return traits;
    }


    @SuppressWarnings("unused") // stapler form binding
    public String getCodeCommitCredentialsId() {
        return codeCommitCredentialsId;
    }

    @DataBoundSetter
    @SuppressWarnings("unused") // stapler form binding
    public void setCodeCommitCredentialsId(String codeCommitCredentialsId) {
        this.codeCommitCredentialsId = codeCommitCredentialsId;
    }

    @DataBoundSetter
    public void setPattern(@NonNull String pattern) {
        this.pattern = pattern;
    }

    @NonNull
    public String getPattern() {
        return pattern;
    }

    @Symbol("awscodecommit")
    @Extension
    public static class DescriptorImpl extends SCMNavigatorDescriptor {

        /**
         * Default aws codecommit URL.
         */
        protected static final String DEFAULT_SERVER_URL = "https://codecommit.us-east-1.amazonaws.com";
        /**
         * The default pattern
         */
        private static final String DEFAULT_PATTERN = ".*";

        @Override
        public String getDisplayName() {
            return Messages.AWSCodeCommitSCMNavigator_DisplayName();
        }

        @Override
        public String getDescription() {
            return Messages.AWSCodeCommitSCMNavigator_Description();
        }


        @Override
        public SCMNavigator newInstance(String name) {
            final AWSCodeCommitSCMNavigator navigator = new AWSCodeCommitSCMNavigator();
            navigator.setAwsCodeCommitURL(DEFAULT_SERVER_URL);
            navigator.setPattern(DEFAULT_PATTERN);
            navigator.setTraits(Lists.<SCMTrait<? extends SCMTrait<?>>>newArrayList(new BranchDiscoveryTrait()));
            return navigator;
        }

        /**
         * Validation patter
         *
         * @param pattern validate the pattern
         * @return OK is a good pattern, error if not
         */
        @SuppressWarnings("unused") // stapler form binding
        public FormValidation doCheckPattern(@QueryParameter String pattern) {
            try{
                Pattern.compile(pattern);
                return FormValidation.ok();
            } catch(PatternSyntaxException e) {
                return FormValidation.error(e.getLocalizedMessage());
            }
        }


        /**
         * Validation URL
         *
         * @param awsCodeCommitURL the url to validate
         * @return OK is a URL, error if not
         */
        @SuppressWarnings("unused") // stapler form binding
        public FormValidation doCheckAwsCodeCommitURL(@QueryParameter String awsCodeCommitURL) {
            if (PATTERN_CODE_COMMIT_URL.matcher(awsCodeCommitURL).matches()) {
                return FormValidation.ok();
            }
            return FormValidation.error(Messages.AWSCodeCommitSCMNavigator_AwsCodeCommitURLCheckKo(PATTERN_CODE_COMMIT_URL.pattern()));
        }


        /**
         * Listbox for the credentials for the API.
         * Use list only the {@link AmazonWebServicesCredentials}
         *
         * @param context SCM source context
         * @return listbox with all authentication
         */
        @SuppressWarnings("unused") // stapler form binding
        public ListBoxModel doFillAwsCredentialsIdItems(@AncestorInPath SCMSourceOwner context) {
            return createListBoxCredentials(context, AmazonWebServicesCredentials.class, AWSCodeCommitSCMNavigator.CREDENTIALS_MATCHER);
        }

        /**
         * Listbox for the credentials for the API.
         *
         * @param context SCM source context
         * @return listbox with all authentication
         */
        @SuppressWarnings("unused") // stapler form binding
        public ListBoxModel doFillCodeCommitCredentialsIdItems(@AncestorInPath SCMSourceOwner context) {
            return createListBoxCredentials(context, StandardUsernameCredentials.class, GitClient.CREDENTIALS_MATCHER);
        }

        /**
         * Create listbox of credentials
         *
         * @param context            SCM source context
         * @param clazz              the credential s class
         * @param credentialsMatcher the matchers
         * @param <T>                the credential s class
         * @return listbox of credentials
         */
        private <T extends StandardCredentials> StandardListBoxModel createListBoxCredentials(SCMSourceOwner context,
                                                                                      final Class<T> clazz, CredentialsMatcher credentialsMatcher) {
            final StandardListBoxModel result = new StandardListBoxModel();
            result.includeMatchingAs(
                    context instanceof Queue.Task
                            ? ((Queue.Task) context).getDefaultAuthentication()
                            : ACL.SYSTEM,
                    context,
                    clazz,
                    URIRequirementBuilder.create().build(),
                    credentialsMatcher
            );
            result.includeEmptyValue();
            return result;
        }


        /**
         * The behaviours for the repositories git.
         *
         * @return the list of traits
         */
        @SuppressWarnings("unused") // stapler form binding
        public List<NamedArrayList<? extends SCMSourceTraitDescriptor>> getTraitsDescriptorLists() {
            final List<NamedArrayList<? extends SCMSourceTraitDescriptor>> result = new ArrayList<>();

            final List<SCMSourceTraitDescriptor> all = new ArrayList<>(SCMSourceTrait._for(null, GitSCMSourceContext.class, GitSCMBuilder.class));

            //add filter and discover
            NamedArrayList.select(all, Messages.AWSCodeCommitSCMNavigator_WithinRepository(), NamedArrayList
                            .anyOf(NamedArrayList.withAnnotation(Discovery.class),
                                    NamedArrayList.withAnnotation(Selection.class)),
                    true, result);

            //add trait on git
            NamedArrayList.select(all, Messages.AWSCodeCommitSCMNavigator_Additional(), new NamedArrayList.Predicate<SCMSourceTraitDescriptor>() {
                @Override
                public boolean test(SCMSourceTraitDescriptor d) {
                    return GitSCM.class.isAssignableFrom(d.getScmClass());
                }
            }, true, result);


            return result;
        }

    }

}