pipeline {
    agent any
    tools {
        maven 'Maven 3'
        jdk 'Java 14'
    }
    stages {
        stage ('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage ('Deploy') {
            when {
                branch "master"
            }
            steps {
                sh 'mvn deploy -DskipTests'
            }
        }
    }
}