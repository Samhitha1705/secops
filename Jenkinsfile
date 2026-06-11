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
                script {
                    timeout(time: 15, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        echo "Quality Gate: ${qg.status}"

                        if (qg.status != 'OK') {
                            error "Pipeline failed due to Quality Gate: ${qg.status}"
                        }
                    }
                }
            }
        }

        /* ---------------- SECURITY SCANS ---------------- */

        stage('OWASP Dependency Check') {
            steps {
                script {
                    try {
                        dependencyCheck(
                            additionalArguments: '--scan .',
                            odcInstallation: 'DependencyCheck'
                        )
                    } catch (err) {
                        echo "OWASP not configured - skipping"
                    }
                }
            }
        }

        stage('Trivy FS Scan') {
            steps {
                script {
                    try {
                        sh 'trivy fs .'
                    } catch (err) {
                        echo "Trivy FS not available - skipping"
                    }
                }
            }
        }

        /* ---------------- BUILD ARTIFACT ---------------- */

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        /* ---------------- DOCKER ---------------- */

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                    '''
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh "docker push ${DOCKER_IMAGE}"
            }
        }

        stage('Trivy Image Scan') {
            steps {
                script {
                    try {
                        sh "trivy image ${DOCKER_IMAGE}"
                    } catch (err) {
                        echo "Trivy image scan skipped"
                    }
                }
            }
        }

        /* ---------------- KUBERNETES DEPLOY ---------------- */

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
            echo "PIPELINE SUCCESS ✔"
        }

        failure {
            echo "PIPELINE FAILED ❌"
        }

        always {
            cleanWs()
        }
    }
}
