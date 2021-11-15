pipeline {
    agent any
    tools {
        maven 'Maven 3'
        jdk 'Java 11'
    }
    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '1'))
    }
    stages {
        stage ('Build') {
            when { not { anyOf {
                branch 'master'
                branch 'develop'
                branch 'rewrite'
            }}}

            steps {
                sh 'mvn clean package'
            }
        }
        stage ('Deploy') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                    branch 'rewrite'
                }
            }

            stages {
                stage('Setup') {
                    steps {
                        rtMavenDeployer(
                                id: "maven-deployer",
                                serverId: "opencollab-artifactory",
                                releaseRepo: "maven-releases",
                                snapshotRepo: "maven-snapshots"
                        )
                        rtMavenResolver(
                                id: "maven-resolver",
                                serverId: "opencollab-artifactory",
                                releaseRepo: "maven-deploy-release",
                                snapshotRepo: "maven-deploy-snapshot"
                        )
                    }
                }

                stage('Release') {
                    when {
                        branch 'master'
                    }

                    steps {
                        rtMavenRun(
                                pom: 'pom.xml',
                                goals: 'source:jar install',
                                deployerId: "maven-deployer",
                                resolverId: "maven-resolver"
                        )
                    }
                }

                stage('Snapshot') {
                    when {
                        anyOf {
                            branch 'develop'
                            branch 'rewrite'
                        }
                    }
                    steps {
                        rtMavenRun(
                                pom: 'pom.xml',
                                goals: 'source:jar install',
                                deployerId: "maven-deployer",
                                resolverId: "maven-resolver"
                        )
                    }
                }

                stage('Publish') {
                    steps {
                        rtPublishBuildInfo(
                                serverId: "opencollab-artifactory"
                        )
                    }
                }
            }
        }
    }
}