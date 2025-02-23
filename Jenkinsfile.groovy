pipeline {
    agent {
        label "workerA"
    }
    triggers {
        cron('H/60 * * * *')
    }

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven "maven-3"
        jdk "jdk-17"
    }
    stages {
        stage('Clean') {
            steps {
                cleanWs()
            }
        }
        stage('Checkout') {
            steps {
                // Get some code from a GitHub repository
                git branch: 'main', url: 'https://github.com/Angiz/szkolenie-ci-jenkins-example.git'
            }
        }
        stage('Build') {
            steps {
                // Run Maven on a Unix agent.
                sh "mvn -Dmaven.test.failure.ignore=true clean verify"

                // To run Maven on a Windows agent, use
                // bat "mvn -Dmaven.test.failure.ignore=true clean package"
            }
        }
    }
    post {
        // If Maven was able to run the tests, even if some of the test
        // failed, record the test results and archive the jar file.
        success {
            junit '**/target/surefire-reports/TEST-*.xml'
            archiveArtifacts 'target/*.jar'
            slackSend (color: 'good', message:
                    """Build success!
                    BUILD_TAG: ${BUILD_TAG} 
                    BUILD_URL: ${BUILD_URL}""")
        }
        failure {
            junit '**/target/surefire-reports/TEST-*.xml'
            archiveArtifacts 'target/*.jar'
            slackSend (color: 'bad', message:
                    """Build failed!
                    BUILD_TAG: ${BUILD_TAG} 
                    BUILD_URL: ${BUILD_URL}""")
        }
    }
}
