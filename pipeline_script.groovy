pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'main', url: 'https://github.com/UnoYoon/20250324a.git'
            }
        }

        stage('Build') {
            steps {
                dir('./step07_cicd') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test'
                    echo "✅ Build complete!"
                }
            }
        }

        stage('Copy jar') {
            steps {
                script {
                    sh 'cp step07_cicd/build/libs/step07_cicd-0.0.1-SNAPSHOT.jar /var/jenkins_home/appjar/'
                }
            }
        }

        stage('Transfer to Server (myserver02)') {
            steps {
                script {
                    def localJar = '/var/jenkins_home/appjar/step07_cicd-0.0.1-SNAPSHOT.jar'
                    def remoteUser = 'jihye2'
                    def remoteHost = '192.168.0.16'
                    def remotePath = '/home/jihye2/jarappdir/'

                    sh "scp ${localJar} ${remoteUser}@${remoteHost}:${remotePath}"
                    echo "🚚 Transferred jar to myserver02!"
                }
            }
        }
    }
}