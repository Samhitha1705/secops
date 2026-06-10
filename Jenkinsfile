pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Unit Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Build + Coverage') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh '''
                    mvn sonar:sonar \
                    -Dsonar.projectKey=devsecops-demo
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                waitForQualityGate abortPipeline: true
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                dependencyCheck(
                    additionalArguments: '--scan .',
                    odcInstallation: 'DependencyCheck'
                )
            }
        }

        stage('Trivy FS Scan') {
            steps {
                sh 'trivy fs .'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t devsecops-demo .'
            }
        }

        stage('Trivy Image Scan') {
            steps {
                sh 'trivy image devsecops-demo'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                docker rm -f devsecops-demo || true

                docker run -d \
                --name devsecops-demo \
                -p 8082:8080 \
                devsecops-demo
                '''
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
