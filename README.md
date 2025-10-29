# 투게더핀즈 백엔드 API

위치 기반 할 일 관리 애플리케이션의 백엔드 서버입니다.

## 프로젝트 개요

투게더핀즈는 사용자가 특정 위치에 도달했을 때 할 일 알림을 받을 수 있는 위치 기반 할 일 관리 서비스입니다.
사용자는 '핀(Pin)'이라는 위치 기반 태그를 생성하고, 해당 핀에 할 일을 등록할 수 있습니다.
또한, 최대 8명까지 그룹을 형성하여 핀과 할 일을 공유할 수 있습니다.

## 기술 스택

- **Language**: Java
- **Framework**: Spring Boot
- **Database**: PostgreSQL
- **Cache**: Redis
- **Authentication**: JWT, OAuth2
- **Real-time**: WebSocket
- **Documentation**: Swagger/OpenAPI 3.0

## 프로젝트 구조

본 프로젝트는 도메인 중심 아키텍처로 구성되어 있으며, 각 도메인은 독립적인 기능을 수행합니다.

```
com.capstone
├── common/          # 공통 유틸리티, 설정, 예외 처리
├── auth/            # 인증 및 토큰 관리
├── user/            # 사용자 프로필 관리
├── task/            # 할 일 관리
├── pin/             # 핀(위치 태그) 관리
├── group/           # 그룹 관리
└── notification/    # 알림 관리
```

각 도메인 모듈은 다음과 같은 구조를 따릅니다:
- `controller/`: API 엔드포인트
- `service/`: 비즈니스 로직
- `repository/`: 데이터베이스 접근
- `entity/`: 도메인 모델
- `dto/`: 데이터 전송 객체
- `enums/`: 열거형 클래스

## 데이터베이스 모델

- **User**: 사용자 정보
- **Task**: 할 일 정보
- **TaskTimeInfo**: 할 일의 시간 정보
- **Pin**: 핀(위치 기반 태그) 정보
- **Group**: 그룹 멤버십 정보
- **Notification**: 알림 정보

## API 엔드포인트

### 인증 (Auth)
사용자 인증 및 토큰 관리

- `POST /api/auth/register`: 사용자 등록
- `POST /api/auth/login`: 사용자 로그인
- `POST /api/auth/logout`: 사용자 로그아웃
- `POST /api/auth/token`: 토큰 발급 및 갱신
- `POST /api/auth/password`: 비밀번호 재설정
- `POST /api/auth/verify`: 토큰 검증

### 사용자 (User)
사용자 프로필 관리

- `POST /api/user`: 사용자 생성
- `GET /api/user/{id}`: 프로필 조회
- `PUT /api/user/{id}`: 프로필 수정
- `DELETE /api/user/{id}`: 프로필 삭제
- `POST /api/user/username`: 아이디 찾기 (전화번호/이메일)
- `POST /api/user/password`: 비밀번호 찾기

### 할 일 (Task)
할 일 관리 및 시간 정보 관리

- `POST /api/task`: 할 일 생성
- `GET /api/task`: 할 일 목록 조회 (사용자별/핀별/완료여부별 필터링 가능)
- `GET /api/task/{id}`: 할 일 단건 조회
- `PUT /api/task/{id}`: 할 일 수정
- `DELETE /api/task/{id}`: 할 일 삭제
- `POST /api/task/{id}/complete`: 할 일 완료 처리
- `POST /api/task/{id}/time`: 할 일 시간 정보 생성
- `GET /api/task/{id}/time`: 할 일 시간 정보 조회
- `PUT /api/task/{id}/time/{timeId}`: 할 일 시간 정보 수정
- `DELETE /api/task/{id}/time/{timeId}`: 할 일 시간 정보 삭제

### 핀 (Pin)
위치 기반 태그 관리

- `POST /api/pin`: 핀 생성
- `GET /api/pin`: 핀 목록 조회 (전체 또는 사용자별)
- `GET /api/pin/{id}`: 핀 단건 조회
- `PUT /api/pin/{id}`: 핀 수정
- `DELETE /api/pin/{id}`: 핀 삭제
- `GET /api/pin/nearby`: 특정 위치 근처의 핀 조회 (위도, 경도, 반경 기반)

### 그룹 (Group)
그룹 관리 및 멤버 관리 (최대 8명)

- `POST /api/group`: 그룹 생성 (핀 소유자만 가능)
- `GET /api/group`: 그룹 목록 조회 (사용자별 또는 핀별)
- `GET /api/group/{id}`: 그룹 단건 조회
- `PUT /api/group/{id}`: 그룹 수정 (핀 변경)
- `DELETE /api/group/{id}`: 그룹 탈퇴
- `POST /api/group/{id}/member`: 그룹 멤버 추가 (핀 소유자만 가능)
- `DELETE /api/group/{id}/member/{memberId}`: 그룹 멤버 제거 (핀 소유자만 가능)

### 알림 (Notification)
알림 관리 및 위치 기반 푸시 알림 (WebSocket)

- `POST /api/notification`: 알림 생성
- `GET /api/notification`: 알림 목록 조회 (읽음여부/타입별 필터링 가능)
- `GET /api/notification/{id}`: 알림 단건 조회
- `DELETE /api/notification/{id}`: 알림 삭제
- `POST /api/notification/{id}/read`: 알림 읽음 처리
- `POST /api/notification/trigger`: 위치 기반 알림 트리거
- `POST /api/notification/push`: 푸시 알림 전송 (WebSocket)
- `GET /api/notification/unread-count`: 읽지 않은 알림 개수 조회

## 주요 기능

### 1. 위치 기반 알림
사용자가 핀에 설정된 위치 반경 내에 도달하면 해당 핀의 할 일들에 대한 알림을 자동으로 받습니다.

### 2. 그룹 공유
최대 8명의 사용자가 하나의 핀을 공유하며, 해당 핀에 속한 할 일들을 함께 관리할 수 있습니다.

### 3. 실시간 푸시 알림
WebSocket을 통해 실시간으로 알림을 전송받을 수 있습니다.

### 4. 시간 기반 할 일 관리
각 할 일에 여러 시간 정보를 등록하여 시간대별로 할 일을 관리할 수 있습니다.

## API 문서

서버 실행 후 Swagger UI를 통해 API 문서를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui.html
```

## 실행 방법
#### 추후 업데이트 예정입니다.

## 보안

- JWT 기반 인증
- Redis를 활용한 토큰 관리
- 비밀번호 암호화 저장
- API 엔드포인트별 권한 검증

## 라이선스

This project is licensed under the MIT License.
