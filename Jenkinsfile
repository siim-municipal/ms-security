pipeline {
    agent any

    // hook local para preguntar a git si hay cambios
    triggers {
            // H/2 significa: "Revisa cada 2 minutos si hay cambios en Git"
            pollSCM('H/2 * * * *')
        }

    tools {
      jdk 'openjdk-21.0.3'
      maven 'maven3'
    }

    options {
      buildDiscarder logRotator(numToKeepStr: '10')
    }

    environment {
        pkgName = 'ms-security'
        currentVersion = 'DEV-SNAPSHOT'
    }

    stages {
        stage("init") {
            steps {
                echo "Procesando rama ${env.GIT_BRANCH}"
                echo "Notificando inicio de build..."
                script {
                    pom = readMavenPom file: "pom.xml"
                    pkgName = pom.artifactId

                    def current = env.GIT_BRANCH.replace("origin/", "");
                    if(current != 'develop') {
                        pom = readMavenPom file: "pom.xml";
                        currentVersion = current != 'master' ? pom.version + '-SNAPSHOT' : pom.version
                    }
                }
            }
        }

        stage("build") {
            steps {
                configFileProvider([configFile(fileId: "devops-settings", variable: "MVN_SETTINGS")]) {
                    sh "mvn -s $MVN_SETTINGS clean compile test install"
                }
            }
        }

        stage("SonarQube") {
            steps {
                withSonarQubeEnv("SonarServer") {
                    script {
                        def fixedBranchName = env.GIT_BRANCH.replace("origin/", "").replace("/", "_")
                        configFileProvider([configFile(fileId: "devops-settings", variable: "MVN_SETTINGS")]) {
                            sh """
                                mvn -s $MVN_SETTINGS \
                                org.sonarsource.scanner.maven:sonar-maven-plugin:3.11.0.3922:sonar \
                                -Dsonar.projectName=${pkgName}:${fixedBranchName} \
                                -Dsonar.projectKey=${pkgName}:${fixedBranchName}
                            """
                        }
                    }
                }
            }
        }

        stage("SonarQube Quality Gate") {
            when {
                not {
                    anyOf {
                        branch "master"
                        branch "qa"
                    }
                }
            }
            steps {
                timeout(time: 15, unit: "MINUTES") {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage("package") {
            when {
                anyOf {
                    branch "master"
                    branch "develop"
                    branch "release/*"
                    branch "hotfix/*"
                    branch "qa"
                }
            }
            steps {
                configFileProvider([configFile(fileId: "devops-settings", variable: "MVN_SETTINGS")]) {
                    sh "mvn -s $MVN_SETTINGS package -DskipTests"
                }
            }
        }
    }

    post {
        always {
            deleteDir()
        }
        success {
            echo "Notificando SUCCESSFULL"
        }
        unsuccessful {
            echo "Notificando FAILED"
        }
    }

}