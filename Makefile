#
# The MIT License
# Copyright © 2016 Stephane Jeandeaux and all contributors
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#

jenkins_url=http://localhost:8080
plugin=aws-codecommit-jobs

help: ## this help
	@grep -hE '^[a-zA-Z_-]+.*?:.*?## .*$$' ${MAKEFILE_LIST} | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

infra-up: infra-down ## start infra jenkins
	docker-compose up -d --build

infra-down: ## down infra jenkins
	(docker-compose stop && docker-compose rm -f) || true

install: build load jenkins-safeRestart ## install the plugin into jenkins and restarts it.

build: ## build the plugin
	mvn clean package -DskipTests=false

load: ## load the plugin into jenkins
	curl -i -F file=@target/$(plugin).hpi $(jenkins_url)/pluginManager/uploadPlugin

jenkins-safeRestart: ## restart jenkins
	curl -X POST $(jenkins_url)/safeRestart




