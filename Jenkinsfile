pipeline {

    agent any

    tools {
        maven 'Maven3'
    }

    environment {
        SONAR_SERVER = 'sonar'
        DOCKER_IMAGE = 'vedasamhitha17/devsecops-demo:latest'
        KUBECONFIG = '/var/lib/jenkins/.kube/config'
    }

    stages {

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build + Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONAR_SERVER}") {
                    sh '''
                    mvn sonar:sonar \
                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }

        stage('Quality Gate Check') {
            steps {
                timeout(time: 15, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Quality Gate failed: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                script {
                    def status = sh(
                        script: '''
                        dependencyCheck \
                        --scan . \
                        --format HTML
                        ''',
                        returnStatus: true
                    )

                    if (status != 0) {
                        unstable('OWASP found vulnerabilities (non-blocking)')
                    }
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                sh "docker push ${DOCKER_IMAGE}"
            }
        }

        stage('Trivy File System Scan') {
            steps {
                script {
                    def status = sh(
                        script: '''
                        trivy fs \
                        --severity HIGH,CRITICAL \
                        --exit-code 1 \
                        .
                        ''',
                        returnStatus: true
                    )

                    if (status != 0) {
                        unstable('Trivy FS found vulnerabilities (non-blocking)')
                    }
                }
            }
        }

        stage('Trivy Image Scan') {
            steps {
                script {
                    def status = sh(
                        script: """
                        trivy image \
                        --severity HIGH,CRITICAL \
                        --exit-code 1 \
                        ${DOCKER_IMAGE}
                        """,
                        returnStatus: true
                    )

                    if (status != 0) {
                        unstable('Trivy Image found vulnerabilities (non-blocking)')
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                export KUBECONFIG=${KUBECONFIG}

                kubectl apply -f k8s/
                kubectl rollout status deployment/devsecops-demo
                kubectl get pods
                kubectl get svc
                """
            }
        }
    }

    post {
        success {
            echo 'PIPELINE SUCCESS ✔'
        }

        unstable {
            echo 'PIPELINE SUCCESS WITH WARNINGS ⚠️ (Security issues found but deployed)'
        }

        failure {
            echo 'PIPELINE FAILED ❌'
        }

        always {
            cleanWs()
        }
    }
}
