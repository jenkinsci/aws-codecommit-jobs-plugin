node{
  stage ('Build') {

    checkout scm

    withMaven(
        maven: 'maven-3.5',
	jdk: 'jdk8'){

      sh "mvn clean install -U"
    }
  }
}

node{
  stage ('Deploy') {

    checkout scm

    withMaven(
        maven: 'maven-3.5',
	jdk: 'jdk8'){

      sh "mvn clean deploy -U"
    }
  }
}
