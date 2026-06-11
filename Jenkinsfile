pipeline {

    agent any

    tools {
        maven 'Maven3'
    }

    environment {
        SONAR_SERVER = 'sonar'
        DOCKER_IMAGE = 'vedasamhitha17/devsecops-demo:latest'
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
                        echo "SonarQube Quality Gate status: ${qg.status}"

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
                sh 'trivy fs .'
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
                    usernameVariable: 'USER',
                    passwordVariable: 'PASS'
                )]) {
                    sh '''
                    echo $PASS | docker login -u $USER --password-stdin
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
                sh "trivy image ${DOCKER_IMAGE}"
            }
        }

        /* ---------------- KUBERNETES ---------------- */

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                kubectl apply -f k8s/
                kubectl rollout status deployment/devsecops-demo
                kubectl get pods
                kubectl get svc
                '''
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
