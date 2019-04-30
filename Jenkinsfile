pipeline {
    agent {
        kubernetes {
            label 'jenkins-agent-pod'
            defaultContainer 'maven'
            yamlFile 'jenkins/jenkins-agent.yaml'
        }
    }
    stages {
        stage('build') {
            steps {
                sh 'mvn clean install -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'
                junit '**/target/surefire-reports/*.xml' 
            }
        }
        stage('deploy') {
            when {
                buildingTag()
            }
            steps {
                sh 'mvn deploy -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'
            }
        }
        stage('cleanup') {
            steps {
                sh 'docker image prune -a --force'
            }
        }
    }
}