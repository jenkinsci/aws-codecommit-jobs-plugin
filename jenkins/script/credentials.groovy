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
import jenkins.model.*;




domain = com.cloudbees.plugins.credentials.domains.Domain.global()
credsStore = Jenkins.instance.getExtensionList("com.cloudbees.plugins.credentials.SystemCredentialsProvider")[0].getStore()



//Git secret
gitSecret = new com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl(
		com.cloudbees.plugins.credentials.CredentialsScope.GLOBAL,
		"ami-jenkins-git-http",
		"ami-jenkins-git-http",
		System.getenv("GIT_USER"),
		System.getenv("GIT_PASSWORD")
)
credsStore.addCredentials(domain, gitSecret)

//AMI amazon
amiSecret = new com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl(
		com.cloudbees.plugins.credentials.CredentialsScope.GLOBAL,
		"ami-jenkins",
		System.getenv("AMI_ACCESS"),
		System.getenv("AMI_SECRET"),
		"ami-jenkins"
)
credsStore.addCredentials(domain, amiSecret)

