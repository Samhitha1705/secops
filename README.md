# 🚀 DevSecOps CI/CD Pipeline Project (Jenkins + Docker + Kubernetes + Security Tools)



## 📌 Overview

This project demonstrates a complete **DevSecOps CI/CD pipeline** that automates:

- Code build and testing
- Static code analysis
- Dependency vulnerability scanning
- Container security scanning
- Docker image creation and publishing
- Kubernetes deployment

It follows real-world DevSecOps practices where security is integrated into every stage of the pipeline.

---

## 🏗️ Architecture Flow


GitHub Repository
↓
Jenkins CI/CD Pipeline
↓
Maven Build + Unit Tests
↓
SonarQube Code Analysis
↓
OWASP Dependency Check
↓
Docker Image Build
↓
Push to Docker Hub
↓
Trivy Security Scans (FS + Image)
↓
Kubernetes Deployment
↓
Application Running


Tools Used


| Tool                   | Purpose                         |
| ---------------------- | ------------------------------- |
| Jenkins                | CI/CD automation                |
| Maven                  | Build & dependency management   |
| SonarQube              | Code quality + bug detection    |
| OWASP Dependency Check | Library vulnerability scanning  |
| Trivy                  | Container + filesystem scanning |
| Docker                 | Containerization                |
| Kubernetes             | Deployment platform             |



Jenkins Pipeline Explanation

🔹 1. Clean Workspace

cleanWs()

✔ Removes previous build files
✔ Ensures fresh build environment


🔹 2. Checkout Code

checkout scm

✔ Pulls latest code from GitHub repository

🔹 3. Build + Test

mvn clean verify

✔ Compiles source code
✔ Runs unit tests
✔ Generates build artifacts

🔹 4. SonarQube Analysis

mvn sonar:sonar

✔ Performs static code analysis
✔ Detects:

Bugs
Code smells
Vulnerabilities


🔹 5. Quality Gate Check

waitForQualityGate()

✔ Ensures code meets SonarQube standards
✔ Fails pipeline if quality is poor

🔹 6. OWASP Dependency Check

dependencyCheck --scan . --format HTML

✔ Scans project dependencies
✔ Identifies known CVEs
✔ Runs in non-blocking mode (unstable only)

🔹 7. Package Application

mvn package -DskipTests

✔ Creates JAR file for deployment

🔹 8. Docker Build

docker build -t <image> .

✔ Builds Docker image from Dockerfile

🔹 9. Docker Login

docker login

✔ Authenticates Jenkins with Docker Hub

🔹 10. Push Docker Image

docker push


✔ Uploads image to Docker Hub registry

🔹 11. Trivy File System Scan

trivy fs --severity HIGH,CRITICAL .

✔ Scans source code & dependencies
✔ Detects vulnerabilities in files
✔ Marked as UNSTABLE if issues found

🔹 12. Trivy Image Scan

trivy image --severity HIGH,CRITICAL <image>

✔ Scans Docker image layers
✔ Finds OS & library vulnerabilities
✔ Does NOT block deployment (unstable only)

🔹 13. Kubernetes Deployment

kubectl apply -f k8s/
kubectl rollout status deployment/devsecops-demo

✔ Deploys application to Kubernetes cluster
✔ Ensures rollout success
✔ Displays pods and services

☁️ Deployment Output


After successful pipeline execution:

Application deployed on Kubernetes
Pods running successfully
Service exposed via NodePort
Accessible via:

http://172.21.7.189:31080/

http://localhost:31080


Pipeline Status Logic


| Status      | Meaning                                        |
| ----------- | ---------------------------------------------- |
| SUCCESS ✔   | Everything clean                               |
| UNSTABLE ⚠️ | Vulnerabilities found but deployment continued |
| FAILURE ❌   | Build or critical stage failed                 |


🔥 Key Highlights of This Project


Full CI/CD automation
Multi-layer security scanning
Real Kubernetes deployment
Docker containerization
Industry-style DevSecOps flow
Production-like pipeline structure


🏁 Conclusion

This project demonstrates a complete end-to-end DevSecOps pipeline where:

Code is automatically built, tested, scanned for vulnerabilities, containerized, and deployed to Kubernetes with security checks at every stage.


