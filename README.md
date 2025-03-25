# jenkins-scp-cicd-practice

<br>

## 🤝 Team Members
| <img src="https://github.com/kcklkb.png" width="200px"> | <img src="https://github.com/woody6624.png" width="200px"> | <img src="https://github.com/parkjhhh.png" width="200px"> | <img src="https://github.com/unoYoon.png" width="200px"> |
| :---: | :---: | :---: | :---: |
| [김창규](https://github.com/kcklkb) | [김우현](https://github.com/woody6624) | [박지혜](https://github.com/parkjhhh) | [윤원호](https://github.com/unoYoon) |

<br>

## 🚀프로젝트 개요

GitHub의 수정된 값이 있을 때마다 자동으로 파이프라인을 수행하기 위해, 

GitHub Webhook을 사용합니다. 이를 위해 Ngrok을 통해 Jenkins 서버를 외부에 노출시키고, 

GitHub Webhook이 이를 트리거하여 Jenkins 빌드가 자동으로 실행되도록 설정합니다.

Ngrok을 사용하면 로컬에서 실행 중인 Jenkins 서버에 외부 접근을 가능하게 합니다. 

이를 통해 GitHub에서 코드 수정 시 실시간으로 자동 빌드 및 배포가 이루어집니다.

<br>

## 🔄프로젝트 목적

- 이 프로젝트는 Jenkins 환경에서 효율적인 CI/CD 파이프라인을 구축하여 개발 및 배포 속도를 극대화하는 것을 목표로 합니다.


- 자동화된 빌드 및 배포를 통해 운영 비용 절감과 안정적인 서비스 제공을 가능하게 합니다.


- GitHub Webhook과 Jenkins를 활용해 개발 변경 사항을 실시간으로 반영하여 지속적인 배포 환경을 구축합니다. 


<br>

## 📄프로젝트 과정

<br>


### ngrok을 통해 외부에서 접근 가능한 환경을 구성

<br>


![image](https://github.com/user-attachments/assets/9034f785-f0d0-4cbe-aea1-5257672f5eb8)

<br>

### GitHub Webhook, 저장소에서 발생한 이벤트 외부 서버로 자동 전달

<br>

![image](https://github.com/user-attachments/assets/7bdacf38-bc87-4727-a544-58a96351d1d6)



### Github hook trigger for GITScm polling 설정

<br>


![image](https://github.com/user-attachments/assets/cff6ab08-18a2-424d-8849-814d1fe548a2)

GitHub Webhook을 통해 코드 변경 사항이 있을 때 

Jenkins가 즉시 빌드를 트리거하도록 하기 위해 사용됩니다.

<br>

### 1. Jenkins 환경 설정

Jenkins를 Docker 컨테이너에서 실행할 경우, 컨테이너 내부에서 SSH 키를 설정해야 합니다.

```
docker exec -it <container ID> bash
ssh-keygen -t rsa -b 4096
ls -l .ssh
cat .ssh/authorized_keys
cat .ssh/id_rsa
cat .ssh/id_rsa.pub
ssh-copy-id ubuntu@10.0.2.20
ssh ubuntu@myserver02  # 비밀번호 없이 접속되는지 확인
```

### 2. 파일 전송 테스트

서버 간 파일 전송이 정상적으로 이루어지는지 확인합니다.

```
scp 복사할파일 username@remote_server_ip:/복사받을경로
```

### 3. 파일 변경 모니터링

server01에서 특정 파일이 수정되었는지 실시간으로 감지합니다.

```
inotifywait -m -e close_write $(pwd)/
```

### 4. Jenkins CI/CD Pipeline

Jenkinsfile을 이용하여 자동 빌드 및 배포를 설정합니다.

```
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
                dir('step07_cicd') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test'
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
        stage('Transfer to Server B') {
            steps {
                script {
                    sh 'scp /var/jenkins_home/appjar/step07_cicd-0.0.1-SNAPSHOT.jar wonho@10.0.2.20:/home/jihye/bind/'
                }
            }
        }
        stage('Restart Application on Server B') {
            steps {
                script {
                    sh 'ssh jihye2@10.0.2.20 "bash /home/jihye/bind/restart.sh"'
                }
            }
        }
    }
}
```


### 5. 자동 배포 및 재시작


서버 B로 파일을 전송 후, 배포 스크립트를 실행하여 애플리케이션을 재시작합니다.


```
ssh wonho@10.0.2.16 "bash /home/wonho/bind/restart.sh"
```


이 과정을 통해 GitHub 코드 변경 → Jenkins 빌드 → 서버 B 배포 → 애플리케이션 

자동 재시작까지의 CI/CD 파이프라인을 구축 완료하였습니다.

<br>

## 🛠️ Troubleshooting



### 문제 상황1 : SSH 접속 시 비밀번호 요청 문제
<br>

원격 서버 myserver01에서 myserver02로 SSH 접속 시, 정상적으로 설정된 SSH 키가 있음에도 불구하고 비밀번호를 계속해서 요구하는 문제가 발생하였습니다.

```
ssh ubuntu@myserver02 # 원래는 비밀번호 없이 접속되어야 함
```

<br>

### 원인 분석

원격 서버(myserver02)의 홈 디렉터리(~) 권한이 잘못 설정되어 SSH 인증이 정상적으로 작동하지 않았습니다.

SSH는 보안상 홈 디렉터리의 권한이 너무 넓거나 잘못된 경우 인증을 거부할 수 있습니다.

<br>

### 해결 방법

myserver02에서 홈 디렉터리(~)의 권한을 755로 수정하여 SSH 키 인증이 정상적으로 작동하도록 합니다.

```
chmod 755 ~  # 홈 디렉터리 권한 변경
```

<br>

### 결과

SSH 접속 시 더 이상 비밀번호를 묻지 않고 정상적으로 로그인됩니다.

SSH 키 기반 인증이 정상적으로 동작하여 자동화 배포 과정에서 문제 발생 가능성이 줄어들었습니다.


---
### 문제 상황2 : myserver01(Unbuntu)에 직접 설치되어있는 Jenkins 아이디 비밀번호 잊어버렸을때 
<br>


### 해결 방법
vi 편집기를 이용해서 xml파일 내용에 useSecurity부분을 false로 변경

![troubleshooting1](https://github.com/user-attachments/assets/6d646132-c9d4-4dbc-a646-e74ea2f31398)

```
jihye@myserver01:~$ vi /var/lib/jenkins/config.xml
```

<br>

### 결과

아이디와 비밀번호를 입력하지않아도 바로 Jenkins Dashboard 창으로 이동<br>
이 내부에서 아이디 확인
