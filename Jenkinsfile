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

        stage('Debug Workspace') {
            steps {
                sh '''
                echo "CURRENT DIRECTORY:"
                pwd

                echo "FILES:"
                ls -la

                echo "CHECK DOCKERFILE:"
                find . -name Dockerfile
                '''
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

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                ls -la
                docker build -t vedasamhitha17/devsecops-demo:latest .
                '''
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
                sh 'docker push vedasamhitha17/devsecops-demo:latest'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                kubectl apply -f k8s/
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
