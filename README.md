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

### ⚙️사전 환경 설정
### 1. ngrok을 통해 외부에서 접근 가능한 환경을 구성

<br>

![image](https://github.com/user-attachments/assets/9034f785-f0d0-4cbe-aea1-5257672f5eb8)

<br>


### 2. GitHub Webhook, 저장소에서 발생한 이벤트 외부 서버로 자동 전달

<br>

![image](https://github.com/user-attachments/assets/7bdacf38-bc87-4727-a544-58a96351d1d6)

<br>

### 3. Github hook trigger for GITScm polling 설정

<br>

![image](https://github.com/user-attachments/assets/cff6ab08-18a2-424d-8849-814d1fe548a2)

GitHub Webhook을 통해 코드 변경 사항이 있을 때 

Jenkins가 즉시 빌드를 트리거하도록 하기 위해 사용됩니다.

<br>

### 4. Jdk, Gradle, Maven 설정
![jdk](https://github.com/user-attachments/assets/3f4fd258-9ad6-4f0b-a255-375fc9677995)
![gradle](https://github.com/user-attachments/assets/e35480d3-e031-4b49-af7a-ea6ed3ce264a)
![maven](https://github.com/user-attachments/assets/83544fdd-482d-4e4d-b502-01af04aca0da)

<br>

### 5. Github 토큰 추가
![git토큰](https://github.com/user-attachments/assets/85d2306d-ca16-46c7-8d64-9bc88cb7c3f5)

<br>

### 6. Jenkins 환경 설정

Jenkins를 Docker 컨테이너에서 실행할 경우, 컨테이너 내부에서 SSH 키를 설정해야 합니다.

``` 
docker exec -it <container ID> bash
ssh-keygen -t rsa -b 4096
ls -l .ssh
cat .ssh/authorized_keys
cat .ssh/id_rsa
cat .ssh/id_rsa.pub
ssh-copy-id ubuntu@<server ip or 설정 hostname>
ssh ubuntu@<server ip or 설정 hostname>  # 비밀번호 없이 접속되는지 확인
```

<br>

### 📤파일 전송


### 1. 파일 전송 테스트

서버 간 파일 전송이 정상적으로 이루어지는지 확인합니다.

```
scp 복사할파일 username@remote_server_ip:/복사받을경로
```

### 2. 파일 변경 모니터링

server01에서 특정 파일이 수정되었는지 실시간으로 감지합니다.

```
inotifywait -m -e close_write $(pwd)/
```

### 3. Jenkins CI/CD Pipeline

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
                    sh 'scp /var/jenkins_home/appjar/step07_cicd-0.0.1-SNAPSHOT.jar wonho@10.0.2.16:/home/jihye/bind/'
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


### 4. 자동 배포 및 재시작


서버 B로 파일을 전송 후, 배포 스크립트를 실행하여 애플리케이션을 재시작합니다.


```
ssh wonho@10.0.2.16 "bash /home/wonho/bind/restart.sh"
```


이 과정을 통해 GitHub 코드 변경 → Jenkins 빌드 → 서버 B 배포 → 애플리케이션 

자동 재시작까지의 CI/CD 파이프라인을 구축 완료하였습니다.

<br>

### 5. jar파일 정상작동 확인
<br>

![정상작동](https://github.com/user-attachments/assets/7ab1331e-43b0-4d0f-8eaa-deac98b7959a)

<br>

## 🛠️ Troubleshooting



### 문제 상황 1 : SSH 접속 시 비밀번호 요청 문제

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
### 문제 상황 2 : myserver01(Unbuntu)에 직접 설치되어있는 Jenkins 아이디 비밀번호 잊어버렸을때 
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

---

### 문제 상황 3 : IP주소 재할당으로 인하여 Jenkins 속도가 심각하게 저하됨
<br>

### 해결 방법 
변경된 IP 주소를 입력하여 해결


![image](https://github.com/user-attachments/assets/5b90ee90-e906-4cc4-bd39-39502dc2051e)

---

### 문제 상황 4 : Ubuntu 서버에 할당된 디스크 용량 부족

<br>

![image](https://github.com/user-attachments/assets/8f6332f9-7210-49e0-a909-fa6e27657118)
<br>


### 원인 분석
Ubuntu VM에 할당된 디스크 용량이 부족하여 Jenkins Built-In Node에서 Disk 관련 용량 부족 문제가 발생합니다.
<br>


### 해결 방법
Ubuntu VM에 디스크 확장을 해주어서 해결합니다.

1. lsblk 를 통하여 현재 Ubuntu VM에 할당된 디스크 용량과 추가적으로 할당이 가능한 디스크 용량을 체크합니다.
```bash
ubuntu@server01:~$ lsblk
NAME                      MAJ:MIN RM  SIZE RO TYPE MOUNTPOINTS
loop0                       7:0    0 44.4M  1 loop /snap/snapd/23771
loop1                       7:1    0 73.9M  1 loop /snap/core22/1748
loop2                       7:2    0 41.3M  1 loop /snap/trivy/276
sda                         8:0    0   20G  0 disk
├─sda1                      8:1    0    1M  0 part
├─sda2                      8:2    0  1.8G  0 part /boot
└─sda3                      8:3    0 18.2G  0 part     # 총 디스크 용량
  └─ubuntu--vg-ubuntu--lv 252:0    0   10G  0 lvm  /   # 우분투에 할당 10G - 추가 할당 가능
```
2. -l +100%FREE 명령어를 통하여 논리 볼륨(/dev/ubuntu-vg/ubuntu-lv)에 남은 모든 여유 공간을 할당합니다.

```bash
ubuntu@server01:~$ sudo lvextend -l +100%FREE /dev/ubuntu-vg/ubuntu-lv
```
3. 논리 볼륨 확장 단계(위)를 마무리하고 파일 시스템 또한 확장해주어야 합니다.
   
```bash
sudo resize2fs /dev/ubuntu-vg/ubuntu-lv
```


4. 정상적으로 할당이 되었는지 확인하기 위하여 df -h 명령어를 통하여 확인합니다.
```bash
ubuntu@server01:~$ df -h
Filesystem                         Size  Used Avail Use% Mounted on
tmpfs                              387M  1.7M  386M   1% /run
/dev/mapper/ubuntu--vg-ubuntu--lv   18G  8.3G  8.8G  49% /
tmpfs                              1.9G     0  1.9G   0% /dev/shm
tmpfs                              5.0M     0  5.0M   0% /run/lock
/dev/sda2                          1.8G   96M  1.6G   6% /boot
tmpfs                              387M   16K  387M   1% /run/user/1000
```
<br>



### 결과
디스크 부족 관련 에러가 나타나지 않으며 노드가 정상적으로 빌드를 수행합니다.

<br>


![image](https://github.com/user-attachments/assets/1e189101-ebf0-4484-8607-2766dc8a189c)
