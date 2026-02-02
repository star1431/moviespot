# MovieSpot - AWS 배포

## 배포 환경

- **서버**: AWS EC2
- **운영체제**: Ubuntu
- **배포 방식**: Docker Compose

---

## 사전 준비사항

### 1. EC2 인스턴스 설정

1. **EC2 인스턴스 생성**
   - 인스턴스 유형 : t3.micro (프리티어)
   - 보안 그룹 설정:
     - SSH (22): 모든 IP 허용
     - HTTP (80): 0.0.0.0/0
     - HTTPS (443): 0.0.0.0/0 (선택사항)
     - MySQL (3306): 필요시 (보안상 비권장)


2. **SSH 키 페어 생성 및 다운로드**

3. **EC2 - 탄력적 IP 생성 후 인스턴스 매핑**

---

## 서버 초기 설정

### 0. 인스턴스키 권한 설정 (Windows)

* Windows에서 SSH 키 파일(.pem)의 권한이 너무 열려있으면 보안 오류로 접속이 거부될 수 있음
* 해당명령어로 키 파일의 권한을 현재 사용자만 읽을 수 있도록 제한

```bash
# 기존 권한 초기화
icacls.exe "<인스턴스키>.pem" /reset

# 현재 사용자에게 읽기 권한만 부여
icacls.exe "<인스턴스키>.pem" /grant:r "%USERNAME%":"(R)"

# 상속 권한 제거 (다른 사용자가 접근하지 못하도록)
icacls.exe "<인스턴스키>.pem" /inheritance:r
```

**참고**: Linux/Mac에서는 `chmod 400 <인스턴스키>.pem` 명령어로 권한을 설정

### 1. 서버 접속

```bash
ssh -i <인스턴스키>.pem ec2-user@<퍼블릭IPv4주소>
```

### 2. 필수 패키지 설치

```bash
# 1) 업데이트 및 도커 설치
sudo yum update -y
sudo yum install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user # ec2-user를 도커 그룹에 추가
exit

# 2) 재접속 후 도커 컴포즈 설치
# docker cli plugin 디렉토리 생성
mkdir -p ~/.docker/cli-plugins

# docker compose 플러그인 다운로드
curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 \
  -o ~/.docker/cli-plugins/docker-compose

# 실행 권한 부여
chmod +x ~/.docker/cli-plugins/docker-compose

# 3) 설치 확인
docker compose version
```

### 3. 스왑 메모리 설정 (권장)

* EC2 t3.micro 인스턴스는 1GB RAM만 제공하므로,
* Docker 컨테이너 실행 시 메모리 부족을 방지하기 위해 스왑 메모리를 설정

```bash
# 2GB 스왑 파일 생성
sudo dd if=/dev/zero of=/swapfile bs=128M count=16

# 권한 수정
sudo chmod 600 /swapfile

# 스왑 파일로 사용하도록 설정
sudo mkswap /swapfile

# 스왑 메모리 활성화
sudo swapon /swapfile

# 스왑 메모리 영구 적용 (재부팅 후에도 유지)
echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab

# 스왑 메모리 확인
free -h
```

### 4. 프로젝트 디렉토리 생성

```bash
sudo mkdir -p /app
cd /app
```

---

## 배포 관련 설정

> **주의**: 프로젝트 최상위 루트(`moviespot/`) 디렉토리에서 실행

### 1. docker-compose.prod.yml

>  DockerHub 별도 계정 사용하는 경우

* `docker-compose.prod.yml` 파일의 이미지 이름 수정

**수정할 부분:**

```yaml
# backend 서비스
image: star1431/moviespot-backend:latest
# ↓ 수정
image: <내DockerHub명>/moviespot-backend:latest

# frontend 서비스
image: star1431/moviespot-frontend:latest
# ↓ 수정
image: <내DockerHub명>/moviespot-frontend:latest
```

* `docker-compose.prod.yml` 수동 배포 시


```bash
scp -i <인스턴스키>.pem docker-compose.prod.yml ec2-user@<퍼블릭IPv4주소>:~/app/
```

### 2. .env 관련 secrets 설정

* 아래 예시를 참고하여 github 액션 시크릿 값 설정
* `.env` 파일은 CI/CD 파이프라인에서 EOF로 생성

```bash
DOCKER_USERNAME=<도커허브아이디>
DOCKER_PASSWORD=<도커허브비밀번호>

EC2_HOST=<퍼블릭IPv4주소>
EC2_USERNAME=ec2-user
EC2_SSH_KEY_PATH=<인스턴스키값>
EC2_PORT=22

MYSQL_ROOT_PASSWORD=<MySQL루트비밀번호>
MYSQL_DATABASE=moviespotdb
MYSQL_USER=<MySQL유저명>
MYSQL_PASSWORD=<MySQL유저비밀번호>

SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/moviespotdb?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=<MySQL유저명>
SPRING_DATASOURCE_PASSWORD=<MySQL유저비밀번호>

CORS_ALLOWED_ORIGINS=http://<퍼블릭IPv4주소> # 또는 DNS 주소

JWT_SECRET=<JWT시크릿키>
JWT_ACCESS_TOKEN_EXP_MS=3600000
JWT_REFRESH_TOKEN_EXP_MS=86400000

GOOGLE_API_ID=<구글OAuth클라이언트ID>
GOOGLE_API_SECRET=<구글OAuth클라이언트시크릿>
NAVER_API_ID=<네이버OAuth클라이언트ID>
NAVER_API_SECRET=<네이버OAuth클라이언트시크릿>
KAKAO_API_ID=<카카오OAuth클라이언트ID>
KAKAO_API_SECRET=<카카오OAuth클라이언트시크릿>

TMDB_API_KEY=<TMDBAPI키>

REACT_APP_API_BASE_URL=http://<퍼블릭IPv4주소> # 또는 DNS 주소
```


### 3. nginx.conf 수동 배포 시

* `nginx.conf` 파일 전송

```bash
scp -i <인스턴스키>.pem -r nginx.conf ec2-user@<퍼블릭IPv4주소>:~/app/
```

---

## AWS EC2 수동 배포 시

> CI/CD를 사용하지 않고, 수동으로 배포하는 경우

#### 1. DockerHub 로그인

```bash
docker login
# DockerHub 사용자명과 비밀번호 입력
```

#### 2. 백엔드 이미지 빌드 및 푸시

```bash
# 프로젝트 루트에서 실행
cd backend

# 이미지 빌드
docker build -t <도커유저명>/moviespot-backend:latest .

# 이미지 푸시
docker push <도커유저명>/moviespot-backend:latest
```

#### 3. 프론트엔드 이미지 빌드 및 푸시

```bash
# 프로젝트 루트에서 실행
cd frontend

# 이미지 빌드
docker build -t <도커유저명>/moviespot-frontend:latest .

# 이미지 푸시
docker push <도커유저명>/moviespot-frontend:latest
```

#### 4. 서버에서 이미지 Pull 및 배포

```bash
ssh -i <인스턴스키>.pem ec2-user@<퍼블릭IPv4주소>
cd ~/app

# 최신 이미지 Pull
docker-compose pull

# 컨테이너 재시작
docker-compose up -d

# 사용하지 않는 이미지 정리
docker image prune -f
```


> 현재는 CI/CD로 자동 배포중인 상태 입니다.


---

## AWS EC2 내 로그 및 이미지 정리

```bash
# 전체 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f nginx

# 사용하지 않는 이미지 정리
docker image prune -f
```